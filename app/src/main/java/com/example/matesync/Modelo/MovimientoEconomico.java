package com.example.matesync.Modelo;

import android.graphics.Movie;

public class MovimientoEconomico {
    private String concepto;
    private String tipo;
    private String userID;
    private String grupoID;
    private double valor;

    public MovimientoEconomico() {}

    public MovimientoEconomico(String concepto, String tipo, String userID, double valor) {
        this.concepto = concepto;
        this.tipo = tipo;
        this.userID = userID;
        this.valor = valor;
    }
}
