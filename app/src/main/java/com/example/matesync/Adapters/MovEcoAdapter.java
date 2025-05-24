package com.example.matesync.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matesync.Modelo.MovimientoEconomico;
import com.example.matesync.R;

import java.util.List;

public class MovEcoAdapter extends RecyclerView.Adapter<MovEcoAdapter.MovEcoViewHolder>{
    private List<MovimientoEconomico> listaMovimientos;
    private MovEcoAdapter.OnMovEvClickListener listener;

    // Interfaz para manejar clicks
    public interface OnMovEvClickListener {
        void onMovimientoClick(MovimientoEconomico movimientoEconomico);
    }
    @NonNull
    @Override
    public MovEcoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista_movimientos, parent, false);
        return new MovEcoAdapter.MovEcoViewHolder(view);
    }
    // Constructor mejorado del adapter de Productos
    public MovEcoAdapter(List<MovimientoEconomico> listaMovimientos, MovEcoAdapter.OnMovEvClickListener listener) {
        this.listaMovimientos = listaMovimientos; //lista a adaptar
        this.listener = listener; // listener que se activará al hacer click en un item
    }
    @Override
    public void onBindViewHolder(@NonNull MovEcoViewHolder holder, int position) {
        MovimientoEconomico movimientoEconomico = listaMovimientos.get(position);
        holder.tvConcepto.setText(movimientoEconomico.getConcepto());
        holder.tvValor.setText(String.valueOf(movimientoEconomico.getValor())+"€");

        // Manejo de clics
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovimientoClick(movimientoEconomico);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMovimientos != null ? listaMovimientos.size() : 0;
    }

    // ViewHolder. "Reciclador" de vistas
    public static class MovEcoViewHolder extends RecyclerView.ViewHolder {
        public TextView tvConcepto, tvValor;

        public MovEcoViewHolder(View itemView) {
            super(itemView);
            tvConcepto = itemView.findViewById(R.id.tvConcepto);
            tvValor = itemView.findViewById(R.id.tvValor);
        }
    }
}
