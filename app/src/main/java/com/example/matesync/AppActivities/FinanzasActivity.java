package com.example.matesync.AppActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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


import com.example.matesync.AuthActivities.LoginActivity;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FinanzasActivity extends AppCompatActivity implements MenuLateralManager.NavigationListener {

    private MenuLateralManager navManager;
    private SharedPreferencesManager sharedPreferences;

    private PieChart pieChart;
    private TextView tvIngresos, tvGastos, tvBalance;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_finanzas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = new SharedPreferencesManager(this);
        crearMenuLateral();
        inicializarComponentes();
        mostrarDatosFinancieros();
        //cargarListaDeMovimientos();
    }

    private void inicializarComponentes() {
        pieChart = findViewById(R.id.pieChart);
        tvIngresos = findViewById(R.id.tvTotalIngresos);
        tvGastos = findViewById(R.id.tvTotalGastos);
        tvBalance = findViewById(R.id.tvBalance);
        recyclerView = findViewById(R.id.rvMovimientos);
        fab = findViewById(R.id.fabAddMovimiento);

        fab.setOnClickListener(v -> {
            // Aquí puedes abrir una nueva actividad para agregar movimientos
            Toast.makeText(this, "Agregar movimiento", Toast.LENGTH_SHORT).show();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void mostrarDatosFinancieros() {
        // Datos simulados
        float ingresos = 1000f;
        float gastos = 450f;
        float balance = ingresos - gastos;

        tvIngresos.setText(ingresos + "€");
        tvGastos.setText(gastos + "€");
        tvBalance.setText(balance + "€");

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(ingresos, "Ingresos"));
        entries.add(new PieEntry(gastos, "Gastos"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimaryDark));
        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.invalidate(); // refrescar
    }

    /*private void cargarListaDeMovimientos() {
        // Datos simulados
        List<Movimiento> movimientos = new ArrayList<>();
        movimientos.add(new Movimiento("Sueldo", "+1000€", "01/05/2025"));
        movimientos.add(new Movimiento("Compra supermercado", "-200€", "03/05/2025"));
        movimientos.add(new Movimiento("Luz", "-50€", "05/05/2025"));

        MovimientoAdapter adapter = new MovimientoAdapter(movimientos);
        recyclerView.setAdapter(adapter);
    }*/

    @Override
    public void onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_grupoDomesticoHome) {
            Intent intent = new Intent(FinanzasActivity.this, GrupoDomHomeActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_tareas) {
            Intent intent = new Intent(FinanzasActivity.this, TareasActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_finanzas) {
            return;
        } else if (item.getItemId() == R.id.nav_listaCompra) {
            Intent intent = new Intent(FinanzasActivity.this, ListaCompraActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_cerrarSesion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(FinanzasActivity.this);
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
                    Intent intent = new Intent(FinanzasActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.create();
            builder.show();
        } else if (item.getItemId() == R.id.nav_inicio) {
            Intent intent = new Intent(FinanzasActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void crearMenuLateral() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navManager = new MenuLateralManager(this, toolbar, R.id.drawer_layout, R.id.nav_view, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navManager.setNavigationListener(this);
    }
}
