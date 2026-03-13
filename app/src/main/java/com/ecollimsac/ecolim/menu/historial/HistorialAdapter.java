package com.ecollimsac.ecolim.menu.historial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecollimsac.ecolim.R;
import com.ecollimsac.ecolim.model.RecoleccionRegistro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private final List<RecoleccionRegistro> data;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault());
    private boolean modoAdministrador = false;
    private final Map<String, String> nombresPorUid = new HashMap<>();

    public HistorialAdapter(List<RecoleccionRegistro> data) {
        this.data = data;
    }

    public void setModoAdministrador(boolean modoAdministrador) {
        this.modoAdministrador = modoAdministrador;
    }

    public void setNombresPorUid(Map<String, String> source) {
        nombresPorUid.clear();
        if (source != null) {
            nombresPorUid.putAll(source);
        }
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial_recoleccion, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        RecoleccionRegistro item = data.get(position);
        holder.tvTitulo.setText(item.getTipoResiduo() + " - " + item.getClasificacion());

        String subtitulo = String.format(Locale.getDefault(), "%.2f Kg | Area: %s", item.getPesoKg(), item.getAreaOrigen());
        if (modoAdministrador) {
            String registradoPor = nombresPorUid.get(item.getUid());
            if (registradoPor == null || registradoPor.trim().isEmpty()) {
                registradoPor = item.getUid();
            }
            subtitulo = subtitulo + "\nRegistrado por: " + registradoPor;
        }
        holder.tvSubtitulo.setText(subtitulo);

        if (item.getCreatedAt() != null) {
            holder.tvFechaHora.setText(sdf.format(new Date(item.getCreatedAt().toDate().getTime())));
        } else {
            holder.tvFechaHora.setText(R.string.historial_fecha_proceso);
        }

        holder.tvEstado.setText(R.string.historial_estado);

        Glide.with(holder.itemView.getContext())
                .load(item.getFotoUrl())
                .placeholder(R.drawable.history2)
                .error(R.drawable.history2)
                .centerCrop()
                .into(holder.ivMiniatura);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMiniatura;
        TextView tvTitulo;
        TextView tvSubtitulo;
        TextView tvFechaHora;
        TextView tvEstado;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMiniatura = itemView.findViewById(R.id.ivMiniatura);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvSubtitulo = itemView.findViewById(R.id.tvSubtitulo);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
