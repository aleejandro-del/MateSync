package com.example.matesync.AppActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.Modelo.UsuarioAdapter;
import com.example.matesync.R;

import java.util.List;

public class GrupoDomHomeActivity extends AppCompatActivity implements MenuLateralManager.NavigationListener {
    private MenuLateralManager navManager;
    SharedPreferencesManager sharedPreferences;
    TextView tvNombreGrupo;
    EditText etCod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grupo_dom_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = new SharedPreferencesManager(this);
        tvNombreGrupo = findViewById(R.id.tvNombreGrupo);
        tvNombreGrupo.setText(sharedPreferences.getNombreGrupo());
        etCod = findViewById(R.id.etCodigo);
        etCod.setText(sharedPreferences.getUserGroupID());
        etCod.setEnabled(false);
        cargarUsuarios();
        crearMenuLateral();



    }

    private void cargarUsuarios() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarMiembrosGrupo(sharedPreferences.getUserGroupID(), this, new ConexionBBDD.MiembrosCallback() {

            @Override
            public void onRecoverMiembrosSuccess(List<Usuario> listaUsuarios) {
                UsuarioAdapter adapter = new UsuarioAdapter(listaUsuarios, new UsuarioAdapter.OnUsuarioClickListener() {
                    @Override
                    public void onMiembroClick(Usuario usuario) {
                        ImageButton btRoles = findViewById(R.id.btRoles);
                        btRoles.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        // Manejar el clic en la tarea
                        Toast.makeText(GrupoDomHomeActivity.this,
                                "Miembro seleccionado: " + usuario.getNombre(),
                                Toast.LENGTH_SHORT).show();

                    }

                });

                //inflando el recyclerview y configurándolo
                RecyclerView recyclerView = findViewById(R.id.recyclerViewMiembros);
                recyclerView.setLayoutManager(new LinearLayoutManager(GrupoDomHomeActivity.this));
                recyclerView.setAdapter(adapter);

                // Si necesitas eliminar TODOS los espacios adicionales
                recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.set(0, 0, 0, 10); // Elimina cualquier espacio
                    }
                });
                //divisor del recycler
                Drawable dividerDrawable = ContextCompat.getDrawable(GrupoDomHomeActivity.this, R.drawable.divider);
                DividerItemDecoration divider = new DividerItemDecoration(GrupoDomHomeActivity.this, DividerItemDecoration.VERTICAL);
                divider.setDrawable(dividerDrawable);
                recyclerView.addItemDecoration(divider);
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

    @Override
    public void onNavigationItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.nav_grupoDomesticoHome) {
            Intent intent = new Intent(GrupoDomHomeActivity.this, GrupoDomHomeActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_tareas) {
            Intent intent = new Intent(GrupoDomHomeActivity.this, TareasActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_finanzas) {

        } else if (item.getItemId() == R.id.nav_listaCompra) {

        } else if (item.getItemId() == R.id.nav_cerrarSesion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(GrupoDomHomeActivity.this);
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
                    Intent intent = new Intent(GrupoDomHomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.create();
            builder.show();
        } else if (item.getItemId() == R.id.nav_inicio) {
            Intent intent = new Intent(GrupoDomHomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}