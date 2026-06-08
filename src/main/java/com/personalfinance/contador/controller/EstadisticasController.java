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
        String selection = cbRango.getValue();
        LocalDate today = LocalDate.now();

        if (selection == null) return;

        switch (selection) {
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
        LocalDate from = dpDesde.getValue();
        LocalDate to = dpHasta.getValue();

        if (from == null || to == null || from.isAfter(to)) {
            return; // Avoid queries if the range is invalid
        }

        try {
            // 1. Pie Chart (Categories)
            loadCategoryPieChart(from, to);

            // 2. Bar Chart (Income vs Expenses)
            loadComparativeBarChart(from, to);

            // 3. Line Chart (Balance Evolution)
            loadBalanceLineChart(from, to);

        } catch (SQLException e) {
            System.err.println("Error loading statistics charts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCategoryPieChart(LocalDate from, LocalDate to) throws SQLException {
        Map<String, Double> map = gastoDAO.getGastosGroupedByCategoria(from, to);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        chartCategorias.setData(pieData);
    }

    private void loadComparativeBarChart(LocalDate from, LocalDate to) throws SQLException {
        double totalIncomes = ingresoDAO.getTotalIngresado(from, to);
        double totalDailyExpenses = gastoDAO.getTotalGastado(from, to);
        double totalFixed = gastoFijoDAO.getTotalGastosFijosActivos();

        chartComparativa.getData().clear();

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Ingresos");
        incomeSeries.getData().add(new XYChart.Data<>("Ingresos", totalIncomes));

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Gastos");
        expenseSeries.getData().add(new XYChart.Data<>("Gastos Diarios", totalDailyExpenses));
        expenseSeries.getData().add(new XYChart.Data<>("Gastos Fijos", totalFixed));

        chartComparativa.getData().addAll(incomeSeries, expenseSeries);
    }

    private void loadBalanceLineChart(LocalDate from, LocalDate to) throws SQLException {
        chartEvolucion.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Balance Neto Acumulado");

        long daysDiff = ChronoUnit.DAYS.between(from, to);

        if (daysDiff <= 31) {
            // Plot day by day
            LocalDate current = from;
            double accumulatedBalance = 0.0;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

            while (!current.isAfter(to)) {
                double dayIncome = ingresoDAO.getTotalIngresado(current, current);
                double dayExpense = gastoDAO.getTotalGastado(current, current);

                // Approximate daily distribution of fixed expenses in the month (to smooth the chart)
                double dailyFixed = 0.0;
                if (current.getDayOfMonth() == 1) {
                    // Load fixed expenses on the first day of the month as a simplification
                    dailyFixed = gastoFijoDAO.getTotalGastosFijosActivos();
                }

                accumulatedBalance += (dayIncome - dayExpense - dailyFixed);

                series.getData().add(new XYChart.Data<>(current.format(fmt), accumulatedBalance));
                current = current.plusDays(1);
            }
        } else {
            // Plot month by month
            LocalDate current = from.withDayOfMonth(1);
            double accumulatedBalance = 0.0;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");

            while (!current.isAfter(to)) {
                LocalDate start = current.withDayOfMonth(1);
                LocalDate end = current.with(TemporalAdjusters.lastDayOfMonth());

                double monthIncome = ingresoDAO.getTotalIngresado(start, end);
                double monthExpense = gastoDAO.getTotalGastado(start, end);
                double monthFixed = gastoFijoDAO.getTotalGastosFijosActivos();

                accumulatedBalance += (monthIncome - monthExpense - monthFixed);

                series.getData().add(new XYChart.Data<>(current.format(fmt), accumulatedBalance));
                current = current.plusMonths(1);
            }
        }

        chartEvolucion.getData().add(series);
    }
}
