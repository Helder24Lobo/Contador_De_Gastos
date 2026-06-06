package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.GastoFijo;
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

public class GastosFijosController implements Initializable {

    @FXML private Label lblTotalFijos;
    @FXML private Label lblPorcentajeIngresos;

    @FXML private TextField txtNombre;
    @FXML private TextField txtValor;
    @FXML private TextField txtDiaCobro;
    @FXML private ComboBox<String> cbEstado;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<GastoFijo> tblGastosFijos;
    @FXML private TableColumn<GastoFijo, Number> colId;
    @FXML private TableColumn<GastoFijo, String> colNombre;
    @FXML private TableColumn<GastoFijo, Number> colValor;
    @FXML private TableColumn<GastoFijo, Number> colDiaCobro;
    @FXML private TableColumn<GastoFijo, String> colEstado;

    private final GastoFijoDAO gastoFijoDAO = new GastoFijoDAO();
    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final ObservableList<GastoFijo> fijosList = FXCollections.observableArrayList();
    private GastoFijo selectedFijo = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEstado.setValue("Activo");

        // Configurar Columnas
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colValor.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValor()));
        colDiaCobro.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDiaCobro()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        colValor.setCellFactory(column -> new TableCell<GastoFijo, Number>() {
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

        // Evento selección tabla
        tblGastosFijos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedFijo = newSelection;
                populateForm(selectedFijo);
            }
        });

        loadGastosFijosData();
    }

    private void loadGastosFijosData() {
        try {
            List<GastoFijo> all = gastoFijoDAO.findAll();
            fijosList.setAll(all);
            tblGastosFijos.setItems(fijosList);

            calculateStatistics();
        } catch (SQLException e) {
            showErrorAlert("Error al cargar gastos fijos", e.getMessage());
        }
    }

    private void calculateStatistics() {
        try {
            double totalFijos = gastoFijoDAO.getTotalGastosFijosActivos();
            lblTotalFijos.setText(currencyFormat.format(totalFijos));

            // Calcular porcentaje respecto a los ingresos del mes actual
            LocalDate now = LocalDate.now();
            LocalDate start = now.withDayOfMonth(1);
            LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
            double totalIngresos = ingresoDAO.getTotalIngresado(start, end);

            if (totalIngresos > 0) {
                double porcentaje = (totalFijos / totalIngresos) * 100;
                lblPorcentajeIngresos.setText(String.format("%.1f%%", porcentaje));
            } else {
                lblPorcentajeIngresos.setText("0.0% (Sin ingresos)");
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular estadísticas de gastos fijos: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        double valor = Double.parseDouble(txtValor.getText().trim());
        int diaCobro = Integer.parseInt(txtDiaCobro.getText().trim());
        String estado = cbEstado.getValue();

        try {
            if (selectedFijo == null) {
                // Crear
                GastoFijo nuevo = new GastoFijo(nombre, valor, diaCobro, estado);
                gastoFijoDAO.insert(nuevo);
            } else {
                // Editar
                selectedFijo.setNombre(nombre);
                selectedFijo.setValor(valor);
                selectedFijo.setDiaCobro(diaCobro);
                selectedFijo.setEstado(estado);
                gastoFijoDAO.update(selectedFijo);
            }

            loadGastosFijosData();
            handleLimpiar(null);
        } catch (SQLException e) {
            showErrorAlert("Error al guardar gasto fijo", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (selectedFijo == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este gasto fijo?");
        alert.setContentText("Nombre: " + selectedFijo.getNombre() + "\nValor: " + currencyFormat.format(selectedFijo.getValor()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gastoFijoDAO.delete(selectedFijo.getId());
                loadGastosFijosData();
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
        cbEstado.setValue("Activo");
        selectedFijo = null;
        btnEliminar.setVisible(false);
        tblGastosFijos.getSelectionModel().clearSelection();
    }

    private void populateForm(GastoFijo gf) {
        txtNombre.setText(gf.getNombre());
        txtValor.setText(String.valueOf(gf.getValor()));
        txtDiaCobro.setText(String.valueOf(gf.getDiaCobro()));
        cbEstado.setValue(gf.getEstado());
        btnEliminar.setVisible(true);
    }

    private boolean validateForm() {
        if (txtNombre.getText().trim().isEmpty()) {
            showWarningAlert("Formulario Incompleto", "Por favor ingresa un nombre para el gasto fijo.");
            return false;
        }
        try {
            double valor = Double.parseDouble(txtValor.getText().trim());
            if (valor < 0) {
                showWarningAlert("Formulario Inválido", "El valor no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Formulario Inválido", "Por favor ingresa un valor mensual válido.");
            return false;
        }
        try {
            int dia = Integer.parseInt(txtDiaCobro.getText().trim());
            if (dia < 1 || dia > 31) {
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
}
