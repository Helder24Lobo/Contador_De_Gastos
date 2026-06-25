package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Savings {

    private int id;
    private LocalDate dateCurrent;
    private String description;
    private double amount;
    private String priority; //Alta, media, baja.

    public Savings() {
    }

    public Savings(int id, LocalDate dateCurrent, String description, String priority, double amount) {
        this.id = id;
        this.dateCurrent = dateCurrent;
        this.description = description;
        this.amount = amount;
        this.priority = priority;
    }

    public Savings(LocalDate dateCurrent, String description, String priority, double amount) {
        this.dateCurrent = dateCurrent;
        this.description = description;
        this.amount = amount;
        this.priority = priority;
    }


    //GET AND SET
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateCurrent() {
        return dateCurrent;
    }

    public void setDateCurrent(LocalDate dateCurrent) {
        this.dateCurrent = dateCurrent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Savings{" +
                "id=" + id +
                ", date=" + dateCurrent +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", priority='" + priority + '\'' +
                '}';
    }
}
