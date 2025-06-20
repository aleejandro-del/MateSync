package com.example.matesync.AppActivities;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import com.example.matesync.ConexionBBDD.ConexionBBDD;
import com.example.matesync.Callbacks.ProductoCallback;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.Producto;
import com.example.matesync.Adapters.ProductoAdapter;
import com.example.matesync.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class ListaCompraActivity extends AppCompatActivity {
    private SharedPreferencesManager sharedPreferences;
    private MenuLateralManager navManager;
    private EditText etProductoName, etProductoDescription, etProductoCantidad;
    private TextView tvListaCompra;
    private ExtendedFloatingActionButton fab;
    private ProductoAdapter adapter; // Mantener referencia al adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listacompra);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarComponentes();
        configurarListeners();
        cargarProductos();
    }


    private void inicializarComponentes() {
        sharedPreferences = new SharedPreferencesManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navManager = new MenuLateralManager(
                this,
                toolbar,
                R.id.drawer_layout,
                R.id.nav_view,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close,
                sharedPreferences
        );

        fab = findViewById(R.id.btAddProducto);
        tvListaCompra = findViewById(R.id.tvListaCompra);
        tvListaCompra.setText("Lista de la compra de " + sharedPreferences.getNombreGrupo());
    }

    private void configurarListeners() {
        fab.setOnClickListener(v -> crearDialogoCreacionProducto());
    }
    //método para crear el diálogo de creación de productos
    private void crearDialogoCreacionProducto() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialogo_crear_producto, null);

        etProductoName = dialogView.findViewById(R.id.etProductoName);
        etProductoDescription = dialogView.findViewById(R.id.etProductoDescription);
        etProductoCantidad = dialogView.findViewById(R.id.etProductoCantidad);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
        );

        builder.setView(dialogView)
                .setTitle("Creación de producto")
                .setMessage("Rellene los datos del producto")
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombreProducto = "";
                    String descripcionProducto = "";
                    long cantidad = 0;
                    try {
                        nombreProducto = etProductoName.getText().toString().trim();
                        descripcionProducto= etProductoDescription.getText().toString().trim();
                        cantidad = Long.parseLong(etProductoCantidad.getText().toString().trim());
                    }catch (Exception e){
                        Toast.makeText(this, "Necesitas introducir los datos obligatorios", Toast.LENGTH_SHORT).show();
                    }


                    if (!nombreProducto.isEmpty() && cantidad > 0) {
                        registrarProducto(nombreProducto, descripcionProducto, cantidad);
                    } else {
                        Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }
    //método para registrar un producto en la base de datos
    private void registrarProducto(String nombre, String descripcion, long cantidad) {
        Log.d("PRODUCTO", "Nombre: " + nombre + ", Descripción: " + descripcion + ", grupoID: " + sharedPreferences.getUserGroupID());

        Producto nuevoProducto = new Producto(
                sharedPreferences.getUserUID(),
                sharedPreferences.getUserGroupID(),
                nombre,
                descripcion,
                cantidad
        );

        ConexionBBDD.getInstance().registrarProductoBBDD(nuevoProducto, this, new ProductoCallback() {
                    @Override
                    public void onSuccessRecoveringProductos(List<Producto> listaProductos) {
                        cargarProductos();
                    }

                    @Override
                    public void onSuccessRegisteringProducto() {
                        Toast.makeText(ListaCompraActivity.this, "Producto registrado correctamente", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccessRemovingProducto() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ListaCompraActivity.this, "Ha habido un error en el registro del producto", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    //método que recupera los productos del grupo mediante consulta a Firestore
    private void cargarProductos() {
        ConexionBBDD.getInstance().recuperarProductosGrupo(
                sharedPreferences.getUserGroupID(),
                this,
                new ProductoCallback() {
                    @Override
                    public void onSuccessRecoveringProductos(List<Producto> listaProductos) {
                        configurarRecyclerView(listaProductos);
                    }

                    @Override
                    public void onSuccessRegisteringProducto() {
                        // No utilizado aquí
                    }

                    @Override
                    public void onSuccessRemovingProducto() {

                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ListaCompraActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
                        Log.e("ListaCompra", "Error al cargar productos", e);
                    }
                }
        );
    }

    //método para configurar el recyclerview de los productos
    private void configurarRecyclerView(List<Producto> listaProductos) {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProductos);

        //si el adapter ya existe, solo actualizo sus datos
        if (adapter == null) {
            adapter = new ProductoAdapter(listaProductos, this::manejarClickProducto);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            //configuro las decoraciones
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.set(5, 0, 0, 0);
                }
            });
        } else {
            //si el adapter ya existe, solo actualizar los datos existentes
            adapter.updateProductos(listaProductos); // Necesitarás añadir este método al ProductoAdapter
        }
    }

    //método para manejar la pulsación larga sobre el checkbox de un producto
    private void manejarClickProducto(Producto producto) {
        //mostrar dialog de confirmación antes de eliminar
        new AlertDialog.Builder(this).setTitle("Marcar como comprado").setMessage("¿Confirma que ha comprado \"" + producto.getNombre() + "\"?").setPositiveButton("Sí, comprado", (dialog, which) -> {
                    ConexionBBDD conn = ConexionBBDD.getInstance();
                    conn.borrarProducto(producto, new ProductoCallback() {
                        @Override
                        public void onSuccessRecoveringProductos(List<Producto> listaProductos) {
                            //no utilizado aquí
                        }

                        @Override
                        public void onSuccessRegisteringProducto() {
                            //no utilizado aquí
                        }

                        //si hay éxito al borrar, se vuelven a cargar los productos
                        @Override
                        public void onSuccessRemovingProducto() {
                            Toast.makeText(ListaCompraActivity.this, "Producto borrado correctamente", Toast.LENGTH_SHORT).show();
                            cargarProductos();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ListaCompraActivity.this, "Error al borrar producto", Toast.LENGTH_SHORT).show();
                            Log.e("ListaCompra", "Error al borrar producto", e);
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}