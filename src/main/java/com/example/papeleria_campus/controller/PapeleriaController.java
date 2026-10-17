
package com.example.papeleria_campus.controller;

import com.example.papeleria_campus.dao.ProductoDAO;
import com.example.papeleria_campus.models.Producto;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class PapeleriaController {

    // Campos del formulario
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private ComboBox<String> cmbCategoria;

    // Filtros
    @FXML private TextField txtBuscar;
    @FXML private TextField txtPrecioMin;
    @FXML private ComboBox<String> cmbfiltroCategoria;
    @FXML private CheckBox chkConExistencias;

    // Tabla de productos
    @FXML private TableView<Producto> tablaProductos;

    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    // Información del listado
    @FXML private Label lblContador;
    @FXML private Label lblEstado;

    // Acceso a datos
    private final ProductoDAO productoDAO = new ProductoDAO();

    private final ObservableList<Producto> productos =
            FXCollections.observableArrayList();

    private static final BigDecimal PRECIO_MAXIMO =
            new BigDecimal("99999999.99");

    private static final List<String> CATEGORIAS = List.of(
            "Papelería",
            "Tecnología",
            "Accesorios"
    );

    // Inicialización
    @FXML
    public void initialize() {

        cmbCategoria.getItems().setAll(CATEGORIAS);
        cmbCategoria.setEditable(false);

        cmbfiltroCategoria.getItems().setAll(
                "Todas",
                "Papelería",
                "Tecnología",
                "Accesorios"
        );

        cmbfiltroCategoria.setValue("Todas");
        cmbfiltroCategoria.setEditable(false);

        configurarColumnas();

        tablaProductos.setItems(productos);

        tablaProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {

                    if (actual != null) {
                        cargarFormulario(actual);
                    }
                });

        cargarProductos(null);
    }

    // Configuración del TableView
    private void configurarColumnas() {

        colCodigo.setCellValueFactory(
                dato -> new ReadOnlyStringWrapper(
                        dato.getValue().getCodigo()
                )
        );

        colNombre.setCellValueFactory(
                dato -> new ReadOnlyStringWrapper(
                        dato.getValue().getNombre()
                )
        );

        colCategoria.setCellValueFactory(
                dato -> new ReadOnlyStringWrapper(
                        dato.getValue().getCategoria()
                )
        );

        colPrecio.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getPrecio()
                )
        );

        colStock.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getStock()
                )
        );

        colPrecio.setCellFactory(columna -> new TableCell<>() {

            @Override
            protected void updateItem(
                    BigDecimal precio,
                    boolean vacio
            ) {
                super.updateItem(precio, vacio);

                if (vacio || precio == null) {
                    setText(null);
                } else {
                    setText("C$ " + precio.toPlainString());
                }
            }
        });
    }

    // Consultar PostgreSQL y actualizar la tabla
    private void cargarProductos(Integer idSeleccionado) {

        BigDecimal minimo;

        try {
            minimo = validarPrecioMinimo();
        } catch (IllegalArgumentException e) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Filtro inválido",
                    e.getMessage()
            );
            return;
        }

        Producto seleccionadoAnterior =
                tablaProductos.getSelectionModel().getSelectedItem();

        Integer idConservar = idSeleccionado;

        if (idConservar == null && seleccionadoAnterior != null) {
            idConservar = seleccionadoAnterior.getIdProducto();
        }

        try {
            List<Producto> resultado = productoDAO.buscar(
                    txtBuscar.getText(),
                    cmbfiltroCategoria.getValue(),
                    minimo,
                    chkConExistencias.isSelected()
            );

            productos.setAll(resultado);

            lblContador.setText(
                    "Registros visibles: " + productos.size()
            );

            lblEstado.setText(
                    productos.isEmpty() ? "Sin coincidencias" : ""
            );

            tablaProductos.setPlaceholder(
                    new Label("Sin coincidencias")
            );

            tablaProductos.getSelectionModel().clearSelection();

            if (idConservar != null) {

                Producto encontrado = null;

                for (Producto producto : productos) {
                    if (producto.getIdProducto() == idConservar) {
                        encontrado = producto;
                        break;
                    }
                }

                if (encontrado != null) {
                    tablaProductos.getSelectionModel().select(encontrado);
                } else {
                    limpiarFormularioInterno();
                }
            }

        } catch (SQLException e) {

            productos.clear();

            lblContador.setText("Registros visibles: --");
            lblEstado.setText("Error al consultar");

            tablaProductos.setPlaceholder(
                    new Label("No se pudieron cargar los productos")
            );

            manejarErrorSQL("CONSULTAR", e);
        }
    }

    // Botón Nuevo
    @FXML
    private void NuevoProducto() {

        limpiarFormularioInterno();
        txtCodigo.requestFocus();
    }

    // Botón Guardar
    @FXML
    private void GuardarProducto() {

        try {
            Producto producto = obtenerFormulario(0);

            int filas = productoDAO.insertar(producto);

            if (filas == 1) {

                limpiarFormularioInterno();
                cargarProductos(null);

                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Producto guardado",
                        "El producto se registró correctamente."
                );

            } else {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "No se guardó",
                        "No se insertó ningún registro."
                );
            }

        } catch (IllegalArgumentException e) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Validación",
                    e.getMessage()
            );

        } catch (SQLException e) {

            manejarErrorSQL("GUARDAR", e);
        }
    }

    // Botón Actualizar
    @FXML
    private void ActualizarProducto() {

        Producto seleccionado =
                tablaProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccionar producto",
                    "Debe seleccionar un producto para actualizar."
            );

            return;
        }

        try {
            Producto producto = obtenerFormulario(
                    seleccionado.getIdProducto()
            );

            int filas = productoDAO.actualizar(producto);

            if (filas == 0) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Producto no encontrado",
                        "El producto ya no existe en la base de datos."
                );

                limpiarFormularioInterno();
                cargarProductos(null);
                return;
            }

            cargarProductos(producto.getIdProducto());

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Producto actualizado",
                    "El producto se actualizó correctamente."
            );

        } catch (IllegalArgumentException e) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Validación",
                    e.getMessage()
            );

        } catch (SQLException e) {

            manejarErrorSQL("ACTUALIZAR", e);
        }
    }

    // Botón Eliminar
    @FXML
    private void EliminarProducto() {

        Producto seleccionado =
                tablaProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccionar producto",
                    "Debe seleccionar un producto para eliminar."
            );

            return;
        }

        ButtonType confirmar = new ButtonType(
                "Eliminar",
                ButtonBar.ButtonData.OK_DONE
        );

        ButtonType cancelar = new ButtonType(
                "Cancelar",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el producto " +
                        seleccionado.getNombre() + "?",
                confirmar,
                cancelar
        );

        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Eliminar producto");

        ButtonType respuesta = confirmacion
                .showAndWait()
                .orElse(cancelar);

        if (respuesta != confirmar) {
            return;
        }

        try {
            int filas = productoDAO.eliminar(
                    seleccionado.getIdProducto()
            );

            if (filas == 0) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Producto no encontrado",
                        "El producto ya no existe en la base de datos."
                );

                limpiarFormularioInterno();
                cargarProductos(null);
                return;
            }

            limpiarFormularioInterno();
            cargarProductos(null);

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Producto eliminado",
                    "El producto se eliminó correctamente."
            );

        } catch (SQLException e) {

            manejarErrorSQL("ELIMINAR", e);
        }
    }

    // Botón Buscar
    @FXML
    private void BuscarProducto() {
        cargarProductos(null);
    }

    // Botón Limpiar filtros
    @FXML
    private void LimpiarFiltro() {

        txtBuscar.clear();
        txtPrecioMin.clear();

        cmbfiltroCategoria.setValue("Todas");
        chkConExistencias.setSelected(false);

        cargarProductos(null);
    }

    // Botón Recargar
    @FXML
    private void RecargarProductos() {
        cargarProductos(null);
    }

    // Mostrar datos del producto seleccionado
    private void cargarFormulario(Producto producto) {

        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());

        cmbCategoria.setValue(producto.getCategoria());

        txtPrecio.setText(
                producto.getPrecio().toPlainString()
        );

        txtStock.setText(
                String.valueOf(producto.getStock())
        );
    }

    // Limpiar formulario sin modificar filtros
    private void limpiarFormularioInterno() {

        tablaProductos.getSelectionModel().clearSelection();

        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();

        cmbCategoria.getSelectionModel().clearSelection();
    }

    // Leer y validar los campos del formulario
    private Producto obtenerFormulario(int idProducto) {

        String codigo = txtCodigo.getText()
                .trim()
                .toUpperCase(Locale.ROOT);

        String nombre = txtNombre.getText().trim();

        String categoria = cmbCategoria.getValue();

        String textoPrecio = txtPrecio.getText().trim();

        String textoStock = txtStock.getText().trim();

        // Validar código
        if (codigo.isEmpty() || codigo.length() > 10) {
            throw new IllegalArgumentException(
                    "El código debe tener entre 1 y 10 caracteres."
            );
        }

        // Validar nombre
        if (nombre.length() < 3 || nombre.length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre debe tener entre 3 y 80 caracteres."
            );
        }

        // Validar categoría
        if (!CATEGORIAS.contains(categoria)) {
            throw new IllegalArgumentException(
                    "Seleccione una categoría válida."
            );
        }

        // Validar formato del precio
        if (!textoPrecio.matches("-?\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException(
                    "El precio debe ser numérico, utilizar punto " +
                            "decimal y tener como máximo 2 decimales."
            );
        }

        BigDecimal precio = new BigDecimal(textoPrecio);

        // Validar rango del precio
        if (precio.compareTo(BigDecimal.ZERO) <= 0 ||
                precio.compareTo(PRECIO_MAXIMO) > 0) {

            throw new IllegalArgumentException(
                    "El precio debe estar entre 0.01 y 99999999.99."
            );
        }

        // Validar formato del stock
        if (!textoStock.matches("-?\\d+")) {

            throw new IllegalArgumentException(
                    "El stock debe ser un número entero."
            );
        }

        int stock;

        try {
            stock = Integer.parseInt(textoStock);
        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "El stock excede el rango permitido de INTEGER."
            );
        }

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo."
            );
        }

        // Normalizar los campos en la interfaz
        txtCodigo.setText(codigo);
        txtNombre.setText(nombre);

        return new Producto(
                idProducto,
                codigo,
                nombre,
                categoria,
                precio,
                stock
        );
    }

    // Validación del filtro de precio mínimo
    private BigDecimal validarPrecioMinimo() {

        String texto = txtPrecioMin.getText().trim();

        if (texto.isEmpty()) {
            return null;
        }

        if (!texto.matches("\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException(
                    "El precio mínimo debe ser numérico " +
                            "y tener como máximo 2 decimales."
            );
        }

        BigDecimal minimo = new BigDecimal(texto);

        if (minimo.compareTo(PRECIO_MAXIMO) > 0) {
            throw new IllegalArgumentException(
                    "El precio mínimo excede el rango permitido."
            );
        }

        return minimo;
    }

    // Manejar errores de PostgreSQL
    private void manejarErrorSQL(
            String operacion,
            SQLException e
    ) {

        String estado = e.getSQLState();

        System.err.printf(
                "Controlador | Operación: %s | SQLState: %s%n",
                operacion,
                estado
        );

        String mensaje;

        if ("23505".equals(estado)) {

            mensaje = "Ya existe un producto con ese código.";

        } else if ("23514".equals(estado)
                || "23502".equals(estado)
                || "22003".equals(estado)) {

            mensaje = "Los datos no cumplen las " +
                    "restricciones de PostgreSQL.";

        } else if (estado != null && estado.startsWith("08")) {

            mensaje = "No fue posible conectar con PostgreSQL. " +
                    "Revise la configuración y el servidor.";

        } else if (estado != null && estado.startsWith("28")) {

            mensaje = "No fue posible autenticar la conexión " +
                    "con PostgreSQL.";

        } else {

            mensaje = "No fue posible completar la operación. " +
                    "Verifique la conexión y los datos.";
        }

        mostrarAlerta(
                Alert.AlertType.ERROR,
                "Error de base de datos",
                mensaje
        );
    }

    // Mostrar mensajes
    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje
    ) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
