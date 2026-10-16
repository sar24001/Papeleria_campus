package com.example.papeleria_campus.models;

import java.math.BigDecimal;

public class Producto {

    private final int idProducto;
    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final BigDecimal precio;
    private final int stock;

    public Producto(int idProducto, String codigo, String nombre,
                    String categoria, BigDecimal precio, int stock) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }
}
