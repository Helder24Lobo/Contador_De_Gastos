package com.personalfinance.contador.model;

public class GastoFijo {
    private int id;
    private String name;
    private double amount;
    private int billingDay;
    private String status; // 'Activo' or 'Inactivo'

    public GastoFijo() {
    }

    public GastoFijo(int id, String name, double amount, int billingDay, String status) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.billingDay = billingDay;
        this.status = status;
    }

    public GastoFijo(String name, double amount, int billingDay, String status) {
        this.name = name;
        this.amount = amount;
        this.billingDay = billingDay;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return name;
    }

    public void setNombre(String name) {
        this.name = name;
    }

    public double getValor() {
        return amount;
    }

    public void setValor(double amount) {
        this.amount = amount;
    }

    public int getDiaCobro() {
        return billingDay;
    }

    public void setDiaCobro(int billingDay) {
        this.billingDay = billingDay;
    }

    public String getEstado() {
        return status;
    }

    public void setEstado(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "GastoFijo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", billingDay=" + billingDay +
                ", status='" + status + '\'' +
                '}';
    }
}
