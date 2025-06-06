package com.example.matesync.AuthActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matesync.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarPasswordActivity extends AppCompatActivity {
    FirebaseAuth auth;
    MaterialButton enviarCorreoRecBt, volverButton;
    EditText emailRecuperacionEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance(); //se instancia el objecto de autenticación de Firebase

        enviarCorreoRecBt = findViewById(R.id.enviarCorreoRecBt);
        emailRecuperacionEditText = findViewById(R.id.emailRecuperacionEditText);
        volverButton = findViewById(R.id.volverButton);
        //proceso de envío de email de cambio de contraseña
        enviarCorreoRecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailRecuperacion = emailRecuperacionEditText.getText().toString().trim();
                //si no se introduce nada, se pide al usuario que introduzca un email
                if(emailRecuperacion.isEmpty()){
                    Toast.makeText(RecuperarPasswordActivity.this, "Por favor introduzca un correo electrónico", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    auth.sendPasswordResetEmail(emailRecuperacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RecuperarPasswordActivity.this, "Correo enviado", Toast.LENGTH_LONG).show();
                                // el correo ya se ha enviado. El usuario introducirá una nueva contraseña y Firebase gestionará automáticamente el cambio de contraseña
                            } else {
                                Toast.makeText(RecuperarPasswordActivity.this, "El correo no está registrado o ha ocurrido un error", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });


        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volverLoginActivity(v);
            }
        });
    }


    public void volverLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}