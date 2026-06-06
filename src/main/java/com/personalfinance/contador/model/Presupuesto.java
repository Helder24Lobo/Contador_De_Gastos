package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Presupuesto {
    private int id;
    private String categoria;
    private double valorPresupuestado;
    private LocalDate fechaCreacion;

    public Presupuesto() {
    }

    public Presupuesto(int id, String categoria, double valorPresupuestado, LocalDate fechaCreacion) {
        this.id = id;
        this.categoria = categoria;
        this.valorPresupuestado = valorPresupuestado;
        this.fechaCreacion = fechaCreacion;
    }

    public Presupuesto(String categoria, double valorPresupuestado, LocalDate fechaCreacion) {
        this.categoria = categoria;
        this.valorPresupuestado = valorPresupuestado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getValorPresupuestado() {
        return valorPresupuestado;
    }

    public void setValorPresupuestado(double valorPresupuestado) {
        this.valorPresupuestado = valorPresupuestado;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return "Presupuesto{" +
                "id=" + id +
                ", categoria='" + categoria + '\'' +
                ", valorPresupuestado=" + valorPresupuestado +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
