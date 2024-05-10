package com.example.sushivalenciatfg.adapters;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
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

/**
 * Esta es la clase ComentarioAdapter, que extiende RecyclerView.Adapter.
 * Se utiliza para llenar los elementos de un RecyclerView con los datos de las valoraciones.
 */
public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {
    private List<Comentario> listaComentarios;
    private Context context;


    /**
     * Este es el constructor de la clase ComentarioAdapter.
     *
     * @param listaComentarios La lista de comentarios a mostrar.
     * @param context          El contexto donde se utiliza este adaptador.
     */
    public ComentarioAdapter(List<Comentario> listaComentarios, Context context) {
        this.listaComentarios = listaComentarios;
        this.context = context;
    }


    /**
     * Este método se utiliza para inflar (crear) una nueva vista para cada elemento en el RecyclerView.
     *
     * @param parent   El ViewGroup en el que se añadirá la nueva vista después de que esté vinculada a una posición de adaptador.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo ViewHolder que contiene una vista para el tipo de vista dado.
     */
    @Override
    public ComentarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // creamos la vista de cada elemento de la lista a partir del archivo de layout (item_comentario.xml)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(v);
    }


    /**
     * Este método se utiliza para vincular los datos de un comentario específico a un elemento del RecyclerView (un ViewHolder).
     *
     * @param holder   El ViewHolder que debe actualizarse para representar el contenido del elemento en la posición dada en el conjunto de datos.
     * @param position La posición del elemento en el conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(ComentarioViewHolder holder, int position) {
        // obtenemos el comentario en la posición actual de la lista de comentarios y establecemos sus datos en las vistas correspondientes del ViewHolder
        Comentario comentario = listaComentarios.get(position);
        holder.tvNombreUsuario.setText(comentario.getNombreUsuario());
        holder.rbPuntuacion.setRating(comentario.getPuntuacion());
        holder.tvComentario.setText(comentario.getTextoComentario());

        // Formateamos de la fecha (DateTimeFormatter no está disponible en la API level 24 (Android 7.0 Nougat) que es la compatible con dispositivos que ejecuten versiones de Android anteriores a la 8.0)
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = formatter.format(comentario.getFecha());
        holder.tvFecha.setText(fechaFormateada);

        // Si el usuario tiene una foto, la cargamos; de lo contrario, dejamos la foto por defecto
        if (comentario.getfotoPerfil() != null && !comentario.getfotoPerfil().isEmpty()) {
            Glide.with(holder.iv_fotoPerfil.getContext())
                    .load(comentario.getfotoPerfil())
                    .into(holder.iv_fotoPerfil);
        } else {
            holder.iv_fotoPerfil.setImageResource(R.drawable.foto_perfil_defecto);
        }


        // Cada vez que se necesita mostrar un elemento, se configuran los siguientes listeners para que actúen en función de los datos de ese elemento en particular:
        // listener para el evento de pulsar durante unos segundos sobre el item (comentario) del RecyclerView para eliminarlo
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
        //listener para el evento de pulsar sobre el item (comentario) para responderlo
        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("¿Quieres responder al comentario?")
                    .setView(R.layout.layout_dialogo_respuesta) // layout personalizado para el diálogo de respuesta
                    .setPositiveButton("Enviar", (dialog, which) -> {
                        Dialog d = (Dialog) dialog;
                        EditText etRespuesta = d.findViewById(R.id.etRespuesta);
                        String textoRespuesta = etRespuesta.getText().toString();
                        ((ComentariosActivity) v.getContext()).responderComentario(comentario.getIdComentario(), textoRespuesta); // llamamos al método responderComentario de la actividad ComentariosActivity
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });


        // Si el comentario tiene respuestas, se cargan en el RecyclerView de respuestas que se encuentra dentro del item de comentario
        if (comentario.getRespuestasRestaurante() != null) {
            RespuestaAdapter respuestaAdapter = new RespuestaAdapter(comentario.getRespuestasRestaurante());
            holder.rvRespuestas.setLayoutManager(new LinearLayoutManager(context));
            holder.rvRespuestas.setAdapter(respuestaAdapter);
        }

    }


    /**
     * Este método devuelve el tamaño total de la lista de comentarios.
     *
     * @return El número total de comentarios en la lista.
     */
    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }


    /**
     * Esta es la clase interna ComentarioViewHolder, que extiende RecyclerView.ViewHolder.
     * Se utiliza para contener las vistas que se inflan para cada elemento en el RecyclerView.
     */
    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreUsuario;
        RatingBar rbPuntuacion;
        TextView tvComentario;
        TextView tvFecha;
        CircleImageView iv_fotoPerfil;
        RecyclerView rvRespuestas;


        /**
         * Este es el constructor de la clase ComentarioViewHolder.
         *
         * @param itemView La vista de elemento de la lista.
         */
        public ComentarioViewHolder(View itemView) {
            // llamamos al constructor de la clase padre (RecyclerView.ViewHolder)
            super(itemView);
            // inicializamos las vistas del ViewHolder a partir de los IDs en el archivo de layout (item_comentario.xml)
            tvNombreUsuario = itemView.findViewById(R.id.tv_nombreUsuario);
            rbPuntuacion = itemView.findViewById(R.id.rb_puntuacion);
            tvComentario = itemView.findViewById(R.id.tv_comentario);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            iv_fotoPerfil = itemView.findViewById(R.id.iv_usuario);
            rvRespuestas = itemView.findViewById(R.id.rv_respuestas);
        }
    }
}
