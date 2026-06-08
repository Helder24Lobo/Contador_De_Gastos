package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Specifications;
import com.personalfinance.contador.repository.PresupuestoDAO;
import com.personalfinance.contador.service.BudgetService;
import com.personalfinance.contador.service.BudgetService.BudgetReport;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class BudgetsController implements Initializable {

    @FXML
    private ComboBox<String> cbCategoria;
    @FXML
    private TextField txtValor;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Specifications> tblPresupuestos;
    @FXML
    private TableColumn<Specifications, String> colCategoria;
    @FXML
    private TableColumn<Specifications, Number> colPresupuesto;
    @FXML
    private TableColumn<Specifications, Number> colGastado;
    @FXML
    private TableColumn<Specifications, Void> colConsumo;

    private final PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
    private final BudgetService budgetService = new BudgetService();
    private final ObservableList<Specifications> budgetsList = FXCollections.observableArrayList();
    private Specifications selectedBudget = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final String[] categories = {
            "Arriendo", "Servicios", "Mercado", "Cuota celular", "Parqueadero",
            "Gym", "Aceite moto", "Corte de cabello", "Plan", "Gasolina",
            "Spotify", "Internet", "Otros"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbCategoria.setItems(FXCollections.observableArrayList(categories));

        // Configure Columns
        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoria()));
        colPresupuesto.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValorPresupuestado()));

        // Spent column calculated in real time for the current month
        colGastado.setCellValueFactory(cellData -> {
            try {
                BudgetReport report = budgetService.getCategoryConsumption(cellData.getValue().getCategoria());
                return new SimpleDoubleProperty(report.getTotalGastado());
            } catch (SQLException e) {
                return new SimpleDoubleProperty(0.0);
            }
        });

        // Currency Format
        colPresupuesto.setCellFactory(column -> createCurrencyCell());
        colGastado.setCellFactory(column -> createCurrencyCell());

        // Consumption Column with Progress Bar and Percentage
        colConsumo.setCellFactory(column -> new TableCell<Specifications, Void>() {
            private final ProgressBar progressBar = new ProgressBar(0.0);
            private final Label lblPercentage = new Label("0.0%");
            private final HBox container = new HBox(8, progressBar, lblPercentage);

            {
                progressBar.setPrefWidth(120);
                HBox.setHgrow(progressBar, Priority.ALWAYS);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Specifications budget = getTableView().getItems().get(getIndex());
                    try {
                        BudgetReport report = budgetService.getCategoryConsumption(budget.getCategoria());
                        double percentage = report.getPorcentajeConsumido();
                        double progressFraction = percentage / 100.0;

                        progressBar.setProgress(progressFraction > 1.0 ? 1.0 : progressFraction);
                        lblPercentage.setText(String.format("%.1f%%", percentage));

                        // Change bar color based on percentage
                        if (percentage >= 100.0) {
                            progressBar.setStyle("-fx-accent: #e53e3e;"); // Red
                            lblPercentage.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                        } else if (percentage >= 80.0) {
                            progressBar.setStyle("-fx-accent: #ecc94b;"); // Yellow
                            lblPercentage.setStyle("-fx-text-fill: #d69e2e; -fx-font-weight: bold;");
                        } else {
                            progressBar.setStyle("-fx-accent: #48bb78;"); // Green
                            lblPercentage.setStyle("-fx-text-fill: #38a169;");
                        }
                    } catch (SQLException e) {
                        progressBar.setProgress(0.0);
                        lblPercentage.setText("0.0%");
                    }
                    setGraphic(container);
                }
            }
        });

        // Table selection event
        tblPresupuestos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedBudget = newSelection;
                populateForm(selectedBudget);
            }
        });

        loadBudgetsData();
    }

    private void loadBudgetsData() {
        try {
            List<Specifications> all = presupuestoDAO.findAll();
            budgetsList.setAll(all);
            tblPresupuestos.setItems(budgetsList);
        } catch (SQLException e) {
            showErrorAlert("Error al cargar presupuestos", e.getMessage());
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        String category = cbCategoria.getValue();
        double amount = Double.parseDouble(txtValor.getText().trim());

        try {
            Specifications budget = new Specifications(category, amount, LocalDate.now());
            presupuestoDAO.save(budget);

            loadBudgetsData();
            handleLimpiar(null);
        } catch (SQLException e) {
            showErrorAlert("Error al guardar presupuesto", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (selectedBudget == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este presupuesto?");
        alert.setContentText("Categoría: " + selectedBudget.getCategoria() + "\nPresupuesto: " + currencyFormat.format(selectedBudget.getValorPresupuestado()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                presupuestoDAO.delete(selectedBudget.getId());
                loadBudgetsData();
                handleLimpiar(null);
            } catch (SQLException e) {
                showErrorAlert("Error al eliminar presupuesto", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpiar(ActionEvent event) {
        cbCategoria.setValue(null);
        txtValor.clear();
        selectedBudget = null;
        btnEliminar.setVisible(false);
        tblPresupuestos.getSelectionModel().clearSelection();
    }

    private void populateForm(Specifications budget) {
        cbCategoria.setValue(budget.getCategoria());
        txtValor.setText(String.valueOf(budget.getValorPresupuestado()));
        btnEliminar.setVisible(true);
    }

    private TableCell<Specifications, Number> createCurrencyCell() {
        return new TableCell<Specifications, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item.doubleValue()));
                }
            }
        };
    }

    private boolean validateForm() {
        if (cbCategoria.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona una categoría.");
            return false;
        }
        try {
            double amount = Double.parseDouble(txtValor.getText().trim());
            if (amount < 0) {
                showWarningAlert("Formulario Inválido", "El valor del presupuesto no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Formulario Inválido", "Por favor ingresa un límite de presupuesto mensual válido.");
            return false;
        }
        return true;
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
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
