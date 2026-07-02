package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.FixedExpense;
import com.personalfinance.contador.repository.GastoFijoDAO;
import com.personalfinance.contador.repository.IngresoDAO;
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
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class FixedExpensesController implements Initializable {

    @FXML
    private Label lblTotalFijos;
    @FXML
    private Label lblPorcentajeIngresos;
    @FXML
    private Label lblTotalFijosLista;

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtValor;
    @FXML
    private TextField txtDiaCobro;
    @FXML
    private ComboBox<String> cbEstado;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<FixedExpense> tblGastosFijos;
    @FXML
    private TableColumn<FixedExpense, Number> colId;
    @FXML
    private TableColumn<FixedExpense, String> colNombre;
    @FXML
    private TableColumn<FixedExpense, Number> colValor;
    @FXML
    private TableColumn<FixedExpense, Number> colDiaCobro;
    @FXML
    private TableColumn<FixedExpense, String> colEstado;

    private final GastoFijoDAO gastoFijoDAO = new GastoFijoDAO();
    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final ObservableList<FixedExpense> fixedExpensesList = FXCollections.observableArrayList();
    private FixedExpense selectedFixedExpense = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbEstado.setItems(FXCollections.observableArrayList("Pagado", "Por pagar"));
        cbEstado.setValue("Por pagar");

        // Configure Table Columns
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colValor.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValor()));
        colDiaCobro.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDiaCobro()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        colValor.setCellFactory(column -> new TableCell<FixedExpense, Number>() {
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

        // Row styling depending on paid/unpaid status
        tblGastosFijos.setRowFactory(tv -> new TableRow<FixedExpense>() {
            @Override
            protected void updateItem(FixedExpense item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("pagado", "por-pagar");
                if (item == null || empty) {
                    // Keep default styles
                } else {
                    if ("Pagado".equalsIgnoreCase(item.getEstado())) {
                        getStyleClass().add("pagado");
                    } else if ("Por pagar".equalsIgnoreCase(item.getEstado())) {
                        getStyleClass().add("por-pagar");
                    }
                }
            }
        });

        // Table selection event
        tblGastosFijos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedFixedExpense = newSelection;
                populateForm(selectedFixedExpense);
            }
        });

        fixedExpensesList.addListener((javafx.collections.ListChangeListener<FixedExpense>) c -> updateTotalFijosLista());

        loadFixedExpensesData();
    }

    private void loadFixedExpensesData() {
        try {
            List<FixedExpense> all = gastoFijoDAO.findAll();
            fixedExpensesList.setAll(all);
            tblGastosFijos.setItems(fixedExpensesList);

            calculateStatistics();
        } catch (SQLException e) {
            showErrorAlert("Error al cargar gastos fijos", e.getMessage());
        }
    }

    private void calculateStatistics() {
        try {
            double totalFixed = gastoFijoDAO.getTotalGastosFijosActivos();
            lblTotalFijos.setText(currencyFormat.format(totalFixed));

            // Calculate percentage relative to current month's income
            LocalDate now = LocalDate.now();
            LocalDate start = now.withDayOfMonth(1);
            LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
            double totalIncomes = ingresoDAO.getTotalIngresado(start, end);

            if (totalIncomes > 0) {
                double percentage = (totalFixed / totalIncomes) * 100;
                lblPorcentajeIngresos.setText(String.format("%.1f%%", percentage));
            } else {
                lblPorcentajeIngresos.setText("0.0% (Sin ingresos)");
            }

        } catch (SQLException e) {
            System.err.println("Error calculating fixed expense statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        String name = txtNombre.getText().trim();
        double amount = Double.parseDouble(txtValor.getText().trim());
        int billingDay = Integer.parseInt(txtDiaCobro.getText().trim());
        String status = cbEstado.getValue();

        try {
            if (selectedFixedExpense == null) {
                // Create
                FixedExpense newFixedExpense = new FixedExpense(name, amount, billingDay, status);
                gastoFijoDAO.insert(newFixedExpense);
            } else {
                // Edit
                selectedFixedExpense.setNombre(name);
                selectedFixedExpense.setValor(amount);
                selectedFixedExpense.setDiaCobro(billingDay);
                selectedFixedExpense.setEstado(status);
                gastoFijoDAO.update(selectedFixedExpense);
            }

            loadFixedExpensesData();
            handleLimpiar(null);
        } catch (SQLException e) {
            showErrorAlert("Error al guardar gasto fijo", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (selectedFixedExpense == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este gasto fijo?");
        alert.setContentText("Nombre: " + selectedFixedExpense.getNombre() + "\nValor: " + currencyFormat.format(selectedFixedExpense.getValor()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gastoFijoDAO.delete(selectedFixedExpense.getId());
                loadFixedExpensesData();
                handleLimpiar(null);
            } catch (SQLException e) {
                showErrorAlert("Error al eliminar gasto fijo", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpiar(ActionEvent event) {
        txtNombre.clear();
        txtValor.clear();
        txtDiaCobro.clear();
        cbEstado.setValue("Por pagar");
        selectedFixedExpense = null;
        btnEliminar.setVisible(false);
        tblGastosFijos.getSelectionModel().clearSelection();
    }

    private void populateForm(FixedExpense fixedExpense) {
        txtNombre.setText(fixedExpense.getNombre());
        txtValor.setText(String.valueOf(fixedExpense.getValor()));
        txtDiaCobro.setText(String.valueOf(fixedExpense.getDiaCobro()));
        cbEstado.setValue(fixedExpense.getEstado());
        btnEliminar.setVisible(true);
    }

    private boolean validateForm() {
        if (txtNombre.getText().trim().isEmpty()) {
            showWarningAlert("Formulario Incompleto", "Por favor ingresa un nombre para el gasto fijo.");
            return false;
        }
        try {
            double amount = Double.parseDouble(txtValor.getText().trim());
            if (amount < 0) {
                showWarningAlert("Formulario Inválido", "El valor no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Formulario Inválido", "Por favor ingresa un valor mensual válido.");
            return false;
        }
        try {
            int day = Integer.parseInt(txtDiaCobro.getText().trim());
            if (day < 1 || day > 31) {
                showWarningAlert("Formulario Inválido", "El día de cobro debe estar entre 1 y 31.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Formulario Inválido", "Por favor ingresa un día de cobro numérico válido (1-31).");
            return false;
        }
        if (cbEstado.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona un estado.");
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

    private void updateTotalFijosLista() {
        double total = fixedExpensesList.stream().mapToDouble(FixedExpense::getValor).sum();
        lblTotalFijosLista.setText(currencyFormat.format(total));
    }
}
