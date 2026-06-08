package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Expenditure;
import com.personalfinance.contador.model.Income;
import com.personalfinance.contador.model.Specifications;
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

    @FXML
    private Label lblIngresos;
    @FXML
    private Label lblGastos;
    @FXML
    private Label lblGastosFijos;
    @FXML
    private Label lblBalance;
    @FXML
    private Label lblMovimientosCount;
    @FXML
    private VBox vboxAlertas;
    @FXML
    private PieChart chartGastos;

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

            // 1. Get financial values
            double totalIncomes = ingresoDAO.getTotalIngresado(startOfMonth, endOfMonth);
            double totalExpenses = gastoDAO.getTotalGastado(startOfMonth, endOfMonth);
            double totalFixed = gastoFijoDAO.getTotalGastosFijosActivos();
            double netBalance = totalIncomes - totalExpenses - totalFixed;

            lblIngresos.setText(currencyFormat.format(totalIncomes));
            lblGastos.setText(currencyFormat.format(totalExpenses));
            lblGastosFijos.setText(currencyFormat.format(totalFixed));
            lblBalance.setText(currencyFormat.format(netBalance));

            // Color the balance if negative
            if (netBalance < 0) {
                lblBalance.getStyleClass().removeAll("value-balance");
                lblBalance.setStyle("-fx-text-fill: #e53e3e;"); // Light/dark red
            } else {
                lblBalance.getStyleClass().add("value-balance");
                lblBalance.setStyle("");
            }

            // 2. Number of transactions
            List<Income> incomeList = ingresoDAO.findByFilters(startOfMonth, endOfMonth, null, null);
            List<Expenditure> expenseList = gastoDAO.findByFilters(startOfMonth, endOfMonth, null, null);
            int totalTransactions = incomeList.size() + expenseList.size();
            lblMovimientosCount.setText(String.valueOf(totalTransactions));

            // 3. Load Expense by Category Chart
            loadPieChart(startOfMonth, endOfMonth);

            // 4. Load Budget Alerts
            loadBudgetAlerts();

        } catch (SQLException e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPieChart(LocalDate start, LocalDate end) throws SQLException {
        Map<String, Double> expensesMap = gastoDAO.getGastosGroupedByCategoria(start, end);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (expensesMap.isEmpty()) {
            chartGastos.setTitle("Sin gastos registrados este mes");
            chartGastos.setData(pieChartData);
            return;
        }

        chartGastos.setTitle("Distribución de Gastos");
        for (Map.Entry<String, Double> entry : expensesMap.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + currencyFormat.format(entry.getValue()) + ")", entry.getValue()));
        }
        chartGastos.setData(pieChartData);
    }

    private void loadBudgetAlerts() throws SQLException {
        vboxAlertas.getChildren().clear();
        List<Specifications> budgets = presupuestoDAO.findAll();

        boolean hasAlerts = false;

        for (Specifications budget : budgets) {
            BudgetReport report = budgetService.getCategoryConsumption(budget.getCategoria());
            if (report.getStatus() == BudgetStatus.CRITICAL_100) {
                hasAlerts = true;
                createAlertNode("CRÍTICO", "Has superado el 100% del presupuesto para '" + budget.getCategoria() +
                        "'. Consumo: " + String.format("%.1f", report.getPorcentajeConsumido()) + "% (" +
                        currencyFormat.format(report.getTotalGastado()) + " de " + currencyFormat.format(report.getPresupuestoDefinido()) + ")", "budget-alert-critical");
            } else if (report.getStatus() == BudgetStatus.WARNING_80) {
                hasAlerts = true;
                createAlertNode("ADVERTENCIA", "Has consumido más del 80% del presupuesto para '" + budget.getCategoria() +
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
