package com.example.matesync.Callbacks;
import com.example.matesync.Modelo.MovimientoEconomico;

import java.util.List;

public interface MovEcoCallback {
    void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos);
    void onSuccessRegisteringMovimiento();
    void onSuccessRemovingMovimiento();
    void onFailure(Exception e);
}