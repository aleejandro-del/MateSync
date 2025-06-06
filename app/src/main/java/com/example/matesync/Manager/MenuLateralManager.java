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

    private final Activity activity;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final ActionBarDrawerToggle toggle;
    private final SharedPreferencesManager sharedPreferences;
    private TextView tvUser, tvEmail;

    public MenuLateralManager(Activity activity, Toolbar toolbar,
                              int drawerLayoutId, int navViewId,
                              int openStringRes, int closeStringRes,
                              SharedPreferencesManager sharedPreferences) {

        // Validación robusta de parámetros
        validateInputParameters(activity, sharedPreferences);

        this.activity = activity;
        this.sharedPreferences = sharedPreferences;

        // Inicializar vistas con validación
        this.drawerLayout = initializeDrawerLayout(drawerLayoutId);
        this.navigationView = initializeNavigationView(navViewId);

        // Configurar ActionBarDrawerToggle
        this.toggle = createActionBarToggle(toolbar, openStringRes, closeStringRes);

        // Configurar listeners
        setupDrawerListeners();
        setupNavigationListener();

        // Cargar información del usuario en el header
        loadUserInfoInHeader();
    }

    private void validateInputParameters(Activity activity, SharedPreferencesManager sharedPreferences) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity no puede ser nulo");
        }
        if (sharedPreferences == null) {
            throw new IllegalArgumentException("SharedPreferencesManager no puede ser nulo");
        }
    }

    private DrawerLayout initializeDrawerLayout(int drawerLayoutId) {
        DrawerLayout layout = activity.findViewById(drawerLayoutId);
        if (layout == null) {
            throw new IllegalStateException("No se encontró el DrawerLayout con ID: " + drawerLayoutId);
        }
        return layout;
    }

    private NavigationView initializeNavigationView(int navViewId) {
        NavigationView navView = activity.findViewById(navViewId);
        if (navView == null) {
            throw new IllegalStateException("No se encontró el NavigationView con ID: " + navViewId);
        }
        return navView;
    }

    private ActionBarDrawerToggle createActionBarToggle(Toolbar toolbar, int openStringRes, int closeStringRes) {
        return new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                toolbar,
                openStringRes,
                closeStringRes
        );
    }

    private void setupDrawerListeners() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                toggle.syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                toggle.syncState();
            }
        });
        toggle.syncState();
    }

    private void setupNavigationListener() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadUserInfoInHeader() {
        try {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                tvUser = headerView.findViewById(R.id.tvUser);
                tvEmail = headerView.findViewById(R.id.tvEmail);

                // Cargar datos del usuario desde SharedPreferences
                String userName = sharedPreferences.getUserName();
                String userEmail = sharedPreferences.getUserEmail();

                if (tvUser != null) {
                    tvUser.setText(userName);
                }
                if (tvEmail != null) {
                    tvEmail.setText(userEmail);
                }
            }
        } catch (Exception e) {
            // Log del error sin crashear la aplicación
            android.util.Log.e("MenuLateralManager", "Error cargando información del usuario", e);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Cerrar el drawer con animación suave
        closeDrawerWithDelay();

        // Manejar cerrar sesión
        if (itemId == R.id.nav_cerrarSesion) {
            mostrarDialogoCerrarSesion();
            return true;
        }

        // Obtener actividad de destino
        Class<?> targetActivity = getTargetActivity(itemId);

        // Navegar solo si es diferente a la actividad actual
        if (targetActivity != null && shouldNavigateToActivity(targetActivity)) {
            navigateToActivity(targetActivity);
        }

        return true;
    }

    private void closeDrawerWithDelay() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private Class<?> getTargetActivity(int itemId) {
        if (itemId == R.id.nav_inicio) {
            return MainActivity.class;
        } else if (itemId == R.id.nav_grupoDomesticoHome) {
            return GrupoDomHomeActivity.class;
        } else if (itemId == R.id.nav_tareas) {
            return TareasActivity.class;
        } else if (itemId == R.id.nav_finanzas) {
            return FinanzasActivity.class;
        } else if (itemId == R.id.nav_listaCompra) {
            return ListaCompraActivity.class;
        }
        return null;
    }

    private boolean shouldNavigateToActivity(Class<?> targetActivity) {
        return !activity.getClass().equals(targetActivity);
    }

    private void navigateToActivity(Class<?> targetActivity) {
        try {
            Intent intent = new Intent(activity, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
        } catch (Exception e) {
            android.util.Log.e("MenuLateralManager", "Error navegando a la actividad", e);
        }
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(activity, com.google.android.material.R.style.Base_Theme_MaterialComponents_Dialog_Alert)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setIcon(R.drawable.inicio_logo)
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Cerrar Sesión", (dialog, which) -> {
                    realizarCerrarSesion();
                })
                .setCancelable(true)
                .create()
                .show();
    }

    private void realizarCerrarSesion() {
        try {
            // Limpiar preferencias compartidas
            sharedPreferences.clearPreferences();

            // Navegar a LoginActivity
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finishAffinity();

        } catch (Exception e) {
            android.util.Log.e("MenuLateralManager", "Error cerrando sesión", e);
        }
    }

    // Métodos públicos para control externo
    public void syncState() {
        if (toggle != null) {
            toggle.syncState();
        }
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeDrawer() {
        if (drawerLayout != null && isDrawerOpen()) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void openDrawer() {
        if (drawerLayout != null && !isDrawerOpen()) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    // Método para actualizar información del usuario
    public void updateUserInfo(String userName, String userEmail) {
        if (tvUser != null && userName != null) {
            tvUser.setText(userName);
        }
        if (tvEmail != null && userEmail != null) {
            tvEmail.setText(userEmail);
        }

    }
}