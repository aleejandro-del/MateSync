package com.example.matesync.Callbacks;

public interface CodInvitacionCallback {
    void onCodigoValido(String grupoId, String nombreGrupo);
    void onCodigoNoValido();
    void onError(Exception e);
}