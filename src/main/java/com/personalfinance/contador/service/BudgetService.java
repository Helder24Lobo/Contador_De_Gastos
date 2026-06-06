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
        WARNING_80, // Supera el 80% pero menor al 100%
        CRITICAL_100 // Supera el 100%
    }

    public static class BudgetReport {
        private final BudgetStatus status;
        private final double presupuestoDefinido;
        private final double totalGastado;
        private final double porcentajeConsumido;

        public BudgetReport(BudgetStatus status, double presupuestoDefinido, double totalGastado, double porcentajeConsumido) {
            this.status = status;
            this.presupuestoDefinido = presupuestoDefinido;
            this.totalGastado = totalGastado;
            this.porcentajeConsumido = porcentajeConsumido;
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
     * Valida el estado de presupuesto para una categoría dada considerando un nuevo gasto que se desea registrar.
     */
    public BudgetReport checkNewExpense(String categoria, double nuevoValorGasto) throws SQLException {
        Presupuesto presupuesto = presupuestoDAO.findByCategoria(categoria);
        if (presupuesto == null) {
            return new BudgetReport(BudgetStatus.OK, 0.0, 0.0, 0.0);
        }

        // Obtener rango del mes actual
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        // Calcular el total gastado en este mes para esa categoría
        double totalGastadoActual = gastoDAO.findByFilters(startOfMonth, endOfMonth, categoria, null)
                .stream()
                .mapToDouble(g -> g.getValor())
                .sum();

        double totalConNuevoGasto = totalGastadoActual + nuevoValorGasto;
        double valorPresupuestado = presupuesto.getValorPresupuestado();
        
        if (valorPresupuestado <= 0) {
            return new BudgetReport(BudgetStatus.OK, 0.0, totalConNuevoGasto, 0.0);
        }

        double porcentaje = (totalConNuevoGasto / valorPresupuestado) * 100;
        BudgetStatus status = BudgetStatus.OK;

        if (porcentaje >= 100) {
            status = BudgetStatus.CRITICAL_100;
        } else if (porcentaje >= 80) {
            status = BudgetStatus.WARNING_80;
        }

        return new BudgetReport(status, valorPresupuestado, totalGastadoActual, porcentaje);
    }

    /**
     * Valida el consumo del presupuesto de este mes para una categoría sin agregar nuevos gastos.
     */
    public BudgetReport getCategoryConsumption(String categoria) throws SQLException {
        return checkNewExpense(categoria, 0.0);
    }
}
