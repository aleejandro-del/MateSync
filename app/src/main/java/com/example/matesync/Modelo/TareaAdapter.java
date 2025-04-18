package com.example.matesync.Modelo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.matesync.R;
import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private List<Tarea> listaTareas;
    private OnTareaClickListener listener;

    // Interfaz para manejar clicks
    public interface OnTareaClickListener {
        void onTareaClick(Tarea tarea);
    }

    // Constructor mejorado
    public TareaAdapter(List<Tarea> listaTareas, OnTareaClickListener listener) {
        this.listaTareas = listaTareas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = listaTareas.get(position);
        holder.tvNombre.setText(tarea.getNombre());
        holder.tvDesc.setText(tarea.getDescripcion());

        // Manejo de clics
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTareaClick(tarea);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTareas != null ? listaTareas.size() : 0;
    }

    // ViewHolder como clase estática
    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombre;
        public TextView tvDesc;

        public TareaViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
    }
}