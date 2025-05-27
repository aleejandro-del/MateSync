package com.example.matesync.AppActivities;


import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.example.matesync.Callbacks.ProductoCallback;
import com.example.matesync.Callbacks.TareaCallback;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;

import com.example.matesync.Modelo.Producto;
import com.example.matesync.Adapters.ProductoAdapter;
import com.example.matesync.Modelo.Tarea;

import com.example.matesync.Adapters.TareaAdapter;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.R;

import java.util.List;

public class MainActivity extends AppCompatActivity  {
    SharedPreferencesManager sharedPreferences;
    String email, userUIDshared, userNombre, isAdmin, userGroup;
    private MenuLateralManager navManager;
    public static Usuario USUARIO = null;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuración del menú lateral (solo esta línea en cada Activity)
        navManager = new MenuLateralManager(
                this,
                toolbar,
                R.id.drawer_layout,
                R.id.nav_view,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close,
                sharedPreferences
        );
        checkSharedPreferences();
        Log.d("SharedPreferences", "email: " + sharedPreferences.getUserEmail());
        Log.d("SharedPreferences", "userUID: " + sharedPreferences.getUserUID());
        Log.d("SharedPreferences", "nombre: " + sharedPreferences.getUserName());
        Log.d("SharedPreferences", "isRegistered: " + sharedPreferences.getIsRegistered());
        Log.d("SharedPreferences", "isLogged: " + sharedPreferences.getIsLogged());
        Log.d("SharedPreferences", "UserGroupID: " + sharedPreferences.getUserGroupID());
        Log.d("SharedPreferences", "nombreGrupo: " + sharedPreferences.getNombreGrupo());

        cargarTareas();
        cargarProductos();

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
            // Si el grupoID es null, redirige al usuario a la pantalla de registro o login
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

        //si todo es correcto, creo un objeto Usuario que se podrá utilizar en toda la aplicación
        USUARIO = new Usuario(sharedPreferences.getUserUID(), sharedPreferences.getUserName(), sharedPreferences.getUserGroupID(), sharedPreferences.getIsAdmin(), sharedPreferences.getUserEmail());
    }

    private void cargarTareas() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarTareasGrupo(sharedPreferences.getUserGroupID(), this, new TareaCallback() {

            @Override
            public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {
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

    private void cargarProductos() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarProductosGrupo(sharedPreferences.getUserGroupID(), this, new ProductoCallback() {

            @Override
            public void onSuccessRecoveringProductos(List<Producto> listaProductos) {

                ProductoAdapter adapter = new ProductoAdapter(listaProductos, new ProductoAdapter.OnProductoClickListener() {
                    @Override
                    public void onProductoClick(Producto producto) {
                        // Manejar el clic en la tarea
                        Toast.makeText(MainActivity.this,"Producto seleccionada: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
                    }
                });
                //inflando el recyclerview y configurándolo

                RecyclerView recyclerView = findViewById(R.id.recyclerViewProductos);
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
            public void onSuccessRegisteringProducto() {

            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
            }
        });

    }

}