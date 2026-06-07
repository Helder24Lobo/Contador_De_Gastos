package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Presupuesto;
import com.personalfinance.contador.repository.GastoDAO;
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

public class PresupuestosController implements Initializable {

    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtValor;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<Presupuesto> tblPresupuestos;
    @FXML private TableColumn<Presupuesto, String> colCategoria;
    @FXML private TableColumn<Presupuesto, Number> colPresupuesto;
    @FXML private TableColumn<Presupuesto, Number> colGastado;
    @FXML private TableColumn<Presupuesto, Void> colConsumo;

    private final PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
    private final BudgetService budgetService = new BudgetService();
    private final ObservableList<Presupuesto> presupuestosList = FXCollections.observableArrayList();
    private Presupuesto selectedPresupuesto = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final String[] categorias = {
            "Arriendo", "Servicios", "Mercado", "Cuota celular", "Parqueadero", 
            "Gym", "Aceite moto", "Corte de cabello", "Plan", "Gasolina", 
            "Spotify", "Internet", "Otros"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbCategoria.setItems(FXCollections.observableArrayList(categorias));

        // Configurar Columnas
        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoria()));
        colPresupuesto.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValorPresupuestado()));
        
        // Columna Gastado calculada en tiempo real para el mes en curso
        colGastado.setCellValueFactory(cellData -> {
            try {
                BudgetReport report = budgetService.getCategoryConsumption(cellData.getValue().getCategoria());
                return new SimpleDoubleProperty(report.getTotalGastado());
            } catch (SQLException e) {
                return new SimpleDoubleProperty(0.0);
            }
        });

        // Formato Moneda
        colPresupuesto.setCellFactory(column -> createCurrencyCell());
        colGastado.setCellFactory(column -> createCurrencyCell());

        // Columna Consumo con Barra de Progreso y Porcentaje
        colConsumo.setCellFactory(column -> new TableCell<Presupuesto, Void>() {
            private final ProgressBar pb = new ProgressBar(0.0);
            private final Label lblPorcentaje = new Label("0.0%");
            private final HBox container = new HBox(8, pb, lblPorcentaje);

            {
                pb.setPrefWidth(120);
                HBox.setHgrow(pb, Priority.ALWAYS);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Presupuesto p = getTableView().getItems().get(getIndex());
                    try {
                        BudgetReport report = budgetService.getCategoryConsumption(p.getCategoria());
                        double porcentaje = report.getPorcentajeConsumido();
                        double progressFraction = porcentaje / 100.0;

                        pb.setProgress(progressFraction > 1.0 ? 1.0 : progressFraction);
                        lblPorcentaje.setText(String.format("%.1f%%", porcentaje));

                        // Cambiar color de la barra según porcentaje
                        if (porcentaje >= 100.0) {
                            pb.setStyle("-fx-accent: #e53e3e;"); // Rojo
                            lblPorcentaje.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                        } else if (porcentaje >= 80.0) {
                            pb.setStyle("-fx-accent: #ecc94b;"); // Amarillo
                            lblPorcentaje.setStyle("-fx-text-fill: #d69e2e; -fx-font-weight: bold;");
                        } else {
                            pb.setStyle("-fx-accent: #48bb78;"); // Verde
                            lblPorcentaje.setStyle("-fx-text-fill: #38a169;");
                        }
                    } catch (SQLException e) {
                        pb.setProgress(0.0);
                        lblPorcentaje.setText("0.0%");
                    }
                    setGraphic(container);
                }
            }
        });

        // Evento selección tabla
        tblPresupuestos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPresupuesto = newSelection;
                populateForm(selectedPresupuesto);
            }
        });

        loadPresupuestosData();
    }

    private void loadPresupuestosData() {
        try {
            List<Presupuesto> all = presupuestoDAO.findAll();
            presupuestosList.setAll(all);
            tblPresupuestos.setItems(presupuestosList);
        } catch (SQLException e) {
            showErrorAlert("Error al cargar presupuestos", e.getMessage());
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        String categoria = cbCategoria.getValue();
        double valor = Double.parseDouble(txtValor.getText().trim());

        try {
            Presupuesto pres = new Presupuesto(categoria, valor, LocalDate.now());
            presupuestoDAO.save(pres);

            loadPresupuestosData();
            handleLimpiar(null);
        } catch (SQLException e) {
            showErrorAlert("Error al guardar presupuesto", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (selectedPresupuesto == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este presupuesto?");
        alert.setContentText("Categoría: " + selectedPresupuesto.getCategoria() + "\nPresupuesto: " + currencyFormat.format(selectedPresupuesto.getValorPresupuestado()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                presupuestoDAO.delete(selectedPresupuesto.getId());
                loadPresupuestosData();
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
        selectedPresupuesto = null;
        btnEliminar.setVisible(false);
        tblPresupuestos.getSelectionModel().clearSelection();
    }

    private void populateForm(Presupuesto p) {
        cbCategoria.setValue(p.getCategoria());
        txtValor.setText(String.valueOf(p.getValorPresupuestado()));
        btnEliminar.setVisible(true);
    }

    private TableCell<Presupuesto, Number> createCurrencyCell() {
        return new TableCell<Presupuesto, Number>() {
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
            double valor = Double.parseDouble(txtValor.getText().trim());
            if (valor < 0) {
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
