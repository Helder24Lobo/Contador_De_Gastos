package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Gasto;
import com.personalfinance.contador.model.Ingreso;
import com.personalfinance.contador.model.Presupuesto;
import com.personalfinance.contador.repository.GastoDAO;
import com.personalfinance.contador.repository.GastoFijoDAO;
import com.personalfinance.contador.repository.IngresoDAO;
import com.personalfinance.contador.repository.PresupuestoDAO;
import com.personalfinance.contador.service.BudgetService;
import com.personalfinance.contador.service.BudgetService.BudgetReport;
import com.personalfinance.contador.service.BudgetService.BudgetStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblIngresos;
    @FXML private Label lblGastos;
    @FXML private Label lblGastosFijos;
    @FXML private Label lblBalance;
    @FXML private Label lblMovimientosCount;
    @FXML private VBox vboxAlertas;
    @FXML private PieChart chartGastos;

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final GastoDAO gastoDAO = new GastoDAO();
    private final GastoFijoDAO gastoFijoDAO = new GastoFijoDAO();
    private final PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
    private final BudgetService budgetService = new BudgetService();

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

            // 1. Obtener valores financieros
            double totalIngresos = ingresoDAO.getTotalIngresado(startOfMonth, endOfMonth);
            double totalGastos = gastoDAO.getTotalGastado(startOfMonth, endOfMonth);
            double totalFijos = gastoFijoDAO.getTotalGastosFijosActivos();
            double balanceNeto = totalIngresos - totalGastos - totalFijos;

            lblIngresos.setText(currencyFormat.format(totalIngresos));
            lblGastos.setText(currencyFormat.format(totalGastos));
            lblGastosFijos.setText(currencyFormat.format(totalFijos));
            lblBalance.setText(currencyFormat.format(balanceNeto));

            // Colorear el balance si es negativo
            if (balanceNeto < 0) {
                lblBalance.getStyleClass().removeAll("value-balance");
                lblBalance.setStyle("-fx-text-fill: #e53e3e;"); // Rojo claro/oscuro
            } else {
                lblBalance.getStyleClass().add("value-balance");
                lblBalance.setStyle("");
            }

            // 2. Cantidad de movimientos
            List<Ingreso> listaIngresos = ingresoDAO.findByFilters(startOfMonth, endOfMonth, null, null);
            List<Gasto> listaGastos = gastoDAO.findByFilters(startOfMonth, endOfMonth, null, null);
            int totalMovimientos = listaIngresos.size() + listaGastos.size();
            lblMovimientosCount.setText(String.valueOf(totalMovimientos));

            // 3. Cargar Gráfico de Gastos por Categoría
            loadPieChart(startOfMonth, endOfMonth);

            // 4. Cargar Alertas de Presupuestos
            loadBudgetAlerts();

        } catch (SQLException e) {
            System.err.println("Error al cargar los datos del Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPieChart(LocalDate start, LocalDate end) throws SQLException {
        Map<String, Double> mapGastos = gastoDAO.getGastosGroupedByCategoria(start, end);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (mapGastos.isEmpty()) {
            chartGastos.setTitle("Sin gastos registrados este mes");
            chartGastos.setData(pieChartData);
            return;
        }

        chartGastos.setTitle("Distribución de Gastos");
        for (Map.Entry<String, Double> entry : mapGastos.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + currencyFormat.format(entry.getValue()) + ")", entry.getValue()));
        }
        chartGastos.setData(pieChartData);
    }

    private void loadBudgetAlerts() throws SQLException {
        vboxAlertas.getChildren().clear();
        List<Presupuesto> presupuestos = presupuestoDAO.findAll();
        
        boolean hasAlerts = false;

        for (Presupuesto pres : presupuestos) {
            BudgetReport report = budgetService.getCategoryConsumption(pres.getCategoria());
            if (report.getStatus() == BudgetStatus.CRITICAL_100) {
                hasAlerts = true;
                createAlertNode("CRÍTICO", "Has superado el 100% del presupuesto para '" + pres.getCategoria() + 
                        "'. Consumo: " + String.format("%.1f", report.getPorcentajeConsumido()) + "% (" +
                        currencyFormat.format(report.getTotalGastado()) + " de " + currencyFormat.format(report.getPresupuestoDefinido()) + ")", "budget-alert-critical");
            } else if (report.getStatus() == BudgetStatus.WARNING_80) {
                hasAlerts = true;
                createAlertNode("ADVERTENCIA", "Has consumido más del 80% del presupuesto para '" + pres.getCategoria() + 
                        "'. Consumo: " + String.format("%.1f", report.getPorcentajeConsumido()) + "% (" +
                        currencyFormat.format(report.getTotalGastado()) + " de " + currencyFormat.format(report.getPresupuestoDefinido()) + ")", "budget-alert-warning");
            }
        }

        if (!hasAlerts) {
            Label noAlerts = new Label("Todos tus presupuestos están dentro de los límites saludables.");
            noAlerts.setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold; -fx-font-size: 13px;");
            vboxAlertas.getChildren().add(noAlerts);
        }
    }

    private void createAlertNode(String level, String message, String cssClass) {
        VBox alertBox = new VBox();
        alertBox.getStyleClass().add(cssClass);
        alertBox.setSpacing(5);

        Label lblTitle = new Label(level + ": Presupuesto Excedido");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label lblMsg = new Label(message);
        lblMsg.setWrapText(true);

        alertBox.getChildren().addAll(lblTitle, lblMsg);
        vboxAlertas.getChildren().add(alertBox);
    }
}
