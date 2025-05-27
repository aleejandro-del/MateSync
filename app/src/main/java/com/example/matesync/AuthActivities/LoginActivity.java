package com.example.matesync.AuthActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matesync.AppActivities.GrupoDomActivity;
import com.example.matesync.AppActivities.MainActivity;
import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Callbacks.FirestoreUserCallback;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.R;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginButton, goRegisterButton;
    private TextView goRecoverPassword;
    private boolean estaEnGrupo = false;
    SharedPreferencesManager shared;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        goRegisterButton = findViewById(R.id.goRegisterButton);
        goRecoverPassword = findViewById(R.id.goRecoverPassword);
        shared = new SharedPreferencesManager(this);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                if (!(email.isEmpty() && password.isEmpty())) {
                    login(email, password);

                } else {
                    Toast.makeText(LoginActivity.this, "Debe rellenar los campos", Toast.LENGTH_SHORT).show();
                }


            }
        });

        goRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        goRecoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RecuperarPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void login(String email, String password) {
        ConexionBBDD conn = ConexionBBDD.getInstance();

        conn.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // Obtener el usuario AUTENTICADO correctamente
                        FirebaseUser user = conn.getAuth().getCurrentUser();
                        if (user != null) {
                            String userUID = user.getUid(); // UID válido
                            System.out.println("User UID: " + userUID);


                            shared.setLoggedIn(true);
                            shared.setRegistered(true);
                            shared.setUserEmail(email);
                            shared.setUserUID(userUID);

                            // Obtener datos adicionales de Firestore
                            conn.getUserBBDDdata(userUID, new FirestoreUserCallback() {
                                @Override
                                public void onCallback(String nombreUser, String email, boolean isAdmin, String grupoID, String nombreGrupo) {
                                    if (nombreUser != null && !nombreUser.isEmpty()) {
                                        shared.setUserName(nombreUser);
                                        shared.setUserGroup(grupoID);
                                        shared.setIsAdmin(isAdmin);
                                        shared.setNombreGrupo(nombreGrupo);
                                        Log.d("LoginFlow", "Datos completos obtenidos:");
                                        Log.d("LoginFlow", "Nombre: " + nombreUser);
                                        Log.d("LoginFlow", "GrupoID: " + grupoID);
                                        Log.d("LoginFlow", "Admin: " + isAdmin);
                                        Log.d("LoginFlow", "nombre de grupo: " + nombreGrupo);
                                        estaEnGrupo = !grupoID.isEmpty();
                                        Toast.makeText(LoginActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();

                                        if (estaEnGrupo) {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Intent intent = new Intent(LoginActivity.this, GrupoDomActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        // Manejar errores de autenticación
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}