package com.personalfinance.contador.controller;

import com.personalfinance.contador.util.ConfigManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnGastos;
    @FXML
    private Button btnIngresos;
    @FXML
    private Button btnSavings;
    @FXML
    private Button btnGastosFijos;
    @FXML
    private Button btnPresupuestos;
    @FXML
    private Button btnReportes;
    @FXML
    private Button btnEstadisticas;
    @FXML
    private Button btnConfiguracion;
    @FXML
    private StackPane contentArea;

    private Button currentActiveButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar Dashboard por defecto
        navigateTo("dashboard.fxml", btnDashboard);
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        if (clickedButton == btnDashboard) {
            navigateTo("dashboard.fxml", btnDashboard);
        } else if (clickedButton == btnGastos) {
            navigateTo("gastos.fxml", btnGastos);
        } else if (clickedButton == btnIngresos) {
            navigateTo("ingresos.fxml", btnIngresos);
        } else if (clickedButton == btnSavings) {
            navigateTo("savings.fxml", btnSavings);
        } else if (clickedButton == btnGastosFijos) {
            navigateTo("gastos_fijos.fxml", btnGastosFijos);
        } else if (clickedButton == btnPresupuestos) {
            navigateTo("presupuestos.fxml", btnPresupuestos);
        } else if (clickedButton == btnReportes) {
            navigateTo("reportes.fxml", btnReportes);
        } else if (clickedButton == btnEstadisticas) {
            navigateTo("estadisticas.fxml", btnEstadisticas);
        } else if (clickedButton == btnConfiguracion) {
            navigateTo("configuracion.fxml", btnConfiguracion);
        }
    }

    public void navigateTo(String fxmlFile, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent view = loader.load();

            // Si navegamos a la vista de configuración, pasarle una referencia de MainController para poder alternar el tema visual
            if (fxmlFile.equals("configuracion.fxml")) {
                ConfigurationController configController = loader.getController();
                configController.setMainController(this);
            }

            contentArea.getChildren().setAll(view);
            if (sourceButton != null) {
                updateActiveButton(sourceButton);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar la vista " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("menu-button-active");
        }
        button.getStyleClass().add("menu-button-active");
        currentActiveButton = button;
    }

    /**
     * Aplica el tema visual actual (claro/oscuro) a la escena.
     */
    public void applyTheme() {
        if (contentArea.getScene() == null) return;
        String theme = ConfigManager.getTheme();
        contentArea.getScene().getStylesheets().clear();
        String stylesheetPath = getClass().getResource("/css/" + theme.toLowerCase() + ".css").toExternalForm();
        contentArea.getScene().getStylesheets().add(stylesheetPath);
    }
}
