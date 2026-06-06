package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Gasto;
import com.personalfinance.contador.model.Ingreso;
import com.personalfinance.contador.repository.GastoDAO;
import com.personalfinance.contador.repository.GastoFijoDAO;
import com.personalfinance.contador.repository.IngresoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class EstadisticasController implements Initializable {

    @FXML private ComboBox<String> cbRango;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML private PieChart chartCategorias;
    @FXML private BarChart<String, Number> chartComparativa;
    @FXML private LineChart<String, Number> chartEvolucion;

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final GastoDAO gastoDAO = new GastoDAO();
    private final GastoFijoDAO gastoFijoDAO = new GastoFijoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRango.setItems(FXCollections.observableArrayList("Este Mes", "Este Año", "Personalizado"));
        cbRango.setValue("Este Mes");
        handleRangoChange(null);
    }

    @FXML
    private void handleRangoChange(ActionEvent event) {
        String seleccion = cbRango.getValue();
        LocalDate today = LocalDate.now();

        if (seleccion == null) return;

        switch (seleccion) {
            case "Este Mes":
                dpDesde.setValue(today.withDayOfMonth(1));
                dpHasta.setValue(today.with(TemporalAdjusters.lastDayOfMonth()));
                setDatesEditable(false);
                break;
            case "Este Año":
                dpDesde.setValue(today.withDayOfYear(1));
                dpHasta.setValue(today.with(TemporalAdjusters.lastDayOfYear()));
                setDatesEditable(false);
                break;
            case "Personalizado":
                setDatesEditable(true);
                break;
        }
        cargarGraficas(null);
    }

    private void setDatesEditable(boolean editable) {
        dpDesde.setDisable(!editable);
        dpHasta.setDisable(!editable);
    }

    @FXML
    private void cargarGraficas(ActionEvent event) {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            return; // Evitar consultas si el rango es inválido
        }

        try {
            // 1. Gráfica de Pastel (Categorías)
            cargarPastelCategorias(desde, hasta);

            // 2. Gráfica de Barras (Ingresos vs Gastos)
            cargarBarrasComparativa(desde, hasta);

            // 3. Gráfica de Líneas (Evolución de Balance)
            cargarLineasEvolucion(desde, hasta);

        } catch (SQLException e) {
            System.err.println("Error al cargar gráficas estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarPastelCategorias(LocalDate desde, LocalDate hasta) throws SQLException {
        Map<String, Double> map = gastoDAO.getGastosGroupedByCategoria(desde, hasta);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        chartCategorias.setData(pieData);
    }

    private void cargarBarrasComparativa(LocalDate desde, LocalDate hasta) throws SQLException {
        double totalIngresos = ingresoDAO.getTotalIngresado(desde, hasta);
        double totalGastosDiarios = gastoDAO.getTotalGastado(desde, hasta);
        double totalFijos = gastoFijoDAO.getTotalGastosFijosActivos();

        chartComparativa.getData().clear();

        XYChart.Series<String, Number> seriesIngresos = new XYChart.Series<>();
        seriesIngresos.setName("Ingresos");
        seriesIngresos.getData().add(new XYChart.Data<>("Ingresos", totalIngresos));

        XYChart.Series<String, Number> seriesGastos = new XYChart.Series<>();
        seriesGastos.setName("Gastos");
        seriesGastos.getData().add(new XYChart.Data<>("Gastos Diarios", totalGastosDiarios));
        seriesGastos.getData().add(new XYChart.Data<>("Gastos Fijos", totalFijos));

        chartComparativa.getData().addAll(seriesIngresos, seriesGastos);
    }

    private void cargarLineasEvolucion(LocalDate desde, LocalDate hasta) throws SQLException {
        chartEvolucion.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Balance Neto Acumulado");

        long diasDiff = ChronoUnit.DAYS.between(desde, hasta);

        if (diasDiff <= 31) {
            // Graficar día a día
            LocalDate actual = desde;
            double balanceAcumulado = 0.0;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

            while (!actual.isAfter(hasta)) {
                double ingDia = ingresoDAO.getTotalIngresado(actual, actual);
                double gastDia = gastoDAO.getTotalGastado(actual, actual);
                
                // Distribución aproximada de gastos fijos diarios en el mes (para suavizar el gráfico)
                double fijosDia = 0.0;
                if (actual.getDayOfMonth() == 1) {
                    // Cargar gastos fijos en el primer día del mes como simplificación
                    fijosDia = gastoFijoDAO.getTotalGastosFijosActivos();
                }

                balanceAcumulado += (ingDia - gastDia - fijosDia);

                series.getData().add(new XYChart.Data<>(actual.format(fmt), balanceAcumulado));
                actual = actual.plusDays(1);
            }
        } else {
            // Graficar mes a mes
            LocalDate actual = desde.withDayOfMonth(1);
            double balanceAcumulado = 0.0;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");

            while (!actual.isAfter(hasta)) {
                LocalDate start = actual.withDayOfMonth(1);
                LocalDate end = actual.with(TemporalAdjusters.lastDayOfMonth());

                double ingMes = ingresoDAO.getTotalIngresado(start, end);
                double gastMes = gastoDAO.getTotalGastado(start, end);
                double fijosMes = gastoFijoDAO.getTotalGastosFijosActivos();

                balanceAcumulado += (ingMes - gastMes - fijosMes);

                series.getData().add(new XYChart.Data<>(actual.format(fmt), balanceAcumulado));
                actual = actual.plusMonths(1);
            }
        }

        chartEvolucion.getData().add(series);
    }
}
