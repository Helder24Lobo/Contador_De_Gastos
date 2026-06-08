package com.personalfinance.contador.controller;

import com.personalfinance.contador.repository.GastoDAO;
import com.personalfinance.contador.repository.GastoFijoDAO;
import com.personalfinance.contador.repository.IngresoDAO;
import com.personalfinance.contador.repository.PresupuestoDAO;
import com.personalfinance.contador.service.BackupService;
import com.personalfinance.contador.util.ConfigManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConfigurationController implements Initializable {

    @FXML
    private ComboBox<String> cbTema;

    private MainController mainController;

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final GastoDAO gastoDAO = new GastoDAO();
    private final GastoFijoDAO gastoFijoDAO = new GastoFijoDAO();
    private final PresupuestoDAO presupuestoDAO = new PresupuestoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbTema.setItems(FXCollections.observableArrayList("Oscuro", "Claro"));

        String currentTheme = ConfigManager.getTheme();
        if (currentTheme.equalsIgnoreCase("light")) {
            cbTema.setValue("Claro");
        } else {
            cbTema.setValue("Oscuro");
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleCambiarTema(ActionEvent event) {
        String seleccion = cbTema.getValue();
        if (seleccion == null) return;

        if (seleccion.equals("Claro")) {
            ConfigManager.setTheme("light");
        } else {
            ConfigManager.setTheme("dark");
        }

        if (mainController != null) {
            mainController.applyTheme();
        }
    }

    @FXML
    private void handleExportarBaseDatos(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Base de Datos (Backup)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Base de datos SQLite (*.db)", "*.db"));
        fileChooser.setInitialFileName("backup_contador_gastos.db");

        File file = fileChooser.showSaveDialog(cbTema.getScene().getWindow());
        if (file != null) {
            try {
                BackupService.exportDatabase(file);
                showSuccessAlert("Respaldo Exitoso", "Los datos se han respaldado correctamente en:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showErrorAlert("Error al exportar datos", e.getMessage());
            }
        }
    }

    @FXML
    private void handleImportarBaseDatos(ActionEvent event) {
        Alert alertConfirm = new Alert(AlertType.CONFIRMATION);
        alertConfirm.setTitle("Confirmar Restauración de Datos");
        alertConfirm.setHeaderText("¡Atención! Se reemplazarán todos los datos actuales");
        alertConfirm.setContentText("Al importar otra base de datos, se sobrescribirán los ingresos, gastos y presupuestos actuales.\n\n¿Deseas continuar?");

        Optional<ButtonType> opt = alertConfirm.showAndWait();
        if (opt.isPresent() && opt.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Importar Base de Datos (Restaurar)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Base de datos SQLite (*.db)", "*.db"));

            File file = fileChooser.showOpenDialog(cbTema.getScene().getWindow());
            if (file != null) {
                try {
                    BackupService.importDatabase(file);
                    showSuccessAlert("Restauración Exitosa", "La base de datos se ha importado con éxito.\nSe recomienda recargar el sistema.");

                    // Recargar vista de configuración
                    if (mainController != null) {
                        mainController.navigateTo("dashboard.fxml", null); // Redirigir al inicio para refrescar
                    }
                } catch (Exception e) {
                    showErrorAlert("Error al importar datos", e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleRestablecerBaseDatos(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Restablecimiento");
        alert.setHeaderText("ZONA PELIGROSA: SE BORRARÁ TODO");
        alert.setContentText("¿Estás absolutamente seguro de eliminar todos los datos de la aplicación?\nEsta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ingresoDAO.clearAll();
                gastoDAO.clearAll();
                gastoFijoDAO.clearAll();
                presupuestoDAO.clearAll();

                showSuccessAlert("Restablecimiento Completado", "Todos los datos han sido borrados de la aplicación.");

                if (mainController != null) {
                    mainController.navigateTo("dashboard.fxml", null);
                }
            } catch (Exception e) {
                showErrorAlert("Error al restablecer base de datos", e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
