package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Gasto;
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

public class GastosController implements Initializable {

    @FXML private Label lblTotalHoy;
    @FXML private Label lblTotalSemana;
    @FXML private Label lblTotalMes;
    @FXML private Label lblTotalAnio;

    @FXML private DatePicker dpFecha;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtValor;
    @FXML private TextField txtObservacion;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroCategoria;
    @FXML private DatePicker dpFiltroDesde;
    @FXML private DatePicker dpFiltroHasta;

    @FXML private TableView<Gasto> tblGastos;
    @FXML private TableColumn<Gasto, Number> colId;
    @FXML private TableColumn<Gasto, String> colFecha;
    @FXML private TableColumn<Gasto, String> colDescripcion;
    @FXML private TableColumn<Gasto, String> colCategoria;
    @FXML private TableColumn<Gasto, Number> colValor;
    @FXML private TableColumn<Gasto, String> colObservacion;

    private final GastoDAO gastoDAO = new GastoDAO();
    private final BudgetService budgetService = new BudgetService();
    private final ObservableList<Gasto> gastosList = FXCollections.observableArrayList();
    private Gasto selectedGasto = null;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final String[] categorias = {
            "Arriendo", "Servicios", "Mercado", "Cuota celular", "Parqueadero", 
            "Gym", "Aceite moto", "Corte de cabello", "Plan", "Gasolina", 
            "Spotify", "Internet", "Otros"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar ComboBoxes
        cbCategoria.setItems(FXCollections.observableArrayList(categorias));
        
        ObservableList<String> filtrosCategorias = FXCollections.observableArrayList("Todas");
        filtrosCategorias.addAll(categorias);
        cbFiltroCategoria.setItems(filtrosCategorias);
        cbFiltroCategoria.setValue("Todas");

        // Configurar Columnas de la Tabla
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha().toString()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoria()));
        colValor.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValor()));
        colObservacion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getObservacion()));

        // Formatear columna valor con moneda
        colValor.setCellFactory(column -> new TableCell<Gasto, Number>() {
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

        // Evento de selección de la tabla
        tblGastos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedGasto = newSelection;
                populateForm(selectedGasto);
            }
        });

        dpFecha.setValue(LocalDate.now());

        // Cargar Datos
        loadGastosData();
    }

    private void loadGastosData() {
        try {
            List<Gasto> allGastos = gastoDAO.findAll();
            gastosList.setAll(allGastos);
            tblGastos.setItems(gastosList);

            calculateStatistics();
        } catch (SQLException e) {
            showErrorAlert("Error al cargar gastos", e.getMessage());
        }
    }

    private void calculateStatistics() {
        try {
            LocalDate today = LocalDate.now();
            
            // Total Hoy
            double hoy = gastoDAO.getTotalGastado(today, today);
            
            // Total Semana (Lunes a hoy)
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            double semana = gastoDAO.getTotalGastado(startOfWeek, today);
            
            // Total Mes
            LocalDate startOfMonth = today.withDayOfMonth(1);
            double mes = gastoDAO.getTotalGastado(startOfMonth, today);
            
            // Total Año
            LocalDate startOfYear = today.withDayOfYear(1);
            double anio = gastoDAO.getTotalGastado(startOfYear, today);

            lblTotalHoy.setText(currencyFormat.format(hoy));
            lblTotalSemana.setText(currencyFormat.format(semana));
            lblTotalMes.setText(currencyFormat.format(mes));
            lblTotalAnio.setText(currencyFormat.format(anio));

        } catch (SQLException e) {
            System.err.println("Error al calcular estadísticas: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        LocalDate fecha = dpFecha.getValue();
        String descripcion = txtDescripcion.getText().trim();
        String categoria = cbCategoria.getValue();
        double valor = Double.parseDouble(txtValor.getText().trim());
        String observacion = txtObservacion.getText().trim();

        try {
            // Validar presupuesto y emitir alertas si es necesario
            BudgetReport report = budgetService.checkNewExpense(categoria, valor);
            if (report.getStatus() == BudgetStatus.CRITICAL_100) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Límite de Presupuesto Excedido");
                alert.setHeaderText("¡Presupuesto Agotado!");
                alert.setContentText("El gasto que intentas guardar supera el 100% de tu presupuesto en la categoría '" + categoria + "'.\n" +
                        "Presupuesto: " + currencyFormat.format(report.getPresupuestoDefinido()) + "\n" +
                        "Gastado + Nuevo Gasto: " + currencyFormat.format(report.getTotalGastado() + valor) + "\n\n" +
                        "¿Deseas registrar este gasto de todos modos?");
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                    return; // Abortar inserción
                }
            } else if (report.getStatus() == BudgetStatus.WARNING_80) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Advertencia de Presupuesto");
                alert.setHeaderText("Consumo cercano al límite (>=80%)");
                alert.setContentText("Al guardar este gasto, habrás consumido el " + String.format("%.1f", report.getPorcentajeConsumido()) + "% de tu presupuesto en la categoría '" + categoria + "'.");
                alert.showAndWait();
            }

            if (selectedGasto == null) {
                // Crear
                Gasto nuevoGasto = new Gasto(fecha, descripcion, categoria, valor, observacion);
                gastoDAO.insert(nuevoGasto);
            } else {
                // Editar
                selectedGasto.setFecha(fecha);
                selectedGasto.setDescripcion(descripcion);
                selectedGasto.setCategoria(categoria);
                selectedGasto.setValor(valor);
                selectedGasto.setObservacion(observacion);
                gastoDAO.update(selectedGasto);
            }

            loadGastosData();
            handleLimpiar(null);

        } catch (SQLException e) {
            showErrorAlert("Error al guardar el gasto", e.getMessage());
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        if (selectedGasto == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este gasto?");
        alert.setContentText("Descripción: " + selectedGasto.getDescripcion() + "\nValor: " + currencyFormat.format(selectedGasto.getValor()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gastoDAO.delete(selectedGasto.getId());
                loadGastosData();
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
        selectedGasto = null;
        btnEliminar.setVisible(false);
        tblGastos.getSelectionModel().clearSelection();
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String search = txtBuscar.getText();
        String categoria = cbFiltroCategoria.getValue();
        LocalDate desde = dpFiltroDesde.getValue();
        LocalDate hasta = dpFiltroHasta.getValue();

        try {
            List<Gasto> filtered = gastoDAO.findByFilters(desde, hasta, categoria, search);
            gastosList.setAll(filtered);
            tblGastos.setItems(gastosList);
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
        loadGastosData();
    }

    private void populateForm(Gasto gasto) {
        dpFecha.setValue(gasto.getFecha());
        txtDescripcion.setText(gasto.getDescripcion());
        cbCategoria.setValue(gasto.getCategoria());
        txtValor.setText(String.valueOf(gasto.getValor()));
        txtObservacion.setText(gasto.getObservacion() != null ? gasto.getObservacion() : "");
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
            double valor = Double.parseDouble(txtValor.getText().trim());
            if (valor < 0) {
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
