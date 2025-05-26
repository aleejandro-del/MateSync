package com.example.matesync.Callbacks;

import com.example.matesync.Modelo.Producto;

import java.util.List;

public interface ProductoCallback {
    void onSuccessRecoveringProductos(List<Producto> listaProductos);
    void onSuccessRegisteringProducto();
    void onFailure(Exception e);
}