package com.example.matesync.AppActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class TareasActivity extends AppCompatActivity implements MenuLateralManager.NavigationListener {
    SharedPreferencesManager sharedPreferences;
    private MenuLateralManager navManager;
    ImageView ivCrear;
    EditText etTaskName, etTaskDescription;
    TextView tvTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tareas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = new SharedPreferencesManager(this);
        tvTareas = findViewById(R.id.tvTareas);
        tvTareas.setText("Tareas del hogar de "+sharedPreferences.getNombreGrupo());
        ivCrear = findViewById(R.id.ivCrear);

        cargarTareas();
        crearMenuLateral();

        ivCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearDialogoCreacionTarea();
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

    private void cargarTareas() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarTareasGrupo(sharedPreferences.getUserGroupID(), this, new ConexionBBDD.TareaCallback() {

            @Override
            public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {

                TareaAdapter adapter = new TareaAdapter(listaTareas, new TareaAdapter.OnTareaClickListener() {
                    @Override
                    public void onTareaClick(Tarea tarea) {
                        // Manejar el clic en la tarea
                        Toast.makeText(TareasActivity.this,
                                "Tarea seleccionada: " + tarea.getNombre(),
                                Toast.LENGTH_SHORT).show();
                    }

                });
                //inflando el recyclerview y configurándolo

                RecyclerView recyclerView = findViewById(R.id.recyclerViewTareas);
                recyclerView.setLayoutManager(new LinearLayoutManager(TareasActivity.this));
                recyclerView.setAdapter(adapter);

                // Si necesitas eliminar TODOS los espacios adicionales
                recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.set(5, 0, 0, 0); // Elimina cualquier espacio
                    }
                });
                //divisor del recycler
                Drawable dividerDrawable = ContextCompat.getDrawable(TareasActivity.this, R.drawable.divider);
                DividerItemDecoration divider = new DividerItemDecoration(TareasActivity.this, DividerItemDecoration.VERTICAL);
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


    // método que crea el diálogo de creación de tarea
    private void crearDialogoCreacionTarea() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialogo_crear_tarea, null);

        etTaskName = dialogView.findViewById(R.id.etTaskName);
        etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
        );
        builder.setView(dialogView);
        builder.setTitle("Creación de tarea");
        builder.setMessage("Rellene los datos de la tarea");

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Cierra el diálogo
            }
        });

        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreTarea = etTaskName.getText().toString().trim();
                String descripcionTarea = etTaskDescription.getText().toString().trim();

                if (!nombreTarea.isEmpty()) {
                    // Aquí procesas los datos (ej: guardar en ViewModel/BD)
                    Log.d("TASK", "Nombre: " + nombreTarea + ", Descripción: " + descripcionTarea);
                    ConexionBBDD conn = ConexionBBDD.getInstance();
                    conn.registrarTareaBBDD(new Tarea(sharedPreferences.getUserName(), sharedPreferences.getUserGroupID(), nombreTarea, descripcionTarea, false), TareasActivity.this, new ConexionBBDD.TareaCallback() {
                        @Override
                        public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {

                        }

                        @Override
                        public void onSuccessRegisteringTarea() {
                            Toast.makeText(TareasActivity.this, "Tarea creada correctamente", Toast.LENGTH_SHORT).show();

                            cargarTareas();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(TareasActivity.this, "Ha habido un error al crear la tarea", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(TareasActivity.this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.create();
        builder.show();

    }

    // método que define la funcionalidad de cada item del menú lateral
    @Override
    public void onNavigationItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.nav_grupoDomesticoHome) {
            Intent intent = new Intent(TareasActivity.this, GrupoDomHomeActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_tareas) {
            Intent intent = new Intent(TareasActivity.this, TareasActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_finanzas) {

        } else if (item.getItemId() == R.id.nav_listaCompra) {

        } else if (item.getItemId() == R.id.nav_cerrarSesion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TareasActivity.this);
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
                    Intent intent = new Intent(TareasActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.create();
            builder.show();
        } else if (item.getItemId() == R.id.nav_inicio) {
            Intent intent = new Intent(TareasActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}


