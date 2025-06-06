package com.example.matesync.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.AppActivities.MainActivity;
import com.example.matesync.Modelo.Usuario;
import com.example.matesync.R;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {
    private List<Usuario> listaUsuarios;
    private OnUsuarioClickListener listener;

    // Interfaz para manejar clicks
    public interface OnUsuarioClickListener {
        void onGestionarUsuarioClick(Usuario usuario);
    }

    // Constructor
    public UsuarioAdapter(List<Usuario> listaUsuarios, OnUsuarioClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_miembros, parent, false);
        return new UsuarioViewHolder(view, listener, listaUsuarios);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.tvNombreMiembro.setText(usuario.getNombre());
        holder.tvEmail.setText(usuario.getEmail());

        // Show admin indicator if user is admin
        if (usuario.isAdmin()) {
            holder.tvRolMiembro.setText("Administrador");
        } else {
            holder.tvRolMiembro.setText("Miembro");
        }
        if(MainActivity.USUARIO.isAdmin()){
            holder.btGestionarUser.setVisibility(View.VISIBLE);
        }else{
            holder.btGestionarUser.setVisibility(View.GONE);
        }

        holder.btGestionarUser.setOnClickListener(v -> {

            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onGestionarUsuarioClick(listaUsuarios.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }

    // ViewHolder como clase estática
    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombreMiembro, tvEmail, tvRolMiembro;
        public ImageView btGestionarUser;
        private OnUsuarioClickListener listener;
        private List<Usuario> listaUsuarios;

        public UsuarioViewHolder(View itemView, OnUsuarioClickListener listener, List<Usuario> listaUsuarios) {
            super(itemView);
            this.listener = listener;
            this.listaUsuarios = listaUsuarios;

            tvNombreMiembro = itemView.findViewById(R.id.tvNombreMiembro);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRolMiembro = itemView.findViewById(R.id.tvRolMiembro);
            btGestionarUser = itemView.findViewById(R.id.btGestionarUser); // Fixed missing semicolon


        }
    }
}