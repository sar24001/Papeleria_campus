# Catálogo de Productos – Papelería Campus

Aplicación de escritorio desarrollada en **Java 21 y JavaFX**, diseñada para gestionar el catálogo de productos de la Papelería Campus mediante operaciones CRUD (Crear, Leer, Actualizar y Eliminar).

El sistema utiliza **PostgreSQL** para almacenar la información y **JDBC** para establecer la comunicación con la base de datos. Implementa el patrón de diseño **DAO (Data Access Object)**, permitiendo separar las operaciones SQL de la lógica del controlador y de la interfaz gráfica.

## 1. Objetivo del proyecto

Desarrollar una aplicación de escritorio que facilite la administración de productos de una papelería, permitiendo consultar precios, controlar existencias, registrar nuevos artículos y mantener actualizada la información almacenada.

El proyecto corresponde a la asignatura **Programación de Aplicaciones de Escritorio** y tiene como finalidad aplicar los conocimientos de JavaFX, PostgreSQL, JDBC, validaciones y operaciones CRUD.

## 2. Funcionalidades principales

### Gestión de productos

- **Registrar:** permite agregar nuevos productos al catálogo.
- **Consultar:** muestra los productos almacenados en PostgreSQL mediante un `TableView`.
- **Actualizar:** modifica los datos de un producto previamente seleccionado.
- **Eliminar:** elimina productos mediante una confirmación del usuario.
- **Nuevo:** limpia los campos del formulario para registrar otro producto.
- **Recargar:** consulta nuevamente la base de datos para actualizar el listado.

### Búsqueda y filtros

- Búsqueda parcial por código o nombre, sin distinguir entre mayúsculas y minúsculas.
- Filtrado por las categorías Papelería, Tecnología y Accesorios.
- Opción para mostrar únicamente productos con existencias.
- Combinación de los filtros disponibles.
- Restablecimiento de filtros para visualizar nuevamente el catálogo.
- Contador de productos visibles según los resultados de la consulta.

### Validaciones

La aplicación valida la información ingresada antes de realizar operaciones en PostgreSQL:

- Código obligatorio, único y de máximo 10 caracteres.
- Normalización de códigos a mayúsculas y eliminación de espacios laterales.
- Nombre obligatorio de 3 a 80 caracteres.
- Selección de una categoría válida.
- Precio numérico, positivo y con máximo dos decimales.
- Stock entero y no negativo.
- Prevención de códigos duplicados.
- Confirmación antes de eliminar registros.
- Manejo de errores de conexión y restricciones SQL.

## 3. Tecnologías utilizadas

| Componente | Versión |
|---|---|
| Java JDK | Eclipse Temurin 21.0.10 |
| JavaFX | 21.0.6 |
| PostgreSQL | 18.4 |
| PostgreSQL JDBC Driver | 42.7.13 |
| Maven Wrapper | 3.8.5 |
| IntelliJ IDEA | 2025.3.3 |
| Sistema operativo | Windows 11 |
| Java Language Level | 21 |

### Dependencias principales

El proyecto utiliza Maven para administrar las dependencias declaradas en `pom.xml`.

| Dependencia | Versión | Función |
|---|---|---|
| `javafx-controls` | 21.0.6 | Componentes gráficos de JavaFX |
| `javafx-fxml` | 21.0.6 | Carga y administración de vistas FXML |
| `postgresql` | 42.7.13 | Conexión JDBC con PostgreSQL |
| `junit-jupiter-api` | 5.12.1 | Dependencia para pruebas unitarias |
| `junit-jupiter-engine` | 5.12.1 | Motor de ejecución de pruebas JUnit |

Las dependencias JUnit forman parte de la configuración del proyecto. Actualmente no se documentan pruebas automatizadas implementadas.

**Plugins de Maven:**

- `maven-compiler-plugin` 3.13.0
- `javafx-maven-plugin` 0.0.8

## 4. Estructura del proyecto

La aplicación está organizada en paquetes que permiten distribuir las responsabilidades del sistema.

```text
prueba_corte_2/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
├── pruebas.md
├── evidencias/
│
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   │
        │   └── ni/edu/uam/prueba_corte_2/
        │       ├── Launcher.java
        │       │
        │       ├── Application/
        │       │   └── Producto_Application.java
        │       │
        │       ├── Controller/
        │       │   └── Producto_Controller.java
        │       │
        │       ├── DAO/
        │       │   └── ProductoDAO.java
        │       │
        │       ├── Connection/
        │       │   └── database_connection.java
        │       │
        │       ├── Models/
        │       │   └── Producto.java
        │       │
        │       └── sql/
        │           ├── Script.sql
        │           └── Datos.sql
        │
        └── resources/
            └── ni/edu/uam/prueba_corte_2/
                └── productos-view.fxml
```

## 5. Arquitectura del sistema

La aplicación implementa una arquitectura que separa la presentación, el control de eventos, el acceso a datos y la conexión con PostgreSQL.

### Modelo — `Producto.java`

Representa la entidad Producto y contiene los atributos utilizados para almacenar y consultar su información.

### Controlador — `Producto_Controller.java`

Gestiona la interacción entre el usuario y la interfaz gráfica.

Se encarga de recibir los eventos de los botones, validar los datos del formulario, gestionar la selección de productos y actualizar el contenido del `TableView`.

### DAO — `ProductoDAO.java`

Implementa el patrón **Data Access Object** y centraliza las operaciones de acceso a la base de datos.

Entre sus responsabilidades se encuentran:

- Ejecutar consultas SQL mediante `PreparedStatement`.
- Obtener productos con los filtros solicitados.
- Insertar nuevos registros.
- Actualizar productos existentes.
- Eliminar productos mediante su identificador.
- Comprobar códigos duplicados.
- Gestionar excepciones SQL y registrar información de diagnóstico.

### Conexión — `database_connection.java`

Establece la conexión con PostgreSQL mediante JDBC.

Utiliza los parámetros de conexión definidos en las variables de entorno del sistema, permitiendo mantener las credenciales fuera del código fuente.

### Vista — `productos-view.fxml`

Define la interfaz gráfica desarrollada con JavaFX y Scene Builder. Contiene los formularios, botones, filtros y la tabla de productos.

### Flujo de información

```text
Usuario
   │
   ▼
productos-view.fxml
   │
   ▼
Producto_Controller
   │
   ▼
ProductoDAO
   │
   ▼
database_connection
   │
   ▼
PostgreSQL
```

Los registros recuperados desde PostgreSQL se convierten en objetos `Producto` y posteriormente se incorporan a una `ObservableList`, que permite visualizar la información en el `TableView`.

## 6. Base de datos

El sistema utiliza PostgreSQL con la siguiente configuración:

- **Base de datos:** `papeleria_campus`
- **Esquema:** `catalogos`
- **Tabla:** `catalogos.producto`

### Estructura de la tabla

| Campo | Tipo de dato | Restricciones |
|---|---|---|
| `id_producto` | SERIAL | PRIMARY KEY |
| `codigo` | VARCHAR(10) | NOT NULL, UNIQUE, CHECK |
| `nombre` | VARCHAR(80) | NOT NULL, CHECK |
| `categoria` | VARCHAR(20) | NOT NULL, CHECK |
| `precio` | NUMERIC(10,2) | NOT NULL, CHECK |
| `stock` | INTEGER | NOT NULL, CHECK |
| `estado` | SMALLINT | CHECK, DEFAULT 1 |

Las categorías permitidas son Papelería, Tecnología y Accesorios.

El campo `estado` admite los valores `0` y `1`, siendo `1` su valor predeterminado.

La base de datos contiene inicialmente **12 productos de prueba**, cuyos precios se expresan en córdobas (C$).

### Scripts SQL

Los archivos se encuentran en:

`src/main/java/ni/edu/uam/prueba_corte_2/sql/`

| Archivo | Descripción |
|---|---|
| `Script.sql` | Crea la base de datos, el esquema, la tabla y sus restricciones |
| `Datos.sql` | Inserta los 12 productos iniciales |

## 7. Instalación y configuración

### Requisitos previos

Antes de ejecutar la aplicación se necesita:

1. Java JDK 21 instalado.
2. PostgreSQL instalado y en ejecución.
3. IntelliJ IDEA o un IDE compatible con Maven y JavaFX.
4. Acceso a los archivos del proyecto.
5. Credenciales de conexión a PostgreSQL.

### Paso 1. Crear la base de datos

Abrir pgAdmin y conectarse a la base de datos `postgres`.

Ejecutar:

```sql
CREATE DATABASE papeleria_campus;
```

Posteriormente, conectarse a `papeleria_campus` y ejecutar el resto de `Script.sql` para crear el esquema, la tabla y sus restricciones.

Finalmente, ejecutar `Datos.sql` para insertar los 12 productos iniciales.

### Paso 2. Configurar la conexión

La conexión con PostgreSQL se configura mediante variables de entorno del sistema.

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_URL` | Dirección JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/papeleria_campus` |
| `DB_USER` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña del usuario | Contraseña local |

En IntelliJ IDEA, estas variables pueden configurarse desde:

**Run → Edit Configurations → Environment variables**

La clase `database_connection.java` utiliza estos valores para establecer la conexión mediante JDBC.

Si la conexión falla, la aplicación muestra un mensaje de error y registra la información de diagnóstico correspondiente en la consola.

### Paso 3. Ejecutar el proyecto

**Opción A. Maven Wrapper**

Desde la carpeta raíz del proyecto, ejecutar en Windows:

```bat
.\mvnw.cmd clean javafx:run
```

En Linux o macOS:

```bash
./mvnw clean javafx:run
```

**Opción B. IntelliJ IDEA**

1. Abrir la carpeta del proyecto.
2. Esperar la sincronización de Maven.
3. Comprobar que el JDK seleccionado sea Java 21.
4. Verificar que PostgreSQL esté ejecutándose.
5. Configurar las variables de entorno.
6. Ejecutar la clase `ni.edu.uam.prueba_corte_2.Launcher`.

## 8. Uso de la aplicación

### Registrar productos

Completar los campos del formulario: código, nombre, categoría, precio y stock.

Presionar **Guardar** para validar la información y registrar el producto en PostgreSQL.

### Consultar productos

Al iniciar la aplicación, se consultan los registros disponibles y se muestran en el `TableView`, ordenados por código.

### Actualizar productos

Seleccionar un registro en la tabla, modificar los datos necesarios y presionar **Actualizar**.

La operación identifica el producto mediante su `id_producto`.

### Eliminar productos

Seleccionar un registro y presionar **Eliminar**.

El sistema solicita una confirmación antes de eliminar definitivamente el producto.

### Buscar y filtrar

Ingresar parte del código o nombre de un producto y presionar **Buscar**.

También se puede seleccionar una categoría o activar la opción para mostrar únicamente productos con existencias.

Los filtros se combinan mediante consultas SQL parametrizadas, sin limitarse a filtrar una lista local.

El botón **Limpiar filtros** restablece los criterios de búsqueda.

## 9. Validaciones y manejo de errores

La aplicación implementa validaciones tanto en JavaFX como en PostgreSQL.

Estas permiten controlar:

- Campos obligatorios sin completar.
- Códigos demasiado largos o duplicados.
- Nombres que no cumplen la longitud establecida.
- Categorías no permitidas.
- Precios inválidos, negativos o fuera del rango permitido.
- Precios con más de dos decimales.
- Stock negativo, decimal o fuera del rango de `INTEGER`.
- Operaciones de actualización o eliminación sin seleccionar un registro.
- Fallos de conexión y errores de restricciones SQL.

El acceso a datos utiliza `PreparedStatement` para parametrizar los valores y `try-with-resources` para cerrar los recursos JDBC utilizados.

Cuando se produce una excepción SQL, el sistema muestra un mensaje comprensible y registra información de diagnóstico, incluyendo el `SQLState`, sin mostrar credenciales al usuario.

## 10. Pruebas funcionales

El proyecto contempla la verificación de búsquedas, filtros, operaciones CRUD y validaciones mediante una base de datos local de pruebas.

### Pruebas de búsqueda

| Código | Caso de prueba | Resultado esperado |
|---|---|---|
| B1 | Buscar `CUADERNO` | 2 productos |
| B2 | Buscar `T00` | 4 productos |
| B3 | Filtrar categoría `Papelería` | 4 productos |
| B4 | Solo productos con existencias | 9 productos |
| B5 | `USB` + `Tecnología` + existencias | 2 productos |
| B6 | Buscar `ZZZ999` | 0 productos |
| B7 | Limpiar filtros | 12 productos |

### Pruebas CRUD

Se considera el siguiente ciclo de prueba:

1. Registrar el producto temporal `X001`.
2. Verificar que el catálogo contenga 13 productos.
3. Actualizar el precio y stock del producto temporal.
4. Comprobar que la actualización se refleje en PostgreSQL.
5. Cancelar una eliminación y verificar que el registro permanezca.
6. Confirmar la eliminación y comprobar que el catálogo vuelva a contener 12 productos.
7. Reiniciar la aplicación para verificar la persistencia de los cambios.

## 11. Seguridad e integridad de la información

El sistema considera las siguientes medidas:

- Consultas parametrizadas con `PreparedStatement`.
- Restricciones de integridad mediante `PRIMARY KEY`, `UNIQUE`, `NOT NULL` y `CHECK`.
- Validación de entradas antes de realizar operaciones SQL.
- Manejo controlado de excepciones.
- Cierre adecuado de conexiones, sentencias y resultados JDBC.
- Confirmación para operaciones de eliminación.
- Configuración de conexión mediante variables de entorno.
- Separación del acceso a datos mediante el patrón DAO.

Estas medidas permiten mantener la consistencia de la información almacenada en PostgreSQL.

## 12. Documentación y control de versiones

El proyecto utiliza Git para registrar los cambios del código fuente y facilitar el seguimiento de su desarrollo.

La documentación incluye los scripts SQL, las instrucciones de instalación, la estructura de clases y las pruebas funcionales.

Se recomienda excluir mediante `.gitignore` los archivos temporales del IDE, los directorios de compilación y otros archivos locales que no formen parte del código fuente.

## 13. Autor

**Maria Alejandra Sarante Salinas**

**Asignatura:** Programación de Aplicaciones de Escritorio

---

**Última actualización de referencia:** 15 de octubre de 2026.
