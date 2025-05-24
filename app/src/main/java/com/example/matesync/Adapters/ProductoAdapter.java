package com.example.matesync.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.Modelo.Producto;
import com.example.matesync.R;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>{

    private List<Producto> listaProductos;
    private ProductoAdapter.OnProductoClickListener listener;

    // Interfaz para manejar clicks
    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    // Constructor mejorado del adapter de Productos
    public ProductoAdapter(List<Producto> listaProductos, ProductoAdapter.OnProductoClickListener listener) {
        this.listaProductos = listaProductos; //lista a adaptar
        this.listener = listener; // listener que se activará al hacer click en un item
    }


    //método que infla cada item de la lista
    @NonNull
    @Override
    public ProductoAdapter.ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_producto, parent, false);
        return new ProductoAdapter.ProductoViewHolder(view);
    }
    //llena los datos de un producto en una fila.
    @Override
    public void onBindViewHolder(@NonNull ProductoAdapter.ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.tvNombre.setText(producto.getNombre());
        holder.tvDesc.setText(producto.getDescripcion());

        // Manejo de clics
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductoClick(producto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    // ViewHolder. "Reciclador" de vistas
    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombre;
        public TextView tvDesc;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvDesc = itemView.findViewById(R.id.tvDescProducto);
        }
    }
}
