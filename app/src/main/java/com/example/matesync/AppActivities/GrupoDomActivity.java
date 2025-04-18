package com.example.matesync.AppActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.GrupoDomestico;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class GrupoDomActivity extends AppCompatActivity {
    Button btCrearGrupo, btUnirseGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grupo_dom);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btCrearGrupo = findViewById(R.id.btCrearGrupo);
        btUnirseGrupo = findViewById(R.id.btUnirseGrupo);

        EditText campoCrearGrupo = new EditText(GrupoDomActivity.this);
        campoCrearGrupo.setHint("Nombre del grupo");
        campoCrearGrupo.setWidth(70);
        SharedPreferencesManager shared = new SharedPreferencesManager(GrupoDomActivity.this);
        btCrearGrupo.setOnClickListener(new View.OnClickListener() {
            GrupoDomestico grupoDom = null;

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GrupoDomActivity.this);
                builder.setTitle("CREAR GRUPO");
                builder.setMessage("Introduce el nombre del Grupo");
                builder.setView(campoCrearGrupo);
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });

                builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nombreGrupo = campoCrearGrupo.getText().toString().trim();
                        if (!nombreGrupo.isEmpty()) {

                            String grupoID = FirebaseFirestore.getInstance().collection("gruposDomesticos").document().getId();
                            ArrayList<Usuario> listaUsuarios = new ArrayList<Usuario>();
                            listaUsuarios.add(new Usuario(shared.getUserUID(), shared.getUserName(), grupoID, true, shared.getUserEmail()));
                            grupoDom = new GrupoDomestico(grupoID, nombreGrupo, generarCodigoInvitacion(), listaUsuarios);
                            ConexionBBDD conn = ConexionBBDD.getInstance();
                            conn.registrarGrupoBBDD(grupoID, grupoDom, new ConexionBBDD.AuthCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(GrupoDomActivity.this, "Grupo creado " + nombreGrupo, Toast.LENGTH_SHORT).show();

                                    String grupoID = FirebaseFirestore.getInstance().collection("gruposDomesticos").document().getId();
                                    FirebaseFirestore.getInstance()
                                            .collection("usuarios")
                                            .document(shared.getUserUID())
                                            .update("grupoID", grupoID);

                                    FirebaseFirestore.getInstance()
                                            .collection("usuarios")
                                            .document(shared.getUserUID())
                                            .update("admin", true);

                                    shared.setNombreGrupo(nombreGrupo);

                                    Intent intent = new Intent(GrupoDomActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(Exception exception) {
                                    Toast.makeText(GrupoDomActivity.this, "Ha habido un error al crear el grupo" + nombreGrupo, Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(GrupoDomActivity.this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.create();
                builder.show();

            }
        });
        EditText campoUnirseGrupo = new EditText(GrupoDomActivity.this);
        campoUnirseGrupo.setHint("Código de invitación (8 caracteres)");
        campoUnirseGrupo.setWidth(70);
        btUnirseGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GrupoDomActivity.this);
                builder.setTitle("UNIRSE A GRUPO");
                builder.setMessage("Introduce el código de invitación del Grupo");
                builder.setView(campoUnirseGrupo);
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Cierra el diálogo
                    }
                });

                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String codigoInv = campoUnirseGrupo.getText().toString().trim();
                        ConexionBBDD conn = ConexionBBDD.getInstance();
                        conn.verificarCodigoInvitacion(codigoInv, new ConexionBBDD.CodInvitacionCallback() {


                            @Override
                            public void onCodigoValido(String grupoId, String nombreGrupo) {
                                conn.addUserAGrupo(new Usuario(shared.getUserUID(), shared.getUserName(), grupoId, false, shared.getUserEmail()), grupoId, GrupoDomActivity.this);
                                SharedPreferencesManager shared = new SharedPreferencesManager(GrupoDomActivity.this);
                                shared.setUserGroup(grupoId);

                                try {
                                    Thread.sleep(3000);
                                    shared.setNombreGrupo(nombreGrupo);
                                    Intent intent = new Intent(GrupoDomActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                            }

                            @Override
                            public void onCodigoNoValido() {
                                Toast.makeText(GrupoDomActivity.this, "El grupo no existe", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(GrupoDomActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.create();
                builder.show();
            }
        });
    }


    // Genera un código de 8 caracteres (similar a los UID de Firestore pero más corto)
    public static String generarCodigoInvitacion() {
        String uuid = FirebaseFirestore.getInstance().collection("gruposDomesticos").document().getId();
        return uuid.substring(0, 8).toUpperCase(); // Ejemplo: "A3F9B2Z7"
    }
}