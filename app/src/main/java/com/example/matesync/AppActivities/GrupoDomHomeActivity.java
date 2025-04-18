package com.example.matesync.AppActivities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.R;

public class GrupoDomHomeActivity extends AppCompatActivity {
    Usuario usuario;
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
        SharedPreferencesManager shared = new SharedPreferencesManager(this);
        usuario = new Usuario(shared.getUserUID(), shared.getUserName(), shared.getUserGroupID(), shared.getIsAdmin(), shared.getUserEmail());


    }
}