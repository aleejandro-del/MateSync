package com.example.matesync.Modelo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.R;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>{
    private List<Usuario> listaUsuarios;
    private OnUsuarioClickListener listener;

    // Interfaz para manejar clicks
    public interface OnUsuarioClickListener {
        void onMiembroClick(Usuario usuario);
    }

    // Constructor mejorado
    public UsuarioAdapter(List<Usuario> listaUsuarios, OnUsuarioClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_miembros, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.tvNombreMiembro.setText(usuario.getNombre());
        holder.tvEmail.setText(usuario.getEmail());

        // Manejo de clics
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMiembroClick(usuario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }

    // ViewHolder como clase estática
    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombreMiembro;
        public TextView tvEmail;

        public UsuarioViewHolder(View itemView) {
            super(itemView);
            tvNombreMiembro = itemView.findViewById(R.id.tvNombreMiembro);
            tvEmail = itemView.findViewById(R.id.tvEmail);

        }
    }
}
