package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Income;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class IncomeController implements Initializable {

    @FXML
    private DatePicker dpFecha;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private ComboBox<String> cbTipo;
    @FXML
    private TextField txtValor;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cbFiltroTipo;

    @FXML
    private TableView<Income> tblIngresos;
    @FXML
    private TableColumn<Income, Number> colId;
    @FXML
    private TableColumn<Income, String> colFecha;
    @FXML
    private TableColumn<Income, String> colDescripcion;
    @FXML
    private TableColumn<Income, String> colTipo;
    @FXML
    private TableColumn<Income, Number> colValor;

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final ObservableList<Income> incomesList = FXCollections.observableArrayList();
    private Income selectedIncome = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final String[] incomeTypes = {
            "Salario", "Bonificación", "Venta", "Freelance", "Otros"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize ComboBoxes
        cbTipo.setItems(FXCollections.observableArrayList(incomeTypes));

        ObservableList<String> filterTypes = FXCollections.observableArrayList("Todos");
        filterTypes.addAll(incomeTypes);
        cbFiltroTipo.setItems(filterTypes);
        cbFiltroTipo.setValue("Todos");

        // Configure Table Columns
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha().toString()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo()));
        colValor.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValor()));

        colValor.setCellFactory(column -> new TableCell<Income, Number>() {
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
        tblIngresos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedIncome = newSelection;
                populateForm(selectedIncome);
            }
        });

        dpFecha.setValue(LocalDate.now());

        loadIncomesData();
    }

    private void loadIncomesData() {
        try {
            List<Income> all = ingresoDAO.findAll();
            incomesList.setAll(all);
            tblIngresos.setItems(incomesList);
        } catch (SQLException e) {
            showErrorAlert("Error al cargar ingresos", e.getMessage());
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        LocalDate date = dpFecha.getValue();
        String description = txtDescripcion.getText().trim();
        String type = cbTipo.getValue();
        double amount = Double.parseDouble(txtValor.getText().trim());

        try {
            if (selectedIncome == null) {
                // Create
                Income newIncome = new Income(date, description, amount, type);
                ingresoDAO.insert(newIncome);
            } else {
                // Edit
                selectedIncome.setFecha(date);
                selectedIncome.setDescripcion(description);
                selectedIncome.setTipo(type);
                selectedIncome.setValor(amount);
                ingresoDAO.update(selectedIncome);
            }

            loadIncomesData();
            handleLimpiar(null);
        } catch (SQLException e) {
            showErrorAlert("Error al guardar ingreso", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (selectedIncome == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este ingreso?");
        alert.setContentText("Descripción: " + selectedIncome.getDescripcion() + "\nValor: " + currencyFormat.format(selectedIncome.getValor()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ingresoDAO.delete(selectedIncome.getId());
                loadIncomesData();
                handleLimpiar(null);
            } catch (SQLException e) {
                showErrorAlert("Error al eliminar ingreso", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpiar(ActionEvent event) {
        dpFecha.setValue(LocalDate.now());
        txtDescripcion.clear();
        cbTipo.setValue(null);
        txtValor.clear();
        selectedIncome = null;
        btnEliminar.setVisible(false);
        tblIngresos.getSelectionModel().clearSelection();
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String search = txtBuscar.getText();
        String type = cbFiltroTipo.getValue();

        try {
            List<Income> filtered = ingresoDAO.findByFilters(null, null, type, search);
            incomesList.setAll(filtered);
            tblIngresos.setItems(incomesList);
        } catch (SQLException e) {
            showErrorAlert("Error al filtrar ingresos", e.getMessage());
        }
    }

    @FXML
    private void resetFilters(ActionEvent event) {
        txtBuscar.clear();
        cbFiltroTipo.setValue("Todos");
        loadIncomesData();
    }

    private void populateForm(Income income) {
        dpFecha.setValue(income.getFecha());
        txtDescripcion.setText(income.getDescripcion());
        cbTipo.setValue(income.getTipo());
        txtValor.setText(String.valueOf(income.getValor()));
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
        if (cbTipo.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona un tipo de ingreso.");
            return false;
        }
        try {
            double amount = Double.parseDouble(txtValor.getText().trim());
            if (amount < 0) {
                showWarningAlert("Formulario Inválido", "El valor del ingreso no puede ser negativo.");
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
