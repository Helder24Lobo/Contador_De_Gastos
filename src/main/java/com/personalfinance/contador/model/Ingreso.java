package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Ingreso {
    private int id;
    private LocalDate date;
    private String description;
    private double amount;
    private String type; // Salario, Bonificación, Venta, Freelance, Otros

    public Ingreso() {
    }

    public Ingreso(int id, LocalDate date, String description, double amount, String type) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public Ingreso(LocalDate date, String description, double amount, String type) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return date;
    }

    public void setFecha(LocalDate date) {
        this.date = date;
    }

    public String getDescripcion() {
        return description;
    }

    public void setDescripcion(String description) {
        this.description = description;
    }

    public double getValor() {
        return amount;
    }

    public void setValor(double amount) {
        this.amount = amount;
    }

    public String getTipo() {
        return type;
    }

    public void setTipo(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Ingreso{" +
                "id=" + id +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
}
