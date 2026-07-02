package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Expenditure;
import com.personalfinance.contador.model.FixedExpense;
import com.personalfinance.contador.model.Income;
import com.personalfinance.contador.repository.GastoDAO;
import com.personalfinance.contador.repository.GastoFijoDAO;
import com.personalfinance.contador.repository.IngresoDAO;
import com.personalfinance.contador.service.ExcelExportService;
import com.personalfinance.contador.service.PdfReportService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML
    private ComboBox<String> cbRangoRapido;
    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final GastoDAO gastoDAO = new GastoDAO();
    private final GastoFijoDAO gastoFijoDAO = new GastoFijoDAO();

    private final String[] rangosRapidos = {
            "Diario", "Semanal", "Mensual", "Anual", "Personalizado"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRangoRapido.setItems(FXCollections.observableArrayList(rangosRapidos));
        cbRangoRapido.setValue("Mensual");
        handleRangoRapido(null);
    }

    @FXML
    private void handleRangoRapido(ActionEvent event) {
        String seleccion = cbRangoRapido.getValue();
        LocalDate today = LocalDate.now();

        if (seleccion == null) return;

        switch (seleccion) {
            case "Diario":
                dpDesde.setValue(today);
                dpHasta.setValue(today);
                setDatesEditable(false);
                break;
            case "Semanal":
                dpDesde.setValue(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
                dpHasta.setValue(today);
                setDatesEditable(false);
                break;
            case "Mensual":
                dpDesde.setValue(today.withDayOfMonth(1));
                dpHasta.setValue(today.with(TemporalAdjusters.lastDayOfMonth()));
                setDatesEditable(false);
                break;
            case "Anual":
                dpDesde.setValue(today.withDayOfYear(1));
                dpHasta.setValue(today.with(TemporalAdjusters.lastDayOfYear()));
                setDatesEditable(false);
                break;
            case "Personalizado":
                setDatesEditable(true);
                break;
        }
    }

    private void setDatesEditable(boolean editable) {
        dpDesde.setDisable(!editable);
        dpHasta.setDisable(!editable);
    }

    @FXML
    private void exportarExcel(ActionEvent event) {
        if (!validateDates()) return;

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("reporte_financiero_" + desde + "_a_" + hasta + ".xlsx");

        File file = fileChooser.showSaveDialog(dpDesde.getScene().getWindow());
        if (file != null) {
            try {
                List<Income> incomes = ingresoDAO.findByFilters(desde, hasta, null, null);
                List<Expenditure> expenditures = gastoDAO.findByFilters(desde, hasta, null, null);
                List<FixedExpense> fijos = gastoFijoDAO.findAll();

                ExcelExportService.exportToExcel(file.getAbsolutePath(), desde, hasta, incomes, expenditures, fijos);
                showSuccessAlert("Exportación Completa", "El reporte en Excel se ha guardado correctamente en:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showErrorAlert("Error al exportar a Excel", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void exportarPDF(ActionEvent event) {
        if (!validateDates()) return;

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documentos PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("reporte_financiero_" + desde + "_a_" + hasta + ".pdf");

        File file = fileChooser.showSaveDialog(dpDesde.getScene().getWindow());
        if (file != null) {
            try {
                List<Income> incomes = ingresoDAO.findByFilters(desde, hasta, null, null);
                List<Expenditure> expenditures = gastoDAO.findByFilters(desde, hasta, null, null);
                List<FixedExpense> fijos = gastoFijoDAO.findAll();
                String tituloReporte = "Reporte Financiero (" + cbRangoRapido.getValue() + ")";
                PdfReportService.generateFinancialReport(file.getAbsolutePath(), tituloReporte, desde, hasta, incomes, expenditures, fijos);

                showSuccessAlert("Exportación Completa", "El reporte en PDF se ha guardado correctamente en:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showErrorAlert("Error al exportar a PDF", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateDates() {
        if (dpDesde.getValue() == null || dpHasta.getValue() == null) {
            showWarningAlert("Fechas Inválidas", "Por favor selecciona la fecha de inicio y fin para el reporte.");
            return false;
        }
        if (dpDesde.getValue().isAfter(dpHasta.getValue())) {
            showWarningAlert("Fechas Inválidas", "La fecha de inicio no puede ser posterior a la fecha final.");
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
