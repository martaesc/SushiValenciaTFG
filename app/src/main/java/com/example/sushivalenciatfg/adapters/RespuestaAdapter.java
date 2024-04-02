package com.example.sushivalenciatfg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.models.Respuesta;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RespuestaAdapter extends RecyclerView.Adapter<RespuestaAdapter.RespuestaViewHolder> {
    private List<Respuesta> listaRespuestas;

    public RespuestaAdapter(List<Respuesta> listaRespuestas) {
        this.listaRespuestas = listaRespuestas;
    }

    @Override
    public RespuestaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_respuesta, parent, false);
        return new RespuestaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RespuestaViewHolder holder, int position) {
        Respuesta respuesta = listaRespuestas.get(position);
        holder.tvNombreUsuarioRestaurante.setText(respuesta.getIdUsuarioRestaurante());
        holder.tvTextoRespuesta.setText(respuesta.getTextoRespuesta());

        // Formateo de la fecha (DateTimeFormatter no está disponible en la API level 24 (Android 7.0 Nougat) que es la compatible con dispositivos que ejecuten versiones de Android anteriores a la 8.0)
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = formatter.format(respuesta.getFecha());
        holder.fechaRespuesta.setText(fechaFormateada);


        // Si el restaurante tiene una foto, la cargas. Si no, dejas la foto por defecto
        if (respuesta.getImagenUsuarioRestaurante() != null) {
            Glide.with(holder.iv_imagenUsuarioRestaurante.getContext())
                    .load(respuesta.getImagenUsuarioRestaurante())
                    .into(holder.iv_imagenUsuarioRestaurante);
        } else {
            holder.iv_imagenUsuarioRestaurante.setImageResource(R.drawable.foto_perfil_defecto);
        }
    }

    @Override
    public int getItemCount() {
        return listaRespuestas.size();
    }

    public static class RespuestaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreUsuarioRestaurante;
        TextView fechaRespuesta;
        CircleImageView iv_imagenUsuarioRestaurante;
        TextView tvTextoRespuesta;

        public RespuestaViewHolder(View itemView) {
            super(itemView);
            tvNombreUsuarioRestaurante = itemView.findViewById(R.id.tv_nombreUsuarioRestaurante);
            fechaRespuesta = itemView.findViewById(R.id.tv_fechaRespuesta);
            iv_imagenUsuarioRestaurante = itemView.findViewById(R.id.iv_imagenUsuarioRestaurante);
            tvTextoRespuesta = itemView.findViewById(R.id.tv_respuestaRestaurante);

        }
    }
}
