package com.example.fase1tfg;

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

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerRepeatPassword;
    private Button registerButton;
    private Button registerGoLoginButton;
    private FirebaseAuth auth;
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
        auth = FirebaseAuth.getInstance();
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerRepeatPassword = findViewById(R.id.registerRepeatPassword);
        registerButton = findViewById(R.id.registerButton);
        registerGoLoginButton = findViewById(R.id.registerGoLoginButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = registerEmail.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();
                String repeatPassword = registerRepeatPassword.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
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

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show();
                    return;
                }

                register(email, password);
            }
        });

        registerGoLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void register(String email, String password) {
        registerButton.setEnabled(false);
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, task-> {
            registerButton.setEnabled(true);
 ;           if(task.isSuccessful()){
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(this, "Fallo en el registro", Toast.LENGTH_LONG).show();
            }
        });;
    }
}