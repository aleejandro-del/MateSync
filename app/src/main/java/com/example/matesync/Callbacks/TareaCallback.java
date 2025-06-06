package com.example.matesync.Callbacks;

import com.example.matesync.Modelo.Tarea;

import java.util.List;

public interface TareaCallback {
    void onSuccessRecoveringTareas(List<Tarea> listaTareas);
    void onSuccessRegisteringTarea();
    void onSuccessRemovingTarea();
    void onFailure(Exception e);
}