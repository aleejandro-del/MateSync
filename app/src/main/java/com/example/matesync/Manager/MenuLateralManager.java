package com.example.matesync.Manager;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.matesync.AppActivities.FinanzasActivity;
import com.example.matesync.AppActivities.GrupoDomHomeActivity;
import com.example.matesync.AppActivities.ListaCompraActivity;
import com.example.matesync.AppActivities.MainActivity;
import com.example.matesync.AppActivities.TareasActivity;
import com.example.matesync.AuthActivities.LoginActivity;
import com.example.matesync.R;
import com.google.android.material.navigation.NavigationView;

public class MenuLateralManager implements NavigationView.OnNavigationItemSelectedListener {
    private  Activity activity;
    private  DrawerLayout drawerLayout;
    private  NavigationView navigationView;
    private  ActionBarDrawerToggle toggle;
    private  SharedPreferencesManager sharedPreferences;
    private TextView tvUser, tvEmail;
    public MenuLateralManager(Activity activity, Toolbar toolbar,
                              int drawerLayoutId, int navViewId,
                              int openStringRes, int closeStringRes,
                              SharedPreferencesManager sharedPreferences) {

        this.activity = activity;
        this.sharedPreferences = sharedPreferences;

        // Obtener referencias
        drawerLayout = activity.findViewById(drawerLayoutId);
        navigationView = activity.findViewById(navViewId);

        // Configurar toggle
        toggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar,
                openStringRes, closeStringRes
        );

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                toggle.syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                toggle.syncState();
            }
        });
        toggle.syncState();

        // Configurar listener de items
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Cerrar el drawer primero
        drawerLayout.closeDrawer(GravityCompat.START);

        // Manejar la navegación
        if (itemId == R.id.nav_cerrarSesion) {
            mostrarDialogoCerrarSesion(sharedPreferences);
            return true;
        }

        Class<?> targetActivity = null;

        if (itemId == R.id.nav_grupoDomesticoHome) {
            targetActivity = GrupoDomHomeActivity.class;
        } else if (itemId == R.id.nav_tareas) {
            targetActivity = TareasActivity.class;
        } else if (itemId == R.id.nav_finanzas) {
            targetActivity = FinanzasActivity.class;
        } else if (itemId == R.id.nav_listaCompra) {
           targetActivity = ListaCompraActivity.class;
        } else if (itemId == R.id.nav_inicio) {
            targetActivity = MainActivity.class;
        }

        // Si es la misma actividad actual, no hacemos nada
        if (targetActivity != null && !activity.getClass().equals(targetActivity)) {
            activity.startActivity(new Intent(activity, targetActivity));
            activity.finish();
        }

        return true;
    }

    private void mostrarDialogoCerrarSesion(SharedPreferencesManager sharedPreferences) {
        new AlertDialog.Builder(activity)
                .setTitle("CERRAR SESIÓN")
                .setMessage("Confirme la decisión de cerrar sesión")
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    sharedPreferences.clearPreferences();
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                    activity.finishAffinity(); // Cierra todas las activities
                })
                .create()
                .show();
    }

    public void syncState() {
        toggle.syncState();
    }

    public boolean isDrawerOpen() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }
}