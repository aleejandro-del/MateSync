package com.example.matesync.AppActivities;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.AuthActivities.LoginActivity;
import com.example.matesync.AuthActivities.RegisterActivity;
import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;

import com.example.matesync.Modelo.Tarea;

import com.example.matesync.Modelo.TareaAdapter;
import com.example.matesync.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MenuLateralManager.NavigationListener {
    SharedPreferencesManager sharedPreferences;
    String email, userUIDshared, userNombre, isAdmin, userGroup;
    private MenuLateralManager navManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkSharedPreferences();
        Log.d("SharedPreferences", "email: " + sharedPreferences.getUserEmail());
        Log.d("SharedPreferences", "userUID: " + sharedPreferences.getUserUID());
        Log.d("SharedPreferences", "nombre: " + sharedPreferences.getUserName());
        Log.d("SharedPreferences", "isRegistered: " + sharedPreferences.getIsRegistered());
        Log.d("SharedPreferences", "isLogged: " + sharedPreferences.getIsLogged());
        Log.d("SharedPreferences", "UserGroupID: " + sharedPreferences.getUserGroupID());
        Log.d("SharedPreferences", "nombreGrupo: " + sharedPreferences.getNombreGrupo());

        cargarTareas();


        crearMenuLateral();
    }


    //método para comprobar si el usuario ya se ha registrado/logeado previamente
    private void checkSharedPreferences() {
        sharedPreferences = new SharedPreferencesManager(this);

        //si es nulo (no existe), lo mando a la pantalla de registro
        if (sharedPreferences.getUserUID().isEmpty()) {
            // Si userUID es null, redirige al usuario a la pantalla de registro o login
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        } else if (sharedPreferences.getUserGroupID().isEmpty()) {
            // Si userUID es null, redirige al usuario a la pantalla de registro o login
            Intent intent = new Intent(MainActivity.this, GrupoDomActivity.class);
            startActivity(intent);
            finish();
        }

        //aqui deberia ir el rellenado de atributos de un objeto Usuario (el que lo está usando)

        boolean isRegistered = sharedPreferences.getIsRegistered();
        boolean isLogged = sharedPreferences.getIsLogged();

        if (isRegistered == false) {

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        } else if (isLogged == false) {
            // Si el usuario está registrado pero no ha iniciado sesión, redirigir a la pantalla de login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            return;
        }
    }

    private void cargarTareas() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarTareasGrupo(sharedPreferences.getUserGroupID(), this, new ConexionBBDD.TareaCallback() {

            @Override
            public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {
                for (Tarea tarea : listaTareas) {
                    System.out.println(tarea.getNombre() + ", " + tarea.getDescripcion() + ", " + tarea.isDone());

                }
                TareaAdapter adapter = new TareaAdapter(listaTareas, new TareaAdapter.OnTareaClickListener() {
                    @Override
                    public void onTareaClick(Tarea tarea) {
                        // Manejar el clic en la tarea
                        Toast.makeText(MainActivity.this,
                                "Tarea seleccionada: " + tarea.getNombre(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
                //inflando el recyclerview y configurándolo

                RecyclerView recyclerView = findViewById(R.id.recyclerViewTareas);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);

                // Si necesitas eliminar TODOS los espacios adicionales
                recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.set(5, 0, 0, 0); // Elimina cualquier espacio
                    }
                });
                //divisor del recycler
                Drawable dividerDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.divider);
                DividerItemDecoration divider = new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL);
                divider.setDrawable(dividerDrawable);
                recyclerView.addItemDecoration(divider);
            }

            @Override
            public void onSuccessRegisteringTarea() {

            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
            }
        });

    }

    //crear el menú lateral
    public void crearMenuLateral() {
        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar helper
        navManager = new MenuLateralManager(this, toolbar, R.id.drawer_layout, R.id.nav_view, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        navManager.setNavigationListener(this);
    }

    //darle funcionalidad al pulsar los elementos del menú lateral
    @Override
    public void onNavigationItemSelected(MenuItem item) {
        // Manejar clics en los ítems

        if (item.getItemId() == R.id.nav_grupoDomesticoHome) {
            Intent intent = new Intent(MainActivity.this, GrupoDomHomeActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_tareas) {
            Intent intent = new Intent(MainActivity.this, TareasActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_finanzas) {

        } else if (item.getItemId() == R.id.nav_listaCompra) {

        } else if (item.getItemId() == R.id.nav_cerrarSesion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("CERRAR SESIÓN");
            builder.setMessage("Confirme la decisión de cerrar sesión");

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); // Cierra el diálogo
                }
            });

            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sharedPreferences.clearPreferences();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.create();
            builder.show();
        }

    }
}