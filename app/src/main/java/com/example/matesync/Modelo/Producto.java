package com.example.matesync.Modelo;

public class Producto {
    private String productoID;
    private String userID;
    private String nombre;
    private long cantidad;
    private String grupoID;
    private String descripcion;

    public Producto(String productoID, String userID, String grupoID, String nombre, String descripcion, long cantidad) {
        this.productoID = productoID;
        this.userID = userID;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.grupoID = grupoID;
        this.descripcion = descripcion;
    }
    public Producto(String userID, String grupoID, String nombre, String descripcion, long cantidad) {
        this.userID = userID;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.grupoID = grupoID;
        this.descripcion = descripcion;
    }
    public Producto(){}

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

    public long getCantidad() {
        return cantidad;
    }

    public void setCantidad(long cantidad) {
        this.cantidad = cantidad;
    }

    public String getGrupoID() {
        return grupoID;
    }

    public void setGrupoID(String grupoID) {
        this.grupoID = grupoID;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
