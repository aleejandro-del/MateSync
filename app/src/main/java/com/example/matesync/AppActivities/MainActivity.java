package com.example.matesync.AppActivities;


import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.AuthActivities.LoginActivity;
import com.example.matesync.AuthActivities.RegisterActivity;
import com.example.matesync.ConexionBBDD.ConexionBBDD;
import com.example.matesync.Callbacks.MovEcoCallback;
import com.example.matesync.Callbacks.ProductoCallback;
import com.example.matesync.Callbacks.TareaCallback;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;

import com.example.matesync.Modelo.MovimientoEconomico;
import com.example.matesync.Modelo.Producto;
import com.example.matesync.Adapters.ProductoAdapter;
import com.example.matesync.Modelo.Tarea;

import com.example.matesync.Adapters.TareaAdapter;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.R;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SharedPreferencesManager sharedPreferences;
    private MenuLateralManager navManager;
    private TextView tvBalanceMain;
    public static TextView tvUltimoMovimiento;
    public static Usuario USUARIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarComponentes();
        verificarEstadoUsuario();
        cargarDatos();
    }

    //método que inicializa los componentes
    private void inicializarComponentes() {
        sharedPreferences = new SharedPreferencesManager(this);
        configurarToolbar();
        configurarMenuLateral();
        tvBalanceMain = findViewById(R.id.tvBalanceMain);
        tvUltimoMovimiento = findViewById(R.id.tvUltimoMovimiento);
        USUARIO = crearUsuarioDesdeSharedPreferences();
        logSharedPreferences();
    }

    //método para configurar la toolbar
    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    //método para configurar e inicializar el menú lateral
    private void configurarMenuLateral() {
        navManager = new MenuLateralManager(
                this,
                findViewById(R.id.toolbar),
                R.id.drawer_layout,
                R.id.nav_view,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close,
                sharedPreferences
        );
    }

    //método que crea el usuario actual cogiendo los datos desde el sharedpreferences cada vez que se abre la app
    private Usuario crearUsuarioDesdeSharedPreferences() {
        return new Usuario(sharedPreferences.getUserUID(), sharedPreferences.getUserName(), sharedPreferences.getUserGroupID(), sharedPreferences.getIsAdmin(), sharedPreferences.getUserEmail()
        );
    }

    private void logSharedPreferences() {
        Log.d("SharedPreferences", "email: " + sharedPreferences.getUserEmail());
        Log.d("SharedPreferences", "userUID: " + sharedPreferences.getUserUID());
        Log.d("SharedPreferences", "nombre: " + sharedPreferences.getUserName());
        Log.d("SharedPreferences", "isRegistered: " + sharedPreferences.getIsRegistered());
        Log.d("SharedPreferences", "isLogged: " + sharedPreferences.getIsLogged());
        Log.d("SharedPreferences", "UserGroupID: " + sharedPreferences.getUserGroupID());
        Log.d("SharedPreferences", "nombreGrupo: " + sharedPreferences.getNombreGrupo());
    }

    //método que verifica el estado del usuario y lo redirige a la actividad correspondiente
    private void verificarEstadoUsuario() {
        if (sharedPreferences.getUserUID().isEmpty()) {
            redirigirA(RegisterActivity.class);
        } else if (sharedPreferences.getUserGroupID().isEmpty()) {
            redirigirA(AccederGrupoDomActivity.class);
        } else if (!sharedPreferences.getIsRegistered()) {
            redirigirA(RegisterActivity.class);
        } else if (!sharedPreferences.getIsLogged()) {
            redirigirA(LoginActivity.class);
        }
    }

    //método que redirige a cualqueir .class (es para navegación)
    private void redirigirA(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
        finish();
    }

    //método que encapsula los métodos de cargar datos de los 3 módulos en el main
    private void cargarDatos() {
        cargarTareas();
        cargarProductos();
        refrescarResumenFinanciero();
    }

    //método que recoge las tareas existentes en el grupo doméstico consultando Firestore
    private void cargarTareas() {
        ConexionBBDD.getInstance().recuperarTareasGrupo(sharedPreferences.getUserGroupID(),
                new TareaCallback() {
                    @Override
                    public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {
                        configurarRecyclerViewTareas(listaTareas);
                    }

                    @Override
                    public void onSuccessRegisteringTarea() {
                        //no se utiliza aquí
                    }

                    @Override
                    public void onSuccessRemovingTarea() {
                        //no se utiliza aquí
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "Algo ha salido mal...", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    //método que inicializa el recyclerview correspondiente a las tareas
    private void configurarRecyclerViewTareas(List<Tarea> tareas) {
        TareaAdapter adapter = new TareaAdapter(tareas, this::manejarClickTarea);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTareas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(5, 0, 0, 0);
            }
        });
    }

    //método que maneja el click en el checkbox de una tarea con diálogo de confirmación
    private void manejarClickTarea(Tarea tarea) {
        new AlertDialog.Builder(this)
                .setTitle("Completar tarea")
                .setMessage("¿Está seguro de que desea marcar como completada la tarea \"" + tarea.getNombre() + "\"?")
                .setPositiveButton("Sí, completar", (dialog, which) -> {
                    eliminarTarea(tarea);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //método que elimina la tarea después de la confirmación
    private void eliminarTarea(Tarea tarea) {
        ConexionBBDD.getInstance().borrarTarea(tarea, new TareaCallback() {
                    @Override
                    public void onSuccessRecoveringTareas(List<Tarea> listaTareas) {
                        //no utilizado aquí
                    }

                    @Override
                    public void onSuccessRegisteringTarea() {
                        //no utilizado aquí
                    }

                    @Override
                    public void onSuccessRemovingTarea() {
                        Toast.makeText(MainActivity.this, "Tarea completada correctamente", Toast.LENGTH_SHORT).show();
                        cargarTareas();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "Error al completar tarea", Toast.LENGTH_SHORT).show();
                        Log.e("TareaError", "Error al borrar tarea", e);
                    }
                }
        );
    }

    //método que recoge los productos existentes en el grupo doméstico consultando Firestore
    private void cargarProductos() {
        ConexionBBDD.getInstance().recuperarProductosGrupo(sharedPreferences.getUserGroupID(), this, new ProductoCallback() {
                    @Override
                    public void onSuccessRecoveringProductos(List<Producto> listaProductos) {
                        configurarRecyclerViewProductos(listaProductos);
                    }

                    @Override
                    public void onSuccessRegisteringProducto() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onSuccessRemovingProducto() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("ProductosError", "Error al cargar productos", e);
                    }
                }
        );
    }

    //método que inicializa el recyclerview correspondiente a los productos
    private void configurarRecyclerViewProductos(List<Producto> productos) {
        ProductoAdapter adapter = new ProductoAdapter(productos, this::manejarClickProducto);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProductos);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(5, 0, 0, 0);
            }
        });
    }

    //método que maneja el click en el checkbox de un producto con diálogo de confirmación
    private void manejarClickProducto(Producto producto) {
        new AlertDialog.Builder(this)
                .setTitle("Marcar como comprado")
                .setMessage("¿Confirma que ha comprado \"" + producto.getNombre() + "\"?")
                .setPositiveButton("Sí, comprado", (dialog, which) -> {
                    eliminarProducto(producto);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //método que elimina el producto después de la confirmación
    private void eliminarProducto(Producto producto) {
        ConexionBBDD.getInstance().borrarProducto(producto, new ProductoCallback() {
                    @Override
                    public void onSuccessRecoveringProductos(List<Producto> listaProductos) {
                        // No utilizado aquí
                    }

                    @Override
                    public void onSuccessRegisteringProducto() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onSuccessRemovingProducto() {
                        Toast.makeText(MainActivity.this, "Producto marcado como comprado", Toast.LENGTH_SHORT).show();
                        cargarProductos();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "Error al marcar producto como comprado", Toast.LENGTH_SHORT).show();
                        Log.e("ProductoError", "Error al borrar producto", e);
                    }
                }
        );
    }

    //método que maneja el click en el checkbox de un movimiento económico
    private void cargarResumenFinanciero() {
        ConexionBBDD.getInstance().recuperarMovimientosGrupo(sharedPreferences.getUserGroupID(), this, new MovEcoCallback() {
                    @Override
                    public void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos) {
                        actualizarResumenFinanciero(listaMovimientos);
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
                        Log.e("FinanzasError", "Error al cargar movimientos financieros", e);
                        // Mostrar valores por defecto en caso de error
                        tvBalanceMain.setText("Balance: 0€");
                        tvUltimoMovimiento.setText("Último: -");
                    }
                }
        );
    }

    //método para actualizar el resumen financiero
    private void actualizarResumenFinanciero(List<MovimientoEconomico> listaMovimientos) {
        float totalIngresos = 0;
        float totalGastos = 0;
        MovimientoEconomico ultimoMovimiento = null;

        // Recorro el arraylist de movimientos económicos y voy sumando los ingresos y los gastos
        for (MovimientoEconomico mov : listaMovimientos) {
            if (mov.getTipo().equals("Ingreso")) {
                totalIngresos += (float) mov.getValor();
            } else if (mov.getTipo().equals("Gasto")) {
                totalGastos += (float) mov.getValor();
            }
        }

        //cojo el último movimiento de la lista (que sería el último añadido)
        if (!listaMovimientos.isEmpty()) {
            ultimoMovimiento = listaMovimientos.get(listaMovimientos.size() - 1);
        }

        //calculo el balance total
        double balance = totalIngresos - totalGastos;

        //actualizo el textview que muestra el balance
        tvBalanceMain.setText(String.format("Balance: %.2f€", balance));

        //aplico el color al textview que muestra el balance
        aplicarColorBalance(balance);

        //actualizo el último movimiento
        if (ultimoMovimiento != null) {
            String signo = ultimoMovimiento.getTipo().equals("Ingreso") ? "+" : "-";
            tvUltimoMovimiento.setText(String.format("Último: %s%.2f€ (%s)",
                    signo, ultimoMovimiento.getValor(), ultimoMovimiento.getConcepto()));
        } else {
            tvUltimoMovimiento.setText("Último: -");
        }
    }

    // método que aplica los colores al card del balance dependiendo del número que sea
    private void aplicarColorBalance(double balance) {
        int color;

        if (balance < 0) {
            // balance negativo (rojo)
            color = ContextCompat.getColor(this, R.color.rojo_balance_negativo);
        } else if (balance >= 0 && balance <= 50) {
            // balance cercano a 0 (entre 0 y 50) (naranja)
            color = ContextCompat.getColor(this, R.color.naranja_balance_neutral);
        } else {
            // balance positivo y lejano de 0 (verde)
            color = ContextCompat.getColor(this, R.color.verde_balance_positivo);
        }

        tvBalanceMain.setTextColor(color);
    }

    // método que refresca el resumen financiero
    public void refrescarResumenFinanciero() {
        cargarResumenFinanciero();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(() ->  cargarResumenFinanciero());
       ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cargarResumenFinanciero();
    }
}