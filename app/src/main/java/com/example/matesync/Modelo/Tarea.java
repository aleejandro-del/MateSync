package com.example.matesync.Modelo;

public class Tarea {

    private String tareaID;
    private String userID;
    private String grupoID;
    private String nombre;
    private String descripcion;
    private boolean isDone;

    public Tarea(String tareaID, String userID, String grupoID, String nombre, String descripcion, boolean isDone) {
        this.tareaID = tareaID;
        this.userID = userID;
        this.grupoID = grupoID;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.isDone = isDone;
    }
    public Tarea( String userID, String grupoID, String nombre, String descripcion, boolean isDone) {

        this.userID = userID;
        this.grupoID = grupoID;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.isDone = isDone;
    }
    public String getTareaID() {
        return tareaID;
    }

    public void setTareaID(String tareaID) {
        this.tareaID = tareaID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getGrupoID() {
        return grupoID;
    }

    public void setGrupoID(String grupoID) {
        this.grupoID = grupoID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
