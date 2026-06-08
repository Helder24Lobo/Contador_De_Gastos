package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Gasto {
    private int id;
    private LocalDate date;
    private String description;
    private String category;
    private double amount;
    private String note;

    public Gasto() {
    }

    public Gasto(int id, LocalDate date, String description, String category, double amount, String note) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.note = note;
    }

    public Gasto(LocalDate date, String description, String category, double amount, String note) {
        this.date = date;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.note = note;
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

    public String getCategoria() {
        return category;
    }

    public void setCategoria(String category) {
        this.category = category;
    }

    public double getValor() {
        return amount;
    }

    public void setValor(double amount) {
        this.amount = amount;
    }

    public String getObservacion() {
        return note;
    }

    public void setObservacion(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Gasto{" +
                "id=" + id +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", note='" + note + '\'' +
                '}';
    }
}
