package com.example.sushivalenciatfg.adapters;

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
import com.example.sushivalenciatfg.activities.MainActivity;
import com.example.sushivalenciatfg.models.Comentario;
import com.example.sushivalenciatfg.models.Respuesta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        if (comentario.getImagenUsuario() != null) {
            Glide.with(holder.iv_imagenUsuario.getContext())
                    .load(comentario.getImagenUsuario())
                    .into(holder.iv_imagenUsuario);
        } else {
            holder.iv_imagenUsuario.setImageResource(R.drawable.foto_perfil_defecto);
        }



        // listener para el evento de pulsar durante unos segundos sobre el item del RecyclerView para eliminar el comentario
        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Comentario")
                    .setMessage("¿Estás seguro de que deseas eliminar este comentario?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        ((ComentariosActivity) v.getContext()).eliminarComentario(comentario.getIdComentario());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

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
        CircleImageView iv_imagenUsuario;
        RecyclerView rvRespuestas;

        public ComentarioViewHolder(View itemView) {
            super(itemView);
            tvNombreUsuario = itemView.findViewById(R.id.tv_nombreUsuario);
            rbPuntuacion = itemView.findViewById(R.id.rb_puntuacion);
            tvComentario = itemView.findViewById(R.id.tv_comentario);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            iv_imagenUsuario = itemView.findViewById(R.id.iv_usuario);
            rvRespuestas = itemView.findViewById(R.id.rv_respuestas);
        }
    }
}
