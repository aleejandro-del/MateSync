package com.example.matesync.AppActivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.example.matesync.Callbacks.AuthCallback;
import com.example.matesync.Callbacks.MiembrosCallback;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.QRManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.Adapters.UsuarioAdapter;
import com.example.matesync.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GrupoDomHomeActivity extends AppCompatActivity {
    private MenuLateralManager navManager;
    private SharedPreferencesManager sharedPreferences;
    private TextView tvNombreGrupo, tvCod;
    private ImageView ibQR, ibShare;
    private String codigoInvitacionGrupo;
    private ImageButton btGestionarUser;
    private RecyclerView recyclerViewMiembros;
    private UsuarioAdapter usuarioAdapter;
    private List<Usuario> listaUsuarios;
    // Constantes para opciones de gestión
    private static final String PROMOTE_TO_ADMIN = "Hacer Administrador";
    private static final String DEMOTE_FROM_ADMIN = "Quitar Administrador";
    private static final String REMOVE_USER = "Expulsar del Grupo";

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
        initializeComponents();
        cargarUsuarios();
        setupToolbar();
        setupQRClickListener();
        setupRecyclerView();
    }

    private void initializeComponents() {
        sharedPreferences = new SharedPreferencesManager(this);
        codigoInvitacionGrupo = sharedPreferences.getICodInv();
        ibQR = findViewById(R.id.ibQR);
        ibShare = findViewById(R.id.ibShare);
        ibShare.setOnClickListener(view -> compartirCodigoInvitacion());

        tvNombreGrupo = findViewById(R.id.tvNombreGrupo);
        tvCod = findViewById(R.id.etCodigo);
        recyclerViewMiembros = findViewById(R.id.recyclerViewMiembros);

        tvNombreGrupo.setText(sharedPreferences.getNombreGrupo());
        tvCod.setEnabled(false);
        tvCod.setText(codigoInvitacionGrupo);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navManager = new MenuLateralManager(
                this,
                toolbar,
                R.id.drawer_layout,
                R.id.nav_view,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close,
                sharedPreferences
        );
    }

    private void setupQRClickListener() {
        ibQR.setOnClickListener(v -> {
            if (codigoInvitacionGrupo != null && !codigoInvitacionGrupo.isEmpty()) {
                mostrarCodigoQR();
            } else {
                Toast.makeText(this, "Cargando información del grupo...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewMiembros.setLayoutManager(new LinearLayoutManager(this));


        recyclerViewMiembros.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 10);
            }
        });
    }


    private void mostrarCodigoQR() {
        try {
            Bitmap qrBitmap = QRManager.generarCodigoQR(codigoInvitacionGrupo);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_mostrar_qr, null);

            ImageView imageViewQR = dialogView.findViewById(R.id.imageViewQR);
            imageViewQR.setImageBitmap(qrBitmap);

            builder.setView(dialogView)
                    .setTitle("Código QR - " + sharedPreferences.getNombreGrupo())
                    .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();

        } catch (Exception e) {
            Log.e("QR_GENERATE", "Error al generar código QR", e);
            Toast.makeText(this, "Error al generar código QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void compartirCodigoInvitacion() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invitación a grupo - " + sharedPreferences.getNombreGrupo());
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "¡Te invito a unirte a mi grupo '" + sharedPreferences.getNombreGrupo() + "' en MateSync!\n\n" +
                        "Código de invitación: " + codigoInvitacionGrupo + "\n\n" +
                        "También puedes escanear el código QR desde la aplicación.");

        startActivity(Intent.createChooser(shareIntent, "Compartir código de invitación"));
    }

    private void copiarCodigoAlPortapapeles() {
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip =
                android.content.ClipData.newPlainText("Código de Invitación", codigoInvitacionGrupo);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show();
    }

    private void cargarUsuarios() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarMiembrosGrupo(sharedPreferences.getUserGroupID(), this, new MiembrosCallback() {
            @Override
            public void onRecoverMiembrosSuccess(List<Usuario> listaUsuarios) {
                GrupoDomHomeActivity.this.listaUsuarios = listaUsuarios;

                usuarioAdapter = new UsuarioAdapter(listaUsuarios, new UsuarioAdapter.OnUsuarioClickListener() {
                    @Override
                    public void onGestionarUsuarioClick(Usuario usuario) {
                        if (sharedPreferences.getIsAdmin()) {
                            mostrarOpcionesRol(usuario);
                        } else {
                            Toast.makeText(GrupoDomHomeActivity.this,
                                    "Solo los administradores pueden gestionar usuarios",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                recyclerViewMiembros.setAdapter(usuarioAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("LOAD_USERS", "Error al cargar usuarios", e);
                Toast.makeText(GrupoDomHomeActivity.this,
                        "Error al cargar miembros del grupo", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void mostrarOpcionesRol(Usuario usuario) {
        if (usuario.getEmail().equals(sharedPreferences.getUserEmail())) {
            Toast.makeText(this, "No puedes gestionar tu propio rol", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gestionar usuario: " + usuario.getNombre());

        String[] opciones;
        if (usuario.isAdmin()) {
            opciones = new String[]{DEMOTE_FROM_ADMIN, REMOVE_USER};
        } else {
            opciones = new String[]{PROMOTE_TO_ADMIN, REMOVE_USER};
        }

        builder.setItems(opciones, (dialog, which) -> {
            String opcionSeleccionada = opciones[which];
            if (opcionSeleccionada.equals(PROMOTE_TO_ADMIN)) {
                cambiarRolUsuario(usuario, true);
            } else if (opcionSeleccionada.equals(DEMOTE_FROM_ADMIN)) {
                cambiarRolUsuario(usuario, false);
            } else if (opcionSeleccionada.equals(REMOVE_USER)) {
                confirmarExpulsarUsuario(usuario);
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void cambiarRolUsuario(Usuario usuario, boolean hacerAdmin) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String grupoId = sharedPreferences.getUserGroupID();

        db.collection("gruposDomesticos")
                .document(grupoId)
                .update("administradores." + usuario.getEmail(), hacerAdmin)
                .addOnSuccessListener(aVoid -> {
                    for (Usuario u : listaUsuarios) {
                        if (u.getEmail().equals(usuario.getEmail())) {
                            u.setAdmin(hacerAdmin);
                            break;
                        }
                    }
                    usuarioAdapter.notifyDataSetChanged();

                    String mensaje = hacerAdmin ?
                            usuario.getNombre() + " es ahora administrador" :
                            usuario.getNombre() + " ya no es administrador";
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("UPDATE_ROLE", "Error al cambiar rol", e);
                    Toast.makeText(this, "Error al cambiar el rol", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmarExpulsarUsuario(Usuario usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar expulsión")
                .setMessage("¿Estás seguro de expulsar a " + usuario.getNombre() + " del grupo?")
                .setPositiveButton("Expulsar", (dialog, which) -> expulsarUsuario(usuario))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void expulsarUsuario(Usuario usuario) {
       ConexionBBDD conn = ConexionBBDD.getInstance();
       conn.borrarUsuarioDeGrupo(usuario.getGrupoID(), usuario.getUserID(), this, new AuthCallback() {
           @Override
           public void onSuccess() {
               Toast.makeText(GrupoDomHomeActivity.this, usuario.getNombre() + " ha sido expulsado", Toast.LENGTH_SHORT).show();
           }

           @Override
           public void onFailure(Exception exception) {
               Toast.makeText(GrupoDomHomeActivity.this, "Error al expulsar usuario", Toast.LENGTH_SHORT).show();
           }
       });



    }
}