package com.personalfinance.contador.service;

import com.personalfinance.contador.model.Presupuesto;
import com.personalfinance.contador.repository.GastoDAO;
import com.personalfinance.contador.repository.PresupuestoDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class BudgetService {

    private final GastoDAO gastoDAO = new GastoDAO();
    private final PresupuestoDAO presupuestoDAO = new PresupuestoDAO();

    public enum BudgetStatus {
        OK,
        WARNING_80, // Exceeds 80% but less than 100%
        CRITICAL_100 // Exceeds 100%
    }

    public static class BudgetReport {
        private final BudgetStatus status;
        private final double presupuestoDefinido;
        private final double totalGastado;
        private final double porcentajeConsumido;

        public BudgetReport(BudgetStatus status, double budgetDefined, double totalSpent, double percentageConsumed) {
            this.status = status;
            this.presupuestoDefinido = budgetDefined;
            this.totalGastado = totalSpent;
            this.porcentajeConsumido = percentageConsumed;
        }

        public BudgetStatus getStatus() {
            return status;
        }

        public double getPresupuestoDefinido() {
            return presupuestoDefinido;
        }

        public double getTotalGastado() {
            return totalGastado;
        }

        public double getPorcentajeConsumido() {
            return porcentajeConsumido;
        }
    }

    /**
     * Validates the budget status for a given category considering a new expense to be recorded.
     */
    public BudgetReport checkNewExpense(String category, double newExpenseAmount) throws SQLException {
        Presupuesto budget = presupuestoDAO.findByCategoria(category);
        if (budget == null) {
            return new BudgetReport(BudgetStatus.OK, 0.0, 0.0, 0.0);
        }

        // Get the current month range
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        // Calculate the total spent this month for that category
        double currentTotalSpent = gastoDAO.findByFilters(startOfMonth, endOfMonth, category, null)
                .stream()
                .mapToDouble(g -> g.getValor())
                .sum();

        double totalWithNewExpense = currentTotalSpent + newExpenseAmount;
        double budgetedAmount = budget.getValorPresupuestado();

        if (budgetedAmount <= 0) {
            return new BudgetReport(BudgetStatus.OK, 0.0, totalWithNewExpense, 0.0);
        }

        double percentage = (totalWithNewExpense / budgetedAmount) * 100;
        BudgetStatus status = BudgetStatus.OK;

        if (percentage >= 100) {
            status = BudgetStatus.CRITICAL_100;
        } else if (percentage >= 80) {
            status = BudgetStatus.WARNING_80;
        }

        return new BudgetReport(status, budgetedAmount, currentTotalSpent, percentage);
    }

    /**
     * Validates the budget consumption for the current month for a category without adding new expenses.
     */
    public BudgetReport getCategoryConsumption(String category) throws SQLException {
        return checkNewExpense(category, 0.0);
    }
}
