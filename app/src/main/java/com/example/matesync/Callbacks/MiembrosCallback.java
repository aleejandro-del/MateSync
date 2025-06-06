package com.example.matesync.Callbacks;
import com.example.matesync.Modelo.Usuario;

import java.util.List;

public interface MiembrosCallback {
    void onRecoverMiembrosSuccess(List<Usuario> listaUsuarios);
    void onFailure(Exception exception);
}

