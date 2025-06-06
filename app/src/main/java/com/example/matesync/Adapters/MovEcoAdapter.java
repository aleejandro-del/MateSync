package com.example.matesync.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.Modelo.MovimientoEconomico;
import com.example.matesync.R;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

public class MovEcoAdapter extends RecyclerView.Adapter<MovEcoAdapter.MovEcoViewHolder> {
    private List<MovimientoEconomico> listaMovimientos;
    private OnMovEvClickListener listener;

    public interface OnMovEvClickListener {
        void onGestionarMovEco(MovimientoEconomico movimientoEconomico);
    }

    public MovEcoAdapter(List<MovimientoEconomico> listaMovimientos, OnMovEvClickListener listener) {
        this.listaMovimientos = listaMovimientos != null ? listaMovimientos : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovEcoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_movimientos, parent, false);
        return new MovEcoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovEcoViewHolder holder, int position) {
        MovimientoEconomico movimientoEconomico = listaMovimientos.get(position);

        // Configurar los textos
        holder.tvConcepto.setText(movimientoEconomico.getConcepto());
        holder.tvValor.setText(String.valueOf(movimientoEconomico.getValor()) + "€");

        // Configurar el tipo de movimiento en los detalles
        holder.tvDetalles.setText(movimientoEconomico.getTipo());

        // Resetear el estado del checkbox para evitar problemas de reciclaje
        holder.btGestionarMovEco.setChecked(false);

        // Configurar el listener del checkbox
        holder.btGestionarMovEco.setOnCheckedChangeListener(null); // Limpiar listener anterior
        holder.btGestionarMovEco.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onGestionarMovEco(listaMovimientos.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMovimientos.size();
    }

    // Método para actualizar la lista completa
    public void updateMovimientos(List<MovimientoEconomico> nuevaLista) {
        this.listaMovimientos.clear();
        this.listaMovimientos.addAll(nuevaLista);
        notifyDataSetChanged();
    }


    public static class MovEcoViewHolder extends RecyclerView.ViewHolder {
        public TextView tvConcepto, tvValor, tvDetalles;
        public MaterialCheckBox btGestionarMovEco;

        public MovEcoViewHolder(View itemView) {
            super(itemView);

            tvConcepto = itemView.findViewById(R.id.tvConcepto);
            tvValor = itemView.findViewById(R.id.tvValor);
            tvDetalles = itemView.findViewById(R.id.tvDetalles);
            btGestionarMovEco = itemView.findViewById(R.id.cbMovEco);
        }
    }
}