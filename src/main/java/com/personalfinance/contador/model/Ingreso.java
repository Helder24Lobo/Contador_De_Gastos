package com.personalfinance.contador.model;

import java.time.LocalDate;

public class Ingreso {
    private int id;
    private LocalDate fecha;
    private String descripcion;
    private double valor;
    private String tipo; // Salario, Bonificación, Venta, Freelance, Otros

    public Ingreso() {
    }

    public Ingreso(int id, LocalDate fecha, String descripcion, double valor, String tipo) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.valor = valor;
        this.tipo = tipo;
    }

    public Ingreso(LocalDate fecha, String descripcion, double valor, String tipo) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.valor = valor;
        this.tipo = tipo;
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

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Ingreso{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                ", valor=" + valor +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}
