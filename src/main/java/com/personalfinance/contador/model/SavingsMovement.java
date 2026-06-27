package com.personalfinance.contador.model;

import java.time.LocalDate;

public class SavingsMovement {

    private int id;
    private int savingId;
    private double amount;
    private LocalDate date;
    private String observation;

    public SavingsMovement() {
    }

    public SavingsMovement(int id, int savingId, double amount, LocalDate date, String observation) {
        this.id = id;
        this.savingId = savingId;
        this.amount = amount;
        this.date = date;
        this.observation = observation;
    }

    public SavingsMovement(int savingId, double amount, LocalDate date, String observation) {
        this.savingId = savingId;
        this.amount = amount;
        this.date = date;
        this.observation = observation;
    }

    // GET AND SET
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSavingId() {
        return savingId;
    }

    public void setSavingId(int savingId) {
        this.savingId = savingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    @Override
    public String toString() {
        return "SavingsMovement{" +
                "id=" + id +
                ", savingId=" + savingId +
                ", amount=" + amount +
                ", date=" + date +
                ", observation='" + observation + '\'' +
                '}';
    }
}
