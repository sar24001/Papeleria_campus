module com.example.papeleria_campus {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.papeleria_campus to javafx.fxml;
    exports com.example.papeleria_campus;
    exports com.example.papeleria_campus.controller;
    opens com.example.papeleria_campus.controller to javafx.fxml;
}