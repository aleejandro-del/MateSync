package com.example.matesync.AppActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
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


import com.example.matesync.Adapters.MovEcoAdapter;
import com.example.matesync.AuthActivities.LoginActivity;
import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Callbacks.MovEcoCallback;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.MovimientoEconomico;
import com.example.matesync.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FinanzasActivity extends AppCompatActivity  {

    private MenuLateralManager navManager;
    private SharedPreferencesManager sharedPreferences;

    private PieChart pieChart;
    private EditText etMovConcepto, etMovCuantia;
    private RadioGroup rgTipoMovimiento;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private String tipo = "";
    private List<MovimientoEconomico> listaMovimientoEconomicos = new ArrayList<>();

    // *** CAMBIO IMPORTANTE: Declarar el adapter como variable de instancia ***
    private MovEcoAdapter adapter;

    private TextView tvTotalIngresos, tvTotalGastos, tvBalance;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuración del menú lateral (solo esta línea en cada Activity)
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
        recyclerView = findViewById(R.id.rvMovimientos);
        fab = findViewById(R.id.btAddMovimiento);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pieChart = findViewById(R.id.pieChart);
        tvTotalIngresos = findViewById(R.id.tvTotalIngresos);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvBalance = findViewById(R.id.tvBalance);

        // *** CAMBIO: Inicializar el adapter una sola vez ***
        setupRecyclerView();

       // crearMenuLateral();

        fab.setOnClickListener(v -> {
            crearDialogoCreacion();
        });
        cargarMovimientos();
    }

    // *** NUEVO MÉTODO: Configurar RecyclerView una sola vez ***
    private void setupRecyclerView() {
        adapter = new MovEcoAdapter(listaMovimientoEconomicos, new MovEcoAdapter.OnMovEvClickListener() {
            @Override
            public void onMovimientoClick(MovimientoEconomico movimientoEconomico) {
                Toast.makeText(FinanzasActivity.this, "Movimiento seleccionado: " + movimientoEconomico.getConcepto(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar decoraciones una sola vez
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(5, 0, 0, 0);
            }
        });

        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(dividerDrawable);
        recyclerView.addItemDecoration(divider);
    }

    private void crearDialogoCreacion() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialogo_crear_movimiento_financiero, null);

        etMovConcepto = dialogView.findViewById(R.id.etMovConcepto);
        etMovCuantia = dialogView.findViewById(R.id.etMovCuantia);
        rgTipoMovimiento = dialogView.findViewById(R.id.rgTipoMovimiento);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
        );
        builder.setView(dialogView);
        builder.setTitle("Creación de movimiento económico");
        builder.setMessage("Rellene los datos del movimiento");

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String concepto = "";
                String valorString = "";
                Float valor = 0f;

                try {
                    concepto = etMovConcepto.getText().toString().trim();
                    valorString = etMovCuantia.getText().toString().trim();
                    valor = Float.parseFloat(valorString);
                    int checkedId = rgTipoMovimiento.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbIngreso) {
                        tipo = "Ingreso";
                    } else if (checkedId == R.id.rbGasto) {
                        tipo = "Gasto";
                    } else {
                        Toast.makeText(FinanzasActivity.this, "Selecciona un tipo de movimiento", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!concepto.isEmpty() | !valorString.isEmpty()) {
                        Log.d("TASK", "Nombre: " + concepto + ", valor: " + valor);
                        ConexionBBDD conn = ConexionBBDD.getInstance();
                        conn.registrarMovEcoBBDD(new MovimientoEconomico(sharedPreferences.getUserUID(), sharedPreferences.getUserGroupID(), concepto, tipo, valor), FinanzasActivity.this, new MovEcoCallback() {
                            @Override
                            public void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos) {

                            }

                            @Override
                            public void onSuccessRegisteringMovimiento() {
                                Toast.makeText(FinanzasActivity.this, "Movimiento registrado correctamente", Toast.LENGTH_SHORT).show();
                                cargarMovimientos();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(FinanzasActivity.this, "Ha habido un error al crear la tarea", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                        Toast.makeText(FinanzasActivity.this, "Debe rellenar ambos campos", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        builder.create();
        builder.show();
    }

    //método para poblar el gráfico de tarta y los 3 campos numéricos que informan de las cifras totales referente a ingresos-gastos-balance
    private void mostrarDatosFinancieros() {
        float totalIngresos = 0;
        float totalGastos = 0;

        for (MovimientoEconomico mov : listaMovimientoEconomicos) {
            System.out.println(mov.getTipo());
            if (mov.getTipo().equals("Ingreso")) {
                totalIngresos += (float) mov.getValor();
            } else if (mov.getTipo().equals("Gasto")) {
                totalGastos += (float) mov.getValor();
            }
        }

        double balance = totalIngresos - totalGastos;
        tvTotalIngresos.setText(totalIngresos + "€");
        tvTotalGastos.setText(totalGastos + "€");
        tvBalance.setText(balance + "€");

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totalIngresos, "Ingresos"));
        entries.add(new PieEntry(totalGastos, "Gastos"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getResources().getColor(R.color.VERDE), getResources().getColor(R.color.ROJO));
        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.invalidate();
    }

    private void cargarMovimientos() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarMovimientosGrupo(sharedPreferences.getUserGroupID(), this, new MovEcoCallback() {

            @Override
            public void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos) {
                // *** CAMBIO PRINCIPAL: Actualizar la lista y notificar al adapter ***
                listaMovimientoEconomicos.clear();
                listaMovimientoEconomicos.addAll(listaMovimientos);

                // Notificar al adapter que los datos han cambiado
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                // Actualizar los datos financieros
                mostrarDatosFinancieros();
            }

            @Override
            public void onSuccessRegisteringMovimiento() {
                // No necesario aquí
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
            }
        });
    }
}