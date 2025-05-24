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
import android.widget.ImageView;
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

import com.example.matesync.AuthActivities.LoginActivity;
import com.example.matesync.BaseDatosController.ConexionBBDD;
import com.example.matesync.Manager.MenuLateralManager;
import com.example.matesync.Manager.SharedPreferencesManager;
import com.example.matesync.Modelo.Producto;
import com.example.matesync.Adapters.ProductoAdapter;
import com.example.matesync.R;

import java.util.List;

public class ListaCompraActivity extends AppCompatActivity implements MenuLateralManager.NavigationListener {
    SharedPreferencesManager sharedPreferences;
    private MenuLateralManager navManager;
    ImageView ivCrearListaCompra;
    EditText etProductoName, etProductoDescription;
    TextView tvTareas;

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
        crearMenuLateral();
        sharedPreferences = new SharedPreferencesManager(this);
        tvTareas = findViewById(R.id.tvTareas);
        tvTareas.setText("Lista de la compra de " + sharedPreferences.getNombreGrupo());
        ivCrearListaCompra = findViewById(R.id.ivCrearListaCompra);

        ivCrearListaCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearDialogoCreacionProducto();
            }
        });
        cargarProductos();
    }

    // método que crea el diálogo de creación de tarea
    private void crearDialogoCreacionProducto() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialogo_crear_producto, null);

        etProductoName = dialogView.findViewById(R.id.etProductoName);
        etProductoDescription = dialogView.findViewById(R.id.etProductoDescription);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
        );
        builder.setView(dialogView);
        builder.setTitle("Creación de producto");
        builder.setMessage("Rellene los datos del producto");

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Cierra el diálogo
            }
        });

        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreProducto = etProductoName.getText().toString().trim();
                String descripcionProducto = etProductoDescription.getText().toString().trim();

                if (!nombreProducto.isEmpty()) {
                    // Aquí procesas los datos (ej: guardar en ViewModel/BD)
                    Log.d("PRODUCTO", "Nombre: " + nombreProducto + ", Descripción: " + descripcionProducto+", grupoID: "+sharedPreferences.getUserGroupID());
                    ConexionBBDD conn = ConexionBBDD.getInstance();

                    conn.registrarProductoBBDD(new Producto(sharedPreferences.getUserUID(), sharedPreferences.getUserGroupID(), nombreProducto, descripcionProducto, 0), ListaCompraActivity.this, new ConexionBBDD.ProductoCallback() {


                        @Override
                        public void onSuccessRecoveringProductos(List<Producto> listaProductos) {

                        }

                        @Override
                        public void onSuccessRegisteringProducto() {
                            Toast.makeText(ListaCompraActivity.this, "Producto registrado correctamente", Toast.LENGTH_SHORT).show();
                            cargarProductos();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ListaCompraActivity.this, "Ha habido un error en el registro del producto", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ListaCompraActivity.this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.create();
        builder.show();

    }

    private void cargarProductos() {
        ConexionBBDD conn = ConexionBBDD.getInstance();
        conn.recuperarProductosGrupo(sharedPreferences.getUserGroupID(), this, new ConexionBBDD.ProductoCallback() {

            @Override
            public void onSuccessRecoveringProductos(List<Producto> listaProductos) {

                ProductoAdapter adapter = new ProductoAdapter(listaProductos, new ProductoAdapter.OnProductoClickListener() {
                    @Override
                    public void onProductoClick(Producto producto) {
                        // Manejar el clic en la tarea
                        Toast.makeText(ListaCompraActivity.this, "Producto seleccionada: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
                    }

                });
                //inflando el recyclerview y configurándolo

                RecyclerView recyclerView = findViewById(R.id.recyclerViewProductos);
                recyclerView.setLayoutManager(new LinearLayoutManager(ListaCompraActivity.this));
                recyclerView.setAdapter(adapter);

                // Si necesitas eliminar TODOS los espacios adicionales
                recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.set(5, 0, 0, 0); // Elimina cualquier espacio
                    }
                });
                //divisor del recycler
                Drawable dividerDrawable = ContextCompat.getDrawable(ListaCompraActivity.this, R.drawable.divider);
                DividerItemDecoration divider = new DividerItemDecoration(ListaCompraActivity.this, DividerItemDecoration.VERTICAL);
                divider.setDrawable(dividerDrawable);
                recyclerView.addItemDecoration(divider);
            }

            @Override
            public void onSuccessRegisteringProducto() {

            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
            }
        });

    }

    //crear el menú lateral
    public void crearMenuLateral() {
        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar helper
        navManager = new MenuLateralManager(this, toolbar, R.id.drawer_layout, R.id.nav_view, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        navManager.setNavigationListener(this);
    }


    // método que define la funcionalidad de cada item del menú lateral
    @Override
    public void onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_grupoDomesticoHome) {
            Intent intent = new Intent(ListaCompraActivity.this, GrupoDomHomeActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_tareas) {
            Intent intent = new Intent(ListaCompraActivity.this, TareasActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_finanzas) {
            Intent intent = new Intent(ListaCompraActivity.this, FinanzasActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_listaCompra) {
            return;
        } else if (item.getItemId() == R.id.nav_cerrarSesion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ListaCompraActivity.this);
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
                    Intent intent = new Intent(ListaCompraActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.create();
            builder.show();
        } else if (item.getItemId() == R.id.nav_inicio) {
            Intent intent = new Intent(ListaCompraActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}