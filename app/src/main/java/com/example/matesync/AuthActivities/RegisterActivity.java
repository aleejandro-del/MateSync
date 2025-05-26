package com.example.matesync.AuthActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Callbacks.AuthCallback;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText etNombre;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etRepeatPassword;
    private Button registerButton;
    private Button registerGoLoginButton;
    SharedPreferencesManager shared ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        shared = new SharedPreferencesManager(this);
        etNombre = findViewById(R.id.registerNombre);
        etEmail = findViewById(R.id.registerEmail);
        etPassword = findViewById(R.id.registerPassword);
        etRepeatPassword = findViewById(R.id.registerRepeatPassword);
        registerButton = findViewById(R.id.registerButton);
        registerGoLoginButton = findViewById(R.id.registerGoLoginButton);

        //acción del botón "Registro"
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String repeatPassword = etRepeatPassword.getText().toString().trim();
                String nombre = etNombre.getText().toString().trim();
                checkFields(nombre, email, password, repeatPassword);
            }
        });
        //acción del botón "Ya tengo cuenta"
        registerGoLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //comprobar si lo introducido cumple los requisitos para el registro en FireBaseAuth
    private void checkFields(String nombre, String email, String password, String repeatPassword) {


        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_LONG).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Ingrese un email válido", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(repeatPassword)) {
            Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
            return;
        }
        if (nombre.length() < 3) {
            Toast.makeText(RegisterActivity.this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show();
            return;
        }
        register(nombre, email, password);


    }

    private void register(String nombre, String email, String password) {
        registerButton.setEnabled(true);
        ConexionBBDD auth = ConexionBBDD.getInstance();

        auth.registerUser(email, nombre, password, this, new AuthCallback() {
            @Override
            public void onSuccess() {

                shared.setRegistered(true);
                shared.setUserName(nombre);
                shared.setUserEmail(email);
                shared.setUserUID(ConexionBBDD.getAuth().getUid());
                shared.setIsAdmin(false);

                // Registro exitoso
                Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception exception) {
                // Error en el registro
                Toast.makeText(RegisterActivity.this, "Fallo en el registro: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}