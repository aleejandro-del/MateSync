package com.example.matesync.Manager;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MenuLateralManager {
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final ActionBarDrawerToggle toggle;
    private NavigationListener listener;
    private TextView tvNombre, tvEmail;
    public interface NavigationListener {
        void onNavigationItemSelected(MenuItem item);
    }

    public MenuLateralManager(Activity activity, Toolbar toolbar,
                              int drawerLayoutId, int navViewId,
                              int openStringRes, int closeStringRes) {

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
        navigationView.setNavigationItemSelectedListener(item -> {
            if (listener != null) {
                listener.onNavigationItemSelected(item);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }


    public void setNavigationListener(NavigationListener listener) {
        this.listener = listener;
    }

    public void syncState() {
        toggle.syncState();
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public boolean isDrawerOpen() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }
}
