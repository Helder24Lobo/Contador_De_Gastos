package com.personalfinance.contador.model;

public class GastoFijo {
    private int id;
    private String nombre;
    private double valor;
    private int diaCobro;
    private String estado; // 'Activo' o 'Inactivo'

    public GastoFijo() {
    }

    public GastoFijo(int id, String nombre, double valor, int diaCobro, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.valor = valor;
        this.diaCobro = diaCobro;
        this.estado = estado;
    }

    public GastoFijo(String nombre, double valor, int diaCobro, String estado) {
        this.nombre = nombre;
        this.valor = valor;
        this.diaCobro = diaCobro;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getDiaCobro() {
        return diaCobro;
    }

    public void setDiaCobro(int diaCobro) {
        this.diaCobro = diaCobro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "GastoFijo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", valor=" + valor +
                ", diaCobro=" + diaCobro +
                ", estado='" + estado + '\'' +
                '}';
    }
}
