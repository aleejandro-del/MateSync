package com.example.matesync.Modelo;

import java.util.ArrayList;

public class GrupoDomestico {

    String grupoID;
    String nombreGrupo;
    String codigoInvitacion;
    ArrayList<Usuario> miembros;

    //constructor vacío para serializar el objeto en Firestore
    public GrupoDomestico(){};

    public GrupoDomestico(String grupoID, String nombreGrupo, String codigoInvitacion, ArrayList<Usuario> miembros) {
        this.grupoID = grupoID;
        this.nombreGrupo = nombreGrupo;
        this.codigoInvitacion = codigoInvitacion;
        this.miembros = miembros;
    }

    public String getGrupoID() {
        return grupoID;
    }

    public void setGrupoID(String grupoID) {
        this.grupoID = grupoID;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public String getCodigoInvitacion() {
        return codigoInvitacion;
    }

    public void setCodigoInvitacion(String codigoInvitacion) {
        this.codigoInvitacion = codigoInvitacion;
    }

    public ArrayList<Usuario> getMiembros() {
        return miembros;
    }

    public void setMiembros(ArrayList<Usuario> miembros) {
        this.miembros = miembros;
    }
}
