package com.example.sushivalenciatfg.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.ComentariosActivity;
import com.example.sushivalenciatfg.models.Comentario;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {
    private List<Comentario> listaComentarios;
    private Context context;

    public ComentarioAdapter(List<Comentario> listaComentarios, Context context) {
        this.listaComentarios = listaComentarios;
        this.context = context;
    }

    @Override
    public ComentarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ComentarioViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);
        holder.tvNombreUsuario.setText(comentario.getNombreUsuario());
        holder.rbPuntuacion.setRating(comentario.getPuntuacion());
        holder.tvComentario.setText(comentario.getTextoComentario());

        // Formateo de la fecha (DateTimeFormatter no está disponible en la API level 24 (Android 7.0 Nougat) que es la compatible con dispositivos que ejecuten versiones de Android anteriores a la 8.0)
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = formatter.format(comentario.getFecha());
        holder.tvFecha.setText(fechaFormateada);

        // Si el usuario tiene una foto, la cargas. Si no, dejas la foto por defecto
        if (comentario.getfotoPerfil() != null) {
            Glide.with(holder.iv_fotoPerfil.getContext())
                    .load(comentario.getfotoPerfil())
                    .into(holder.iv_fotoPerfil);
        } else {
            holder.iv_fotoPerfil.setImageResource(R.drawable.foto_perfil_defecto);
        }


        // listener para el evento de pulsar durante unos segundos sobre el item del RecyclerView para eliminar el comentario
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Comentario")
                    .setMessage("¿Estás seguro de que deseas eliminar este comentario?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        ((ComentariosActivity) v.getContext()).eliminarComentarioDeFirestore(comentario.getIdComentario());
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        //listener para el evento de pulsar sobre el item del comentario para responder
        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("¿Quieres responder al comentario?")
                    .setView(R.layout.layout_dialogo_respuesta) // layout personalizado para el diálogo de respuesta
                    .setPositiveButton("Enviar", (dialog, which) -> {
                        Dialog d = (Dialog) dialog;
                        EditText etRespuesta = d.findViewById(R.id.etRespuesta); // EditText en el layout personalizado
                        String textoRespuesta = etRespuesta.getText().toString();
                        ((ComentariosActivity) v.getContext()).responderComentario(comentario.getIdComentario(), textoRespuesta);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Si el comentario tiene respuestas, se cargan en el RecyclerView
        if (comentario.getRespuestasRestaurante() != null) {
            RespuestaAdapter respuestaAdapter = new RespuestaAdapter(comentario.getRespuestasRestaurante());
            holder.rvRespuestas.setLayoutManager(new LinearLayoutManager(context));
            holder.rvRespuestas.setAdapter(respuestaAdapter);
        }

    }


    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreUsuario;
        RatingBar rbPuntuacion;
        TextView tvComentario;
        TextView tvFecha;
        CircleImageView iv_fotoPerfil;
        RecyclerView rvRespuestas;

        public ComentarioViewHolder(View itemView) {
            super(itemView);
            tvNombreUsuario = itemView.findViewById(R.id.tv_nombreUsuario);
            rbPuntuacion = itemView.findViewById(R.id.rb_puntuacion);
            tvComentario = itemView.findViewById(R.id.tv_comentario);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            iv_fotoPerfil = itemView.findViewById(R.id.iv_usuario);
            rvRespuestas = itemView.findViewById(R.id.rv_respuestas);
        }
    }
}
