package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Savings {

    private int id;
    private String name;
    private String description;
    private double targetValue;
    private LocalDate dateCurrent; // Fecha de creación
    private String status; // Activo, Completado
    private double savedAmount; // Saldo total ahorrado calculado

    public Savings() {
    }

    public Savings(int id, String name, String description, double targetValue, LocalDate dateCurrent, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.targetValue = targetValue;
        this.dateCurrent = dateCurrent;
        this.status = status;
    }

    public Savings(String name, String description, double targetValue, LocalDate dateCurrent, String status) {
        this.name = name;
        this.description = description;
        this.targetValue = targetValue;
        this.dateCurrent = dateCurrent;
        this.status = status;
    }

    // GET AND SET
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public LocalDate getDateCurrent() {
        return dateCurrent;
    }

    public void setDateCurrent(LocalDate dateCurrent) {
        this.dateCurrent = dateCurrent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
    }

    // Métodos Calculados
    public double getRemainingAmount() {
        double diff = targetValue - savedAmount;
        return diff < 0 ? 0.0 : diff;
    }

    public double getProgressPercentage() {
        if (targetValue <= 0) {
            return 0.0;
        }
        double pct = (savedAmount / targetValue) * 100.0;
        return pct > 100.0 ? 100.0 : pct;
    }

    @Override
    public String toString() {
        return "Savings{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", targetValue=" + targetValue +
                ", dateCurrent=" + dateCurrent +
                ", status='" + status + '\'' +
                ", savedAmount=" + savedAmount +
                '}';
    }
}
