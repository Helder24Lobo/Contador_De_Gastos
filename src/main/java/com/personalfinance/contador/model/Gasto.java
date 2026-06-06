package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Gasto {
    private int id;
    private LocalDate fecha;
    private String descripcion;
    private String categoria;
    private double valor;
    private String observacion;

    public Gasto() {
    }

    public Gasto(int id, LocalDate fecha, String descripcion, String categoria, double valor, String observacion) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.valor = valor;
        this.observacion = observacion;
    }

    public Gasto(LocalDate fecha, String descripcion, String categoria, double valor, String observacion) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.valor = valor;
        this.observacion = observacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    @Override
    public String toString() {
        return "Gasto{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                ", categoria='" + categoria + '\'' +
                ", valor=" + valor +
                ", observacion='" + observacion + '\'' +
                '}';
    }
}
