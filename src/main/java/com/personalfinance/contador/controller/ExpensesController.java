package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Expenditure;
import com.personalfinance.contador.repository.GastoDAO;
import com.personalfinance.contador.service.BudgetService;
import com.personalfinance.contador.service.BudgetService.BudgetReport;
import com.personalfinance.contador.service.BudgetService.BudgetStatus;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExpensesController implements Initializable {

    @FXML
    private Label lblTotalHoy;
    @FXML
    private Label lblTotalSemana;
    @FXML
    private Label lblTotalMes;
    @FXML
    private Label lblTotalAnio;

    @FXML
    private DatePicker dpFecha;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private ComboBox<String> cbCategoria;
    @FXML
    private TextField txtValor;
    @FXML
    private TextField txtObservacion;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cbFiltroCategoria;
    @FXML
    private DatePicker dpFiltroDesde;
    @FXML
    private DatePicker dpFiltroHasta;

    @FXML
    private TableView<Expenditure> tblGastos;
    @FXML
    private TableColumn<Expenditure, Number> colId;
    @FXML
    private TableColumn<Expenditure, String> colFecha;
    @FXML
    private TableColumn<Expenditure, String> colDescripcion;
    @FXML
    private TableColumn<Expenditure, String> colCategoria;
    @FXML
    private TableColumn<Expenditure, Number> colValor;
    @FXML
    private TableColumn<Expenditure, String> colObservacion;

    private final GastoDAO gastoDAO = new GastoDAO();
    private final BudgetService budgetService = new BudgetService();
    private final ObservableList<Expenditure> expensesList = FXCollections.observableArrayList();
    private Expenditure selectedExpense = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final String[] categories = {
            "Arriendo", "Servicios", "Mercado", "Cuota celular", "Parqueadero",
            "Gym", "Aceite moto", "Corte de cabello", "Plan", "Gasolina",
            "Spotify", "Internet", "Otros"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize ComboBoxes
        cbCategoria.setItems(FXCollections.observableArrayList(categories));

        ObservableList<String> filterCategories = FXCollections.observableArrayList("Todas");
        filterCategories.addAll(categories);
        cbFiltroCategoria.setItems(filterCategories);
        cbFiltroCategoria.setValue("Todas");

        // Configure Table Columns
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha().toString()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoria()));
        colValor.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValor()));
        colObservacion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getObservacion()));

        // Format amount column with currency
        colValor.setCellFactory(column -> new TableCell<Expenditure, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item.doubleValue()));
                }
            }
        });

        // Table selection event
        tblGastos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedExpense = newSelection;
                populateForm(selectedExpense);
            }
        });

        dpFecha.setValue(LocalDate.now());

        // Load Data
        loadExpensesData();
    }

    private void loadExpensesData() {
        try {
            List<Expenditure> allExpenses = gastoDAO.findAll();
            expensesList.setAll(allExpenses);
            tblGastos.setItems(expensesList);

            calculateStatistics();
        } catch (SQLException e) {
            showErrorAlert("Error al cargar gastos", e.getMessage());
        }
    }

    private void calculateStatistics() {
        try {
            LocalDate today = LocalDate.now();

            // Total Today
            double todayTotal = gastoDAO.getTotalGastado(today, today);

            // Total Week (Monday to today)
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            double weekTotal = gastoDAO.getTotalGastado(startOfWeek, today);

            // Total Month
            LocalDate startOfMonth = today.withDayOfMonth(1);
            double monthTotal = gastoDAO.getTotalGastado(startOfMonth, today);

            // Total Year
            LocalDate startOfYear = today.withDayOfYear(1);
            double yearTotal = gastoDAO.getTotalGastado(startOfYear, today);

            lblTotalHoy.setText(currencyFormat.format(todayTotal));
            lblTotalSemana.setText(currencyFormat.format(weekTotal));
            lblTotalMes.setText(currencyFormat.format(monthTotal));
            lblTotalAnio.setText(currencyFormat.format(yearTotal));

        } catch (SQLException e) {
            System.err.println("Error calculating statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        LocalDate date = dpFecha.getValue();
        String description = txtDescripcion.getText().trim();
        String category = cbCategoria.getValue();
        double amount = Double.parseDouble(txtValor.getText().trim());
        String note = txtObservacion.getText().trim();

        try {
            // Validate budget and emit alerts if necessary
            BudgetReport report = budgetService.checkNewExpense(category, amount);
            if (report.getStatus() == BudgetStatus.CRITICAL_100) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Límite de Presupuesto Excedido");
                alert.setHeaderText("¡Presupuesto Agotado!");
                alert.setContentText("El gasto que intentas guardar supera el 100% de tu presupuesto en la categoría '" + category + "'.\n" +
                        "Presupuesto: " + currencyFormat.format(report.getPresupuestoDefinido()) + "\n" +
                        "Gastado + Nuevo Gasto: " + currencyFormat.format(report.getTotalGastado() + amount) + "\n\n" +
                        "¿Deseas registrar este gasto de todos modos?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                    return; // Abort insertion
                }
            } else if (report.getStatus() == BudgetStatus.WARNING_80) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Advertencia de Presupuesto");
                alert.setHeaderText("Consumo cercano al límite (>=80%)");
                alert.setContentText("Al guardar este gasto, habrás consumido el " + String.format("%.1f", report.getPorcentajeConsumido()) + "% de tu presupuesto en la categoría '" + category + "'.");
                alert.showAndWait();
            }

            if (selectedExpense == null) {
                // Create
                Expenditure newExpense = new Expenditure(date, description, category, amount, note);
                gastoDAO.insert(newExpense);
            } else {
                // Edit
                selectedExpense.setFecha(date);
                selectedExpense.setDescripcion(description);
                selectedExpense.setCategoria(category);
                selectedExpense.setValor(amount);
                selectedExpense.setObservacion(note);
                gastoDAO.update(selectedExpense);
            }

            loadExpensesData();
            handleLimpiar(null);

        } catch (SQLException e) {
            showErrorAlert("Error al guardar el gasto", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (selectedExpense == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este gasto?");
        alert.setContentText("Descripción: " + selectedExpense.getDescripcion() + "\nValor: " + currencyFormat.format(selectedExpense.getValor()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gastoDAO.delete(selectedExpense.getId());
                loadExpensesData();
                handleLimpiar(null);
            } catch (SQLException e) {
                showErrorAlert("Error al eliminar el gasto", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpiar(ActionEvent event) {
        dpFecha.setValue(LocalDate.now());
        txtDescripcion.clear();
        cbCategoria.setValue(null);
        txtValor.clear();
        txtObservacion.clear();
        selectedExpense = null;
        btnEliminar.setVisible(false);
        tblGastos.getSelectionModel().clearSelection();
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String search = txtBuscar.getText();
        String category = cbFiltroCategoria.getValue();
        LocalDate from = dpFiltroDesde.getValue();
        LocalDate to = dpFiltroHasta.getValue();

        try {
            List<Expenditure> filtered = gastoDAO.findByFilters(from, to, category, search);
            expensesList.setAll(filtered);
            tblGastos.setItems(expensesList);
        } catch (SQLException e) {
            showErrorAlert("Error al filtrar gastos", e.getMessage());
        }
    }

    @FXML
    private void resetFilters(ActionEvent event) {
        txtBuscar.clear();
        cbFiltroCategoria.setValue("Todas");
        dpFiltroDesde.setValue(null);
        dpFiltroHasta.setValue(null);
        loadExpensesData();
    }

    private void populateForm(Expenditure expense) {
        dpFecha.setValue(expense.getFecha());
        txtDescripcion.setText(expense.getDescripcion());
        cbCategoria.setValue(expense.getCategoria());
        txtValor.setText(String.valueOf(expense.getValor()));
        txtObservacion.setText(expense.getObservacion() != null ? expense.getObservacion() : "");
        btnEliminar.setVisible(true);
    }

    private boolean validateForm() {
        if (dpFecha.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona una fecha.");
            return false;
        }
        if (txtDescripcion.getText().trim().isEmpty()) {
            showWarningAlert("Formulario Incompleto", "Por favor ingresa una descripción.");
            return false;
        }
        if (cbCategoria.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona una categoría.");
            return false;
        }
        try {
            double amount = Double.parseDouble(txtValor.getText().trim());
            if (amount < 0) {
                showWarningAlert("Formulario Inválido", "El valor del gasto no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Formulario Inválido", "Por favor ingresa un valor numérico válido.");
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
