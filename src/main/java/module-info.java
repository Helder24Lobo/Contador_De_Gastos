module com.personalfinance.contador {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.desktop;
    
    requires org.xerial.sqlitejdbc;
    requires com.github.librepdf.openpdf;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.personalfinance.contador to javafx.fxml;
    opens com.personalfinance.contador.controller to javafx.fxml;
    opens com.personalfinance.contador.model to javafx.base;
    
    exports com.personalfinance.contador;
    exports com.personalfinance.contador.controller;
    exports com.personalfinance.contador.model;
}
