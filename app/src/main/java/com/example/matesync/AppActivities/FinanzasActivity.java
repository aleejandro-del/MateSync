package com.example.matesync.AppActivities;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.Adapters.MovEcoAdapter;
import com.example.matesync.ConexionBBDD.ConexionBBDD;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.ArrayList;
import java.util.List;

public class FinanzasActivity extends AppCompatActivity {

    private MenuLateralManager navManager;
    private SharedPreferencesManager sharedPreferences;
    private MovEcoAdapter adapter;
    private PieChart pieChart;
    private EditText etMovConcepto, etMovCuantia;
    private RadioGroup rgTipoMovimiento;
    private FloatingActionButton fab;
    private TextView tvTotalIngresos, tvTotalGastos, tvBalance;

    private String tipo = "";

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Pasa sharedPreferences al MenuLateralManager
        navManager = new MenuLateralManager(
                this,
                toolbar,
                R.id.drawer_layout,
                R.id.nav_view,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close,
                sharedPreferences
        );

        // Inicializar vistas
        pieChart = findViewById(R.id.pieChart);
        tvTotalIngresos = findViewById(R.id.tvTotalIngresos);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvBalance = findViewById(R.id.tvBalance);
        fab = findViewById(R.id.btAddMovimiento);

        cargarMovimientos();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearDialogoCreacionMovimiento();
            }
        });
    }
    //método que recupera los movimientos económicos del grupo mediante consulta a Firestore
    private void cargarMovimientos() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarMovimientosGrupo(sharedPreferences.getUserGroupID(), this, new MovEcoCallback() {
            @Override
            public void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos) {
                configurarRecyclerViewMovimientos(listaMovimientos);
                mostrarDatosFinancieros(listaMovimientos);
            }

            @Override
            public void onSuccessRegisteringMovimiento() {
                // No utilizado aquí
            }

            @Override
            public void onSuccessRemovingMovimiento() {
                // No utilizado aquí
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(FinanzasActivity.this, "Algo ha salido mal...", Toast.LENGTH_SHORT).show();
            }
        });

    }
    //método para configurar el recyclerview de los movimientos económicos
    private void configurarRecyclerViewMovimientos(List<MovimientoEconomico> listaMovimientos) {
        RecyclerView recyclerView = findViewById(R.id.rvMovimientos);

        //si el adapter ya existe, solo se actualizan los datos
        if (adapter == null) {
            adapter = new MovEcoAdapter(listaMovimientos, this::manejarClickMovimiento);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            //configuro las líneas del recycler (decoración)
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.set(5, 0, 0, 0);
                }
            });
        } else {
            //solo actualizo los datos existentes
            adapter.updateMovimientos(listaMovimientos);
        }
    }
    //método para manejar la pulsación larga sobre el checkbox de un movimiento económico
    private void manejarClickMovimiento(MovimientoEconomico movimiento) {
        //mostrar dialog de confirmación antes de eliminar
        new AlertDialog.Builder(this)
                .setTitle("Eliminar movimiento")
                .setMessage("¿Estás seguro de que quieres eliminar este movimiento?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ConexionBBDD conn = ConexionBBDD.getInstance();
                    conn.borrarMovimientoEconomico(movimiento, this, new MovEcoCallback() {
                        @Override
                        public void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos) {
                            // No se utiliza
                        }

                        @Override
                        public void onSuccessRegisteringMovimiento() {
                            // No se utiliza
                        }

                        @Override
                        public void onSuccessRemovingMovimiento() {
                            runOnUiThread(() -> {
                                Toast.makeText(FinanzasActivity.this, "Movimiento eliminado correctamente", Toast.LENGTH_SHORT).show();
                                cargarMovimientos();
                                setResult(RESULT_OK);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(FinanzasActivity.this, "Error al eliminar el movimiento", Toast.LENGTH_SHORT).show();

                            });
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //método que crea el diálogo de creación de movimiento económico
    private void crearDialogoCreacionMovimiento() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialogo_crear_movimiento_financiero, null);

        etMovConcepto = dialogView.findViewById(R.id.etMovConcepto);
        etMovCuantia = dialogView.findViewById(R.id.etMovCuantia);

        //obtener referencias a los radio buttons y cards
        MaterialRadioButton rbIngreso = dialogView.findViewById(R.id.rbIngreso);
        MaterialRadioButton rbGasto = dialogView.findViewById(R.id.rbGasto);
        MaterialCardView cardIngreso = dialogView.findViewById(R.id.cardIngreso);
        MaterialCardView cardGasto = dialogView.findViewById(R.id.cardGasto);

        //configuro los listeners para las cards para que actúen como radio buttons
        cardIngreso.setOnClickListener(v -> {
            rbIngreso.setChecked(true);
            rbGasto.setChecked(false);
            actualizarEstiloCards(cardIngreso, cardGasto, true);
        });

        cardGasto.setOnClickListener(v -> {
            rbGasto.setChecked(true);
            rbIngreso.setChecked(false);
            actualizarEstiloCards(cardIngreso, cardGasto, false);
        });

        //también configuro los listeners para los radio buttons
        rbIngreso.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rbGasto.setChecked(false);
                actualizarEstiloCards(cardIngreso, cardGasto, true);
            }
        });

        rbGasto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rbIngreso.setChecked(false);
                actualizarEstiloCards(cardIngreso, cardGasto, false);
            }
        });
        //empiezo a crear el diálogo para la creación de un movimiento económico
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
        );
        builder.setView(dialogView);
        builder.setTitle("Creación de movimiento económico");
        builder.setMessage("Rellene los datos del movimiento");

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String concepto = etMovConcepto.getText().toString().trim();
            String valorString = etMovCuantia.getText().toString().trim();

            if (concepto.isEmpty() || valorString.isEmpty()) {
                Toast.makeText(FinanzasActivity.this, "Debe rellenar ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar qué radio button está seleccionado
            if (rbIngreso.isChecked()) {
                tipo = "Ingreso";
            } else if (rbGasto.isChecked()) {
                tipo = "Gasto";
            } else {
                Toast.makeText(FinanzasActivity.this, "Selecciona un tipo de movimiento", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Float valor = Float.parseFloat(valorString);
                Log.d("MOVIMIENTO", "Concepto: " + concepto + ", Valor: " + valor + ", Tipo: " + tipo);

                ConexionBBDD conn = ConexionBBDD.getInstance();
                conn.registrarMovEcoBBDD(new MovimientoEconomico(sharedPreferences.getUserUID(), sharedPreferences.getUserGroupID(), concepto, tipo, valor), FinanzasActivity.this, new MovEcoCallback() {
                            @Override
                            public void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos) {
                                // No utilizado aquí
                            }

                            @Override
                            public void onSuccessRegisteringMovimiento() {
                                Toast.makeText(FinanzasActivity.this, "Movimiento registrado correctamente", Toast.LENGTH_SHORT).show();
                                cargarMovimientos();

                                String signo = "";
                                if(signo.equals("Ingreso")){
                                    signo = "+";
                                }else if (signo.equals("Gasto")){
                                    signo = "-";
                                }
                                MainActivity.tvUltimoMovimiento.setText(String.format("Último: %s%.2f€ (%s)",
                                        signo, valor, concepto));
                                setResult(RESULT_OK);
                            }

                            @Override
                            public void onSuccessRemovingMovimiento() {
                                // No utilizado aquí
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(FinanzasActivity.this, "Ha habido un error al crear el movimiento", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (NumberFormatException e) {
                Toast.makeText(FinanzasActivity.this, "El valor debe ser un número válido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.create().show();

    }

    // método auxiliar para actualizar el estilo visual de las cards
    private void actualizarEstiloCards(MaterialCardView cardIngreso, MaterialCardView cardGasto, boolean ingresoSeleccionado) {
        if (ingresoSeleccionado) {
            cardIngreso.setStrokeColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary40));
            cardIngreso.setStrokeWidth(4);
            cardGasto.setStrokeColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_tertiary70));
            cardGasto.setStrokeWidth(2);
        } else {
            cardGasto.setStrokeColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary40));
            cardGasto.setStrokeWidth(4);
            cardIngreso.setStrokeColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_tertiary70));
            cardIngreso.setStrokeWidth(2);
        }
    }

    //método para poblar el gráfico de tarta y los 3 campos numéricos que informan de las cifras totales referente a ingresos-gastos-balance
    private void mostrarDatosFinancieros(List<MovimientoEconomico> listaMovimientos) {
        float totalIngresos = 0;
        float totalGastos = 0;

        // Recorro la lista de movimientos económicos previamente sacada de la BBDD
        for (MovimientoEconomico mov : listaMovimientos) {
            if (mov.getTipo().equals("Ingreso")) {
                totalIngresos += (float) mov.getValor();
            } else if (mov.getTipo().equals("Gasto")) {
                totalGastos += (float) mov.getValor();
            }
        }

        // Calculo el balance
        double balance = totalIngresos - totalGastos;

        // Actualizo los campos
        tvTotalIngresos.setText(totalIngresos + "€");
        tvTotalGastos.setText(totalGastos + "€");
        tvBalance.setText(balance + "€");

        // Determino las partes en las que va a estar dividido el gráfico
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totalIngresos, "Ingresos"));
        entries.add(new PieEntry(totalGastos, "Gastos"));

        // Configuro el gráfico
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getResources().getColor(R.color.VERDE), getResources().getColor(R.color.ROJO));
        PieData data = new PieData(dataSet);
        data.setValueTextSize(20);
        pieChart.setData(data);

        // Configuro la descripción
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.invalidate();
    }

}