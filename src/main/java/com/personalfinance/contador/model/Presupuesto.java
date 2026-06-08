package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Presupuesto {
    private int id;
    private String category;
    private double budgetedAmount;
    private LocalDate creationDate;

    public Presupuesto() {
    }

    public Presupuesto(int id, String category, double budgetedAmount, LocalDate creationDate) {
        this.id = id;
        this.category = category;
        this.budgetedAmount = budgetedAmount;
        this.creationDate = creationDate;
    }

    public Presupuesto(String category, double budgetedAmount, LocalDate creationDate) {
        this.category = category;
        this.budgetedAmount = budgetedAmount;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoria() {
        return category;
    }

    public void setCategoria(String category) {
        this.category = category;
    }

    public double getValorPresupuestado() {
        return budgetedAmount;
    }

    public void setValorPresupuestado(double budgetedAmount) {
        this.budgetedAmount = budgetedAmount;
    }

    public LocalDate getFechaCreacion() {
        return creationDate;
    }

    public void setFechaCreacion(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Presupuesto{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", budgetedAmount=" + budgetedAmount +
                ", creationDate=" + creationDate +
                '}';
    }
}
