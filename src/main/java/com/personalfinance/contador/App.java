package com.personalfinance.contador;

import com.personalfinance.contador.service.BackupService;
import com.personalfinance.contador.util.ConfigManager;
import com.personalfinance.contador.util.DatabaseHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Manejador global de excepciones no capturadas
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Excepción no capturada en hilo " + thread.getName() + ": " + throwable.getMessage());
            throwable.printStackTrace();

            // Mostrar diálogo al usuario si es posible
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error Inesperado");
                alert.setHeaderText("Ha ocurrido un error en la aplicación");
                alert.setContentText(throwable.toString());
                alert.showAndWait();
            });
        });

        try {
            // 1. Inicializar Base de Datos (Crear tablas e índices)
            DatabaseHelper.initializeDatabase();

            // 2. Realizar copia de seguridad automática al iniciar
            BackupService.performAutoBackup();

            // 3. Cargar Vista Principal FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // 4. Aplicar Tema Persistido (Claro u Oscuro)
            String theme = ConfigManager.getTheme();
            String stylesheetPath = getClass().getResource("/css/" + theme.toLowerCase() + ".css").toExternalForm();
            scene.getStylesheets().add(stylesheetPath);

            primaryStage.setTitle("Gestor Financiero Personal");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(720);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Error fatal al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
