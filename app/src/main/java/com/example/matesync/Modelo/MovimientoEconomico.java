package com.example.matesync.Modelo;

import android.graphics.Movie;

public class MovimientoEconomico {
    private String concepto;
    private String tipo;
    private String userID;
    private String grupoID;
    private float valor;
    private String movID;

    public MovimientoEconomico() {}

    public MovimientoEconomico(String userID, String grupoID, String concepto, String tipo, float valor) {
        this.grupoID = grupoID;
        this.concepto = concepto;
        this.tipo = tipo;
        this.userID = userID;
        this.valor = valor;
    }

    public MovimientoEconomico(String movID, String grupoID, String userID, String concepto, String tipo, float valor) {
        this.grupoID = grupoID;
        this.concepto = concepto;
        this.tipo = tipo;
        this.userID = userID;
        this.valor = valor;
        this.movID = movID;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public double getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public String getMovID() {
        return movID;
    }

    public void setMovID(String movID) {
        this.movID = movID;
    }
}
