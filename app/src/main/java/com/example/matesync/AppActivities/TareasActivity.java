package com.example.matesync.AppActivities;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.ConexionBBDD.ConexionBBDD;
import com.example.matesync.Callbacks.TareaCallback;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.Tarea;
import com.example.matesync.Adapters.TareaAdapter;
import com.example.matesync.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class TareasActivity extends AppCompatActivity {
    private SharedPreferencesManager sharedPreferences;
    private MenuLateralManager navManager;
    private EditText etTaskName, etTaskDescription;
    private TextView tvTareas;
    private ExtendedFloatingActionButton fab;
    private TareaAdapter adapter; // Mantener referencia al adapter

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

        inicializarComponentes();
        configurarListeners();
        cargarTareas();
    }

    //método que inicializa los componentes
    private void inicializarComponentes() {
        sharedPreferences = new SharedPreferencesManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navManager = new MenuLateralManager(this, toolbar, R.id.drawer_layout, R.id.nav_view, R.string.navigation_drawer_open, R.string.navigation_drawer_close, sharedPreferences);

        fab = findViewById(R.id.btAddTarea);
        tvTareas = findViewById(R.id.tvTareas);
        tvTareas.setText("Tareas del hogar de " + sharedPreferences.getNombreGrupo());
    }

    //configuro el listener del boton flotante
    private void configurarListeners() {
        fab.setOnClickListener(v -> crearDialogoCreacionTarea());
    }

    //método que recoge las tareas existentes en el grupo doméstico consultando Firestore
    private void cargarTareas() {
        ConexionBBDD.getInstance().recuperarTareasGrupo(sharedPreferences.getUserGroupID(),
                new TareaCallback() {
                    @Override
                    public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {
                        configurarRecyclerView(listaTareas);
                    }

                    @Override
                    public void onSuccessRegisteringTarea() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onSuccessRemovingTarea() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(TareasActivity.this, "Algo ha salido mal...", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    //método que inicializa el recyclerview correspondiente a las tareas
    private void configurarRecyclerView(List<Tarea> listaTareas) {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTareas);

        // Si el adapter ya existe, solo actualizar los datos
        if (adapter == null) {
            adapter = new TareaAdapter(listaTareas, this::manejarClickTarea);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.set(5, 0, 0, 0);
                }
            });
        } else {
            // Solo actualizar los datos existentes
            adapter.updateTareas(listaTareas); // Necesitarás añadir este método al TareaAdapter
        }
    }

    //método que maneja el click en el checkbox de una tarea
    private void manejarClickTarea(Tarea tarea) {
        // Mostrar confirmación antes de eliminar
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tarea")
                .setMessage("¿Estás seguro de que quieres eliminar esta tarea?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ConexionBBDD conn = ConexionBBDD.getInstance();
                    conn.borrarTarea(tarea, new TareaCallback() {
                        @Override
                        public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {
                            // No se utiliza aquí
                        }

                        @Override
                        public void onSuccessRegisteringTarea() {
                            // No se utiliza aquí
                        }

                        @Override
                        public void onSuccessRemovingTarea() {
                            runOnUiThread(() -> {
                                Toast.makeText(TareasActivity.this, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show();
                                cargarTareas(); // Recargar todas las tareas
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(TareasActivity.this, "Error al eliminar la tarea", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //método que crea el diálogo de creación de tarea
    private void crearDialogoCreacionTarea() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialogo_crear_tarea, null);

        etTaskName = dialogView.findViewById(R.id.etTaskName);
        etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
        );

        builder.setView(dialogView)
                .setTitle("Creación de tarea")
                .setMessage("Rellene los datos de la tarea")
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombreTarea = etTaskName.getText().toString().trim();
                    String descripcionTarea = etTaskDescription.getText().toString().trim();

                    if (!nombreTarea.isEmpty()) {
                        registrarTarea(nombreTarea, descripcionTarea);
                    } else {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    //método que registra la tarea en la base de datos
    private void registrarTarea(String nombre, String descripcion) {
        Log.d("TASK", "Nombre: " + nombre + ", Descripción: " + descripcion);

        Tarea nuevaTarea = new Tarea(
                sharedPreferences.getUserUID(),
                sharedPreferences.getUserGroupID(),
                nombre,
                descripcion,
                false
        );

        ConexionBBDD.getInstance().registrarTareaBBDD(nuevaTarea,
                new TareaCallback() {
                    @Override
                    public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {
                        // No utilizado aquí
                    }

                    @Override
                    public void onSuccessRegisteringTarea() {
                        runOnUiThread(() -> {
                            Toast.makeText(TareasActivity.this, "Tarea creada correctamente", Toast.LENGTH_SHORT).show();
                            cargarTareas(); // Recargar todas las tareas
                        });
                    }

                    @Override
                    public void onSuccessRemovingTarea() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(TareasActivity.this, "Ha habido un error al crear la tarea", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }
}