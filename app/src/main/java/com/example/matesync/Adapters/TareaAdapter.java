package com.example.matesync.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.AppActivities.MainActivity;
import com.example.matesync.Modelo.Tarea;
import com.example.matesync.R;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private List<Tarea> listaTareas;
    private TareaAdapter.OnTareaClickListener listener;

    // Interfaz para manejar clicks
    public interface OnTareaClickListener {
        void onGestionarTareaClick(Tarea tarea);
    }

    // Constructor mejorado del adapter de Tareas
    public TareaAdapter(List<Tarea> listaTareas, TareaAdapter.OnTareaClickListener listener) {
        this.listaTareas = listaTareas; //lista a adaptar
        this.listener = listener; // listener que se activará al hacer click en un item
    }


    //método que infla cada item de la lista
    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    //llena los datos de una tarea en una fila.
    @Override
    public void onBindViewHolder(@NonNull TareaAdapter.TareaViewHolder holder, int position) {
        Tarea tarea = listaTareas.get(position);
        holder.tvNombre.setText(tarea.getNombre());
        holder.tvDesc.setText(tarea.getDescripcion());

        // Mostrar icono de gestión solo si es admin
        if (MainActivity.USUARIO != null ) {
            holder.btGestionarTarea.setVisibility(View.VISIBLE);
        } else {
            holder.btGestionarTarea.setVisibility(View.GONE);
        }

        // Resetear el estado del checkbox para evitar problemas de reciclaje
        holder.btGestionarTarea.setOnCheckedChangeListener(null); // Eliminar listener temporalmente
        holder.btGestionarTarea.setChecked(false); // Desmarcar por defecto
        holder.btGestionarTarea.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && listener != null) {
                listener.onGestionarTareaClick(tarea);
                buttonView.setChecked(false); // Desmarcar inmediatamente
            }
        });
    }
    public void updateTareas(List<Tarea> nuevasTareas) {
        this.listaTareas.clear();
        this.listaTareas.addAll(nuevasTareas);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return listaTareas != null ? listaTareas.size() : 0;
    }

    // ViewHolder. "Reciclador" de vistas
    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombre, tvDesc;
        public MaterialCheckBox btGestionarTarea;

        public TareaViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btGestionarTarea = itemView.findViewById(R.id.cbBut);

        }
    }
}