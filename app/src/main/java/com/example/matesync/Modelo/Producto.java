package com.example.matesync.Modelo;

public class Producto {
    private String productoID;
    private String userID;
    private String nombre;
    private int cantidad;

    public Producto(String productoID, String userID, String nombre, int cantidad) {
        this.productoID = productoID;
        this.userID = userID;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getProductoID() {
        return productoID;
    }

    public void setProductoID(String productoID) {
        this.productoID = productoID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
