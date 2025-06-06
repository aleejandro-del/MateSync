package com.example.matesync.Callbacks;

//interfaces de callback
public interface FirestoreUserCallback {
    void onCallback(String nombreUser, String email, boolean isAdmin, String grupoID, String nombreGrupo);
}

