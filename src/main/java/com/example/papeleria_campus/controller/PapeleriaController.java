
package com.example.papeleria_campus.controller;

import com.example.papeleria_campus.connection.DatabaseConnection;
import com.example.papeleria_campus.models.Producto;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Locale;

public class PapeleriaController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private ComboBox<String> cmbCategoria;

    // Campos de búsqueda
    @FXML private TextField txtBuscar;
    @FXML private TextField txtPrecioMin;
    @FXML private ComboBox<String> cmbfiltroCategoria;
    @FXML private CheckBox chkConExistencias;

    // Tabla de productos
    @FXML private TableView<Producto> tablaProductos;

    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    // Información
    @FXML private Label lblContador;
    @FXML private Label lblEstado;

    private final ObservableList<Producto> listaProductos =
            FXCollections.observableArrayList();

    private static final BigDecimal PRECIO_MAXIMO =
            new BigDecimal("99999999.99");

    private static final List<String> CATEGORIAS = List.of(
            "Papelería",
            "Tecnología",
            "Accesorios"
    );

    // Inicializar controlador
    @FXML
    public void initialize() {

        cmbCategoria.getItems().setAll(CATEGORIAS);

        cmbfiltroCategoria.getItems().setAll(
                "Todas",
                "Papelería",
                "Tecnología",
                "Accesorios"
        );

        cmbCategoria.setEditable(false);
        cmbfiltroCategoria.setEditable(false);
        cmbfiltroCategoria.setValue("Todas");

        configurarTabla();

        tablaProductos.setItems(listaProductos);

        tablaProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        cargarFormulario(seleccionado);
                    }
                });

        cargarProductos(null);
    }

    // Configurar columnas del TableView
    private void configurarTabla() {

        colId.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getIdProducto()
                )
        );

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
            protected void updateItem(BigDecimal precio, boolean vacio) {
                super.updateItem(precio, vacio);

                if (vacio || precio == null) {
                    setText(null);
                } else {
                    setText("C$ " + precio.toPlainString());
                }
            }
        });
    }

    // Consultar productos desde PostgreSQL
    private void cargarProductos(Integer idSeleccionado) {

        BigDecimal precioMinimo;

        try {
            precioMinimo = obtenerPrecioMinimo();
        } catch (IllegalArgumentException e) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Filtro inválido",
                    e.getMessage()
            );
            return;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT id_producto, codigo, nombre,
                       categoria, precio, stock
                FROM catalogo.producto
                WHERE 1=1
                """);

        String texto = txtBuscar.getText().trim();
        String categoria = cmbfiltroCategoria.getValue();

        if (!texto.isEmpty()) {
            sql.append(" AND (codigo ILIKE ? OR nombre ILIKE ?)");
        }

        if (categoria != null && !categoria.equals("Todas")) {
            sql.append(" AND categoria = ?");
        }

        if (precioMinimo != null) {
            sql.append(" AND precio >= ?");
        }

        if (chkConExistencias.isSelected()) {
            sql.append(" AND stock > 0");
        }

        sql.append(" ORDER BY codigo ASC");

        Integer idConservar = idSeleccionado;

        if (idConservar == null) {
            Producto actual = tablaProductos
                    .getSelectionModel().getSelectedItem();

            if (actual != null) {
                idConservar = actual.getIdProducto();
            }
        }

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql.toString())) {

            int indice = 1;

            if (!texto.isEmpty()) {
                ps.setString(indice++, "%" + texto + "%");
                ps.setString(indice++, "%" + texto + "%");
            }

            if (categoria != null && !categoria.equals("Todas")) {
                ps.setString(indice++, categoria);
            }

            if (precioMinimo != null) {
                ps.setBigDecimal(indice++, precioMinimo);
            }

            ObservableList<Producto> resultados =
                    FXCollections.observableArrayList();

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Producto producto = new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getBigDecimal("precio"),
                            rs.getInt("stock")
                    );

                    resultados.add(producto);
                }
            }

            listaProductos.setAll(resultados);

            lblContador.setText(
                    "Registros visibles: " + listaProductos.size()
            );

            lblEstado.setText(
                    listaProductos.isEmpty() ? "Sin coincidencias" : ""
            );

            tablaProductos.setPlaceholder(
                    new Label("Sin coincidencias")
            );

            tablaProductos.getSelectionModel().clearSelection();

            Producto encontrado = null;

            if (idConservar != null) {
                for (Producto producto : listaProductos) {
                    if (producto.getIdProducto() == idConservar) {
                        encontrado = producto;
                        break;
                    }
                }
            }

            if (encontrado != null) {
                tablaProductos.getSelectionModel().select(encontrado);
            } else {
                limpiarFormulario();
            }

        } catch (SQLException e) {

            listaProductos.clear();
            limpiarFormulario();

            lblContador.setText("Registros visibles: --");
            lblEstado.setText("Error de conexión o consulta");

            tablaProductos.setPlaceholder(
                    new Label("Error al consultar PostgreSQL")
            );

            manejarErrorSQL("CONSULTAR", e);
        }
    }

    // Botón Nuevo
    @FXML
    private void NuevoProducto() {

        limpiarFormulario();
        txtCodigo.requestFocus();
    }

    // Botón Guardar
    @FXML
    private void GuardarProducto() {

        Producto producto;

        try {
            producto = obtenerDatosFormulario(0);
        } catch (IllegalArgumentException e) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Validación",
                    e.getMessage()
            );
            return;
        }

        String sql = """
                INSERT INTO catalogo.producto
                (codigo, nombre, categoria, precio, stock)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            asignarParametros(ps, producto);

            int filas = ps.executeUpdate();

            if (filas == 1) {

                limpiarFormulario();
                cargarProductos(null);

                mostrarMensaje(
                        Alert.AlertType.INFORMATION,
                        "Guardar producto",
                        "Producto guardado correctamente."
                );

            } else {
                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "Guardar producto",
                        "No se insertó ningún registro."
                );
            }

        } catch (SQLException e) {
            manejarErrorSQL("GUARDAR", e);
        }
    }

    // Botón Actualizar
    @FXML
    private void ActualizarProducto() {

        Producto seleccionado = tablaProductos
                .getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Actualizar producto",
                    "Seleccione un producto de la tabla."
            );

            return;
        }

        Producto producto;

        try {
            producto = obtenerDatosFormulario(
                    seleccionado.getIdProducto()
            );
        } catch (IllegalArgumentException e) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Validación",
                    e.getMessage()
            );
            return;
        }

        String sql = """
                UPDATE catalogo.producto
                SET codigo = ?,
                    nombre = ?,
                    categoria = ?,
                    precio = ?,
                    stock = ?
                WHERE id_producto = ?
                """;

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            asignarParametros(ps, producto);

            ps.setInt(6, producto.getIdProducto());

            int filas = ps.executeUpdate();

            if (filas == 0) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "Actualizar producto",
                        "El producto ya no existe."
                );

                limpiarFormulario();
                cargarProductos(null);
                return;
            }

            cargarProductos(producto.getIdProducto());

            mostrarMensaje(
                    Alert.AlertType.INFORMATION,
                    "Actualizar producto",
                    "Producto actualizado correctamente."
            );

        } catch (SQLException e) {
            manejarErrorSQL("ACTUALIZAR", e);
        }
    }

    // Botón Eliminar
    @FXML
    private void EliminarProducto() {

        Producto seleccionado = tablaProductos
                .getSelectionModel().getSelectedItem();

        if (seleccionado == null) {

            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Eliminar producto",
                    "Seleccione un producto de la tabla."
            );

            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(
                "¿Desea eliminar este producto?"
        );

        confirmacion.setContentText(
                seleccionado.getCodigo() + " - " +
                        seleccionado.getNombre()
        );

        if (confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String sql = """
                DELETE FROM catalogo.producto
                WHERE id_producto = ?
                """;

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, seleccionado.getIdProducto());

            int filas = ps.executeUpdate();

            if (filas == 0) {

                mostrarMensaje(
                        Alert.AlertType.WARNING,
                        "Eliminar producto",
                        "El producto ya no existe."
                );

                limpiarFormulario();
                cargarProductos(null);
                return;
            }

            limpiarFormulario();
            cargarProductos(null);

            mostrarMensaje(
                    Alert.AlertType.INFORMATION,
                    "Eliminar producto",
                    "Producto eliminado correctamente."
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

    // Cargar producto seleccionado al formulario
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

    // Limpiar campos del formulario
    private void limpiarFormulario() {

        tablaProductos.getSelectionModel().clearSelection();

        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();

        cmbCategoria.getSelectionModel().clearSelection();
    }

    // Validar y obtener datos del formulario
    private Producto obtenerDatosFormulario(int idProducto) {

        String codigo = txtCodigo.getText()
                .trim()
                .toUpperCase(Locale.ROOT);

        String nombre = txtNombre.getText().trim();
        String categoria = cmbCategoria.getValue();

        String precioTexto = txtPrecio.getText().trim();
        String stockTexto = txtStock.getText().trim();

        // Código
        if (codigo.isEmpty() || codigo.length() > 10) {
            throw new IllegalArgumentException(
                    "El código debe tener entre 1 y 10 caracteres."
            );
        }

        // Nombre
        if (nombre.length() < 3 || nombre.length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre debe tener entre 3 y 80 caracteres."
            );
        }

        // Categoría
        if (!CATEGORIAS.contains(categoria)) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una categoría válida."
            );
        }

        // Precio
        if (!precioTexto.matches("-?\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException(
                    "Ingrese un precio numérico con máximo 2 decimales."
            );
        }

        BigDecimal precio = new BigDecimal(precioTexto);

        if (precio.compareTo(BigDecimal.ZERO) <= 0 ||
                precio.compareTo(PRECIO_MAXIMO) > 0) {

            throw new IllegalArgumentException(
                    "El precio debe estar entre 0.01 y 99999999.99."
            );
        }

        // Stock
        if (!stockTexto.matches("-?\\d+")) {
            throw new IllegalArgumentException(
                    "El stock debe ser un número entero."
            );
        }

        int stock;

        try {
            stock = Integer.parseInt(stockTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El stock excede el rango permitido."
            );
        }

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo."
            );
        }

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

    // Validar precio mínimo del filtro
    private BigDecimal obtenerPrecioMinimo() {

        String texto = txtPrecioMin.getText().trim();

        if (texto.isEmpty()) {
            return null;
        }

        if (!texto.matches("\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException(
                    "El precio mínimo debe ser numérico y " +
                            "tener como máximo 2 decimales."
            );
        }

        BigDecimal precio = new BigDecimal(texto);

        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new IllegalArgumentException(
                    "El precio mínimo excede el máximo permitido."
            );
        }

        return precio;
    }

    // Asignar valores a los PreparedStatement
    private void asignarParametros(
            PreparedStatement ps,
            Producto producto
    ) throws SQLException {

        ps.setString(1, producto.getCodigo());
        ps.setString(2, producto.getNombre());
        ps.setString(3, producto.getCategoria());
        ps.setBigDecimal(4, producto.getPrecio());
        ps.setInt(5, producto.getStock());
    }

    // Manejar errores de PostgreSQL
    private void manejarErrorSQL(
            String operacion,
            SQLException e
    ) {

        String estado = e.getSQLState();

        System.err.println(
                "Operación: " + operacion +
                        " | SQLState: " + estado
        );

        String mensaje;

        if ("23505".equals(estado)) {

            mensaje = "Ya existe un producto con ese código.";

        } else if ("23514".equals(estado)
                || "23502".equals(estado)
                || "22003".equals(estado)) {

            mensaje = "Los datos no cumplen las restricciones.";

        } else if (estado != null && estado.startsWith("08")) {

            mensaje = "No se pudo conectar con PostgreSQL.";

        } else if (estado != null && estado.startsWith("28")) {

            mensaje = "Error de autenticación en PostgreSQL.";

        } else {

            mensaje = "No fue posible completar la operación.";
        }

        mostrarMensaje(
                Alert.AlertType.ERROR,
                "Error en base de datos",
                mensaje
        );
    }

    // Mostrar mensajes
    private void mostrarMensaje(
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
