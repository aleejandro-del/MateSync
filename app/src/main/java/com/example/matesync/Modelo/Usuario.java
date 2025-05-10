package com.example.matesync.Modelo;

public class Usuario {
    private String userID;
    private String nombre;
    private String grupoID;
    private boolean isAdmin;
    private String email;

    public Usuario(String userID, String nombre, String grupoID, boolean isAdmin, String email) {
        this.userID = userID;
        this.nombre = nombre;
        this.grupoID = grupoID;
        this.isAdmin = isAdmin;
        this.email = email;
    }

    public Usuario(String userID, String nombre, boolean isAdmin, String email) {
        this.userID = userID;
        this.nombre = nombre;
        this.isAdmin = isAdmin;
        this.email = email;
    }

    //constructor vacío para serializar el objeto en Firestore
    public Usuario() {
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

    public String getGrupoID() {
        return grupoID;
    }

    public void setGrupoID(String grupoID) {
        this.grupoID = grupoID;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
