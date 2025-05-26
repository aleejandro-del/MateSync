package com.example.matesync.AppActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Callbacks.MiembrosCallback;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.Adapters.UsuarioAdapter;
import com.example.matesync.R;

import java.util.List;

public class GrupoDomHomeActivity extends AppCompatActivity {
    private MenuLateralManager navManager;
    SharedPreferencesManager sharedPreferences;
    TextView tvNombreGrupo;
    EditText etCod;
    ImageView ibQR;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuración del menú lateral
        navManager = new MenuLateralManager(
                this,
                toolbar,
                R.id.drawer_layout,
                R.id.nav_view,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close,
                sharedPreferences
        );

        sharedPreferences = new SharedPreferencesManager(this);
        ibQR = findViewById(R.id.ibQR);
        tvNombreGrupo = findViewById(R.id.tvNombreGrupo);
        tvNombreGrupo.setText(sharedPreferences.getNombreGrupo());
        etCod = findViewById(R.id.etCodigo);
        etCod.setText(sharedPreferences.getUserGroupID());
        etCod.setEnabled(false);
        cargarUsuarios();



    }

    private void cargarUsuarios() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarMiembrosGrupo(sharedPreferences.getUserGroupID(), this, new MiembrosCallback() {

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

}