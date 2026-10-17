package com.example.papeleria_campus.dao;




import com.example.papeleria_campus.connection.DatabaseConnection;
import com.example.papeleria_campus.models.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Consultar productos con filtros
    public List<Producto> buscar(
            String texto,
            String categoria,
            BigDecimal precioMinimo,
            boolean conExistencias
    ) throws SQLException {

        String busqueda = texto == null ? "" : texto.trim();

        StringBuilder sql = new StringBuilder(
                "SELECT id_producto, codigo, nombre, categoria, " +
                        "precio, stock FROM catalogo.producto WHERE 1=1"
        );

        if (!busqueda.isEmpty()) {
            sql.append(
                    " AND (codigo ILIKE ? OR nombre ILIKE ?)"
            );
        }

        if (categoria != null && !categoria.equals("Todas")) {
            sql.append(" AND categoria = ?");
        }

        if (precioMinimo != null) {
            sql.append(" AND precio >= ?");
        }

        if (conExistencias) {
            sql.append(" AND stock > 0");
        }

        sql.append(" ORDER BY codigo ASC");

        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql.toString())) {

            int indice = 1;

            if (!busqueda.isEmpty()) {
                ps.setString(indice++, "%" + busqueda + "%");
                ps.setString(indice++, "%" + busqueda + "%");
            }

            if (categoria != null && !categoria.equals("Todas")) {
                ps.setString(indice++, categoria);
            }

            if (precioMinimo != null) {
                ps.setBigDecimal(indice++, precioMinimo);
            }

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

                    productos.add(producto);
                }
            }

            return productos;

        } catch (SQLException e) {
            registrarError("BUSCAR", e);
            throw e;
        }
    }

    // Insertar producto
    public int insertar(Producto producto) throws SQLException {

        String sql = """
            INSERT INTO catalogo.producto
            (codigo, nombre, categoria, precio, stock)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            asignarDatos(ps, producto);

            return ps.executeUpdate();

        } catch (SQLException e) {
            registrarError("INSERTAR", e);
            throw e;
        }
    }

    // Actualizar producto por ID
    public int actualizar(Producto producto) throws SQLException {

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

            asignarDatos(ps, producto);
            ps.setInt(6, producto.getIdProducto());

            return ps.executeUpdate();

        } catch (SQLException e) {
            registrarError("ACTUALIZAR", e);
            throw e;
        }
    }

    // Eliminar producto por ID
    public int eliminar(int idProducto) throws SQLException {

        String sql = """
            DELETE FROM catalogo.producto
            WHERE id_producto = ?
            """;

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            return ps.executeUpdate();

        } catch (SQLException e) {
            registrarError("ELIMINAR", e);
            throw e;
        }
    }

    private void asignarDatos(
            PreparedStatement ps,
            Producto producto
    ) throws SQLException {

        ps.setString(1, producto.getCodigo());
        ps.setString(2, producto.getNombre());
        ps.setString(3, producto.getCategoria());
        ps.setBigDecimal(4, producto.getPrecio());
        ps.setInt(5, producto.getStock());
    }

    private void registrarError(
            String operacion,
            SQLException e
    ) {
        System.err.printf(
                "ProductoDAO | Operación: %s | SQLState: %s%n",
                operacion,
                e.getSQLState()
        );
    }
}

