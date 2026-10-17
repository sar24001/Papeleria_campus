--Creacion de la base de datos
CREATE DATABASE bdpapeleria_campus;

-- Creación del esquema
CREATE SCHEMA catalogo;

-- Creación de la tabla producto
CREATE TABLE catalogo.producto
(
    id_producto SERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    categoria VARCHAR(20) NOT NULL,
    precio NUMERIC(10,2) NOT NULL,
    stock INTEGER NOT NULL,

    -- Validar código normalizado, no vacío y en mayúsculas
    CONSTRAINT chk_producto_codigo
        CHECK (
            codigo = UPPER(TRIM(codigo))
                AND LENGTH(TRIM(codigo)) BETWEEN 1 AND 10
            ),

    -- Validar longitud del nombre
    CONSTRAINT chk_producto_nombre
        CHECK (
            LENGTH(TRIM(nombre)) BETWEEN 3 AND 80
            ),

    -- Validar categorías permitidas
    CONSTRAINT chk_producto_categoria
        CHECK (
            categoria IN (
                          'Papelería',
                          'Tecnología',
                          'Accesorios'
                )
            ),

    -- Validar precio positivo
    CONSTRAINT chk_producto_precio
        CHECK (precio > 0),

    -- Validar stock no negativo
    CONSTRAINT chk_producto_stock
        CHECK (stock >= 0)
);