package com.example.matesync.AppActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Callbacks.AuthCallback;
import com.example.matesync.Callbacks.CodInvitacionCallback;
import com.example.matesync.Manager.QRManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.GrupoDomestico;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class GrupoDomActivity extends AppCompatActivity {
    private Button btCrearGrupo, btUnirseGrupo, btEscanearQR; // AÑADIDO: botón QR
    private SharedPreferencesManager shared;
    private ConexionBBDD conn;

    // AÑADIDO: Launcher para el escáner QR
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                } else {
                    String codigoEscaneado = result.getContents();
                    Log.d("QR_SCAN", "Código escaneado: " + codigoEscaneado);
                    procesarCodigoQREscaneado(codigoEscaneado);
                }
            }
    );

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

        initializeComponents();
        setupEventListeners();
    }

    private void initializeComponents() {
        btCrearGrupo = findViewById(R.id.btCrearGrupo);
        btUnirseGrupo = findViewById(R.id.btUnirseGrupo);

        shared = new SharedPreferencesManager(this);
        conn = ConexionBBDD.getInstance();
    }

    private void setupEventListeners() {
        btCrearGrupo.setOnClickListener(v -> mostrarDialogoCrearGrupo());
        btUnirseGrupo.setOnClickListener(v -> mostrarOpcionesUnirse()); // MODIFICADO: ahora muestra opciones
    }

    // AÑADIDO: Método para mostrar opciones de unirse
    private void mostrarOpcionesUnirse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unirse a Grupo");
        builder.setMessage("¿Cómo quieres unirte al grupo?");

        builder.setPositiveButton("Código Manual", (dialog, which) -> mostrarDialogoUnirseGrupo());
        builder.setNeutralButton("Escanear QR", (dialog, which) -> iniciarEscaneoQR());
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // AÑADIDO: Método para iniciar escaneo QR
    private void iniciarEscaneoQR() {
        // Verificar permisos de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }

        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Escanea el código QR del grupo");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(false);

        barcodeLauncher.launch(options);
    }

    // AÑADIDO: Método para procesar código QR escaneado
    private void procesarCodigoQREscaneado(String codigoEscaneado) {
        try {
            // Validar que el código sea de 8 caracteres (formato actual)
            if (codigoEscaneado != null && codigoEscaneado.trim().length() == 8) {
                verificarYUnirseAGrupo(codigoEscaneado.trim().toUpperCase());
            } else {
                Toast.makeText(this, "El código QR no contiene un código de invitación válido de 8 caracteres",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("QR_PROCESS", "Error al procesar código QR", e);
            Toast.makeText(this, "Error al procesar el código QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoCrearGrupo() {
        EditText campoCrearGrupo = new EditText(this);
        campoCrearGrupo.setHint("Nombre del grupo");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CREAR GRUPO");
        builder.setMessage("Introduce el nombre del Grupo");
        builder.setView(campoCrearGrupo);

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String nombreGrupo = campoCrearGrupo.getText().toString().trim();
            if (validarNombreGrupo(nombreGrupo)) {
                crearGrupoDomestico(nombreGrupo);
            }
        });

        builder.create().show();
    }

    private boolean validarNombreGrupo(String nombreGrupo) {
        if (nombreGrupo.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (nombreGrupo.length() < 3) {
            Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (nombreGrupo.length() > 30) {
            Toast.makeText(this, "El nombre no puede exceder 30 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void crearGrupoDomestico(String nombreGrupo) {
        // 1. Generar ID único para el grupo
        String grupoID = FirebaseFirestore.getInstance().collection("gruposDomesticos").document().getId();
        // 2. Generar código de invitación separado
        String codigoInvitacion = generarCodigoInvitacion();

        // 3. Crear usuario administrador con el grupoID correcto
        Usuario usuarioAdmin = new Usuario(
                shared.getUserUID(),
                shared.getUserName(),
                grupoID,  // Aquí asignamos el grupoID, no el código de invitación
                true,
                shared.getUserEmail()
        );

        // 4. Crear lista de miembros
        ArrayList<Usuario> listaUsuarios = new ArrayList<>();
        listaUsuarios.add(usuarioAdmin);

        // 5. Crear grupo doméstico
        GrupoDomestico grupoDom = new GrupoDomestico(
                grupoID,          // ID del documento
                nombreGrupo,
                codigoInvitacion, // Código para compartir (8 caracteres)
                listaUsuarios
        );

        // Guardar ambos valores en SharedPreferences
        shared.setCodigoInvitacion(codigoInvitacion);
        shared.setUserGroup(grupoID);

        // 6. Registrar el grupo primero en Firestore
        conn.registrarGrupoBBDD(grupoID, grupoDom, new AuthCallback() {
            @Override
            public void onSuccess() {
                Log.d("GrupoDom", "Grupo creado. Ahora actualizando usuario...");

                // 7. Actualizar el usuario con el grupoID
                FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(shared.getUserUID())
                        .update(
                                "grupoID", grupoID,
                                "admin", true
                        )
                        .addOnSuccessListener(aVoid -> {
                            Log.d("GrupoDom", "Usuario actualizado correctamente");

                            // Actualizar SharedPreferences
                            shared.setNombreGrupo(nombreGrupo);
                            shared.setIsAdmin(true);

                            // Mostrar QR con el código de invitación
                            mostrarCodigoQRGrupo(codigoInvitacion, nombreGrupo);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("GrupoDom", "Error al actualizar usuario", e);
                            // Opcional: eliminar el grupo si falla la actualización del usuario
                        });
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e("GrupoDom", "Error al crear grupo", exception);
                Toast.makeText(GrupoDomActivity.this,
                        "Error al crear el grupo: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarUsuarioEnBBDD(String grupoID, String nombreGrupo, String codigoInvitacion) { // MODIFICADO: añadido parámetro
        // Actualizar el usuario en la colección principal con el grupoID correcto
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(shared.getUserUID())
                .update("grupoID", grupoID, "admin", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("GrupoDom", "Usuario actualizado correctamente");

                    // Actualizar SharedPreferences
                    shared.setUserGroup(grupoID);
                    shared.setNombreGrupo(nombreGrupo);
                    shared.setIsAdmin(true);

                    Toast.makeText(GrupoDomActivity.this,
                            "Grupo '" + nombreGrupo + "' creado exitosamente",
                            Toast.LENGTH_SHORT).show();

                    // AÑADIDO: Mostrar código QR después de crear el grupo
                    mostrarCodigoQRGrupo(codigoInvitacion, nombreGrupo);
                })
                .addOnFailureListener(e -> {
                    Log.e("GrupoDom", "Error al actualizar usuario", e);
                    Toast.makeText(GrupoDomActivity.this,
                            "Error al finalizar la creación del grupo",
                            Toast.LENGTH_LONG).show();
                });
    }

    // AÑADIDO: Método para mostrar QR después de crear grupo
    private void mostrarCodigoQRGrupo(String codigoInvitacion, String nombreGrupo) {
        try {
            // Generar QR con el código de invitación
            Bitmap qrBitmap = QRManager.generarCodigoQR(codigoInvitacion);

            // Crear diálogo para mostrar el QR
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_mostrar_qr, null);

            ImageView imageViewQR = dialogView.findViewById(R.id.imageViewQR);
            imageViewQR.setImageBitmap(qrBitmap);

            builder.setView(dialogView);
            builder.setTitle("Código QR del Grupo");
            builder.setMessage("Comparte este código QR para que otros se unan a '" + nombreGrupo + "'");

            builder.setPositiveButton("Continuar", (dialog, which) -> {
                dialog.dismiss();
                navegarAMainActivity();
            });

            builder.create().show();

        } catch (Exception e) {
            Log.e("QR_GENERATE", "Error al generar código QR", e);
            Toast.makeText(this, "Error al generar código QR", Toast.LENGTH_SHORT).show();
            navegarAMainActivity();
        }
    }

    private void mostrarDialogoUnirseGrupo() {
        EditText campoUnirseGrupo = new EditText(this);
        campoUnirseGrupo.setHint("Código de invitación (8 caracteres)");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("UNIRSE A GRUPO");
        builder.setMessage("Introduce el código de invitación del Grupo");
        builder.setView(campoUnirseGrupo);

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String codigoInv = campoUnirseGrupo.getText().toString().trim();
            if (validarCodigoInvitacion(codigoInv)) {
                verificarYUnirseAGrupo(codigoInv);
            }
        });

        builder.create().show();
    }

    private boolean validarCodigoInvitacion(String codigo) {
        if (codigo.isEmpty()) {
            Toast.makeText(this, "El código no puede estar vacío", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (codigo.length() != 8) {
            Toast.makeText(this, "El código debe tener exactamente 8 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void verificarYUnirseAGrupo(String codigoInv) {
        conn.verificarCodigoInvitacion(codigoInv, new CodInvitacionCallback() {
            @Override
            public void onCodigoValido(String codigoInvitacion, String grupoId, String nombreGrupo) {
                Log.d("GrupoDom", "Código válido. Uniéndose al grupo: " + nombreGrupo);
                unirseAGrupo(codigoInvitacion, grupoId, nombreGrupo);

            }

            @Override
            public void onCodigoNoValido() {
                Toast.makeText(GrupoDomActivity.this,
                        "El código de invitación no es válido",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Log.e("GrupoDom", "Error al verificar código", e);
                Toast.makeText(GrupoDomActivity.this,
                        "Error al verificar el código: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void unirseAGrupo(String codigoInvitacion, String grupoId, String nombreGrupo) {
        Usuario usuario = new Usuario(
                shared.getUserUID(),
                shared.getUserName(),
                grupoId,
                false,
                shared.getUserEmail()
        );

        conn.addUserAGrupo(usuario, grupoId, this);

        // Actualizar SharedPreferences
        shared.setUserGroup(grupoId);
        shared.setNombreGrupo(nombreGrupo);
        shared.setIsAdmin(false);
        shared.setCodigoInvitacion(codigoInvitacion);
        Toast.makeText(this,
                "Te has unido exitosamente al grupo '" + nombreGrupo + "'",
                Toast.LENGTH_SHORT).show();

        // Dar tiempo para que se complete la operación en Firestore
        new android.os.Handler().postDelayed(() -> {
            navegarAMainActivity();
        }, 2000);
    }

    private void navegarAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Genera un código de 8 caracteres únicos
    public static String generarCodigoInvitacion() {
        String uuid = FirebaseFirestore.getInstance().collection("gruposDomesticos").document().getId();

        return uuid.substring(0, 8).toUpperCase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar recursos si es necesario
        if (conn != null) {
            conn.cleanupListeners();
        }
    }
}