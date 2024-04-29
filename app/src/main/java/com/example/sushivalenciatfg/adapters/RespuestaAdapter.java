package com.example.sushivalenciatfg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.ComentariosActivity;
import com.example.sushivalenciatfg.models.Respuesta;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Esta es la clase RespuestaAdapter, que extiende RecyclerView.Adapter.
 * Se utiliza para llenar los elementos de un RecyclerView con los datos de las respuestas.
 */
public class RespuestaAdapter extends RecyclerView.Adapter<RespuestaAdapter.RespuestaViewHolder> {
    private List<Respuesta> listaRespuestas;


    /**
     * Este es el constructor de la clase RespuestaAdapter.
     *
     * @param listaRespuestas La lista de respuestas a mostrar.
     */
    public RespuestaAdapter(List<Respuesta> listaRespuestas) {
        this.listaRespuestas = listaRespuestas;
    }


    /**
     * Este método se utiliza para inflar (crear) una nueva vista para cada elemento en el RecyclerView.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista después de que esté vinculada a una posición de adaptador.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo ViewHolder que contiene una vista para el tipo de vista dado.
     */
    @Override
    public RespuestaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflamos la vista de cada elemento de la lista a partir del archivo de layout (item_view.xml)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_respuesta, parent, false);
        return new RespuestaViewHolder(v);
    }


    /**
     * Este método se utiliza para vincular los datos de una respuesta específica a un elemento del RecyclerView (un ViewHolder).
     *
     * @param holder El ViewHolder que debe actualizarse para representar el contenido del elemento en la posición dada en el conjunto de datos.
     * @param position La posición del elemento en el conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(RespuestaViewHolder holder, int position) {
        // obtenemos la respuesta en la posición actual de la lista de respuestas y establecemos sus datos en las vistas correspondientes del ViewHolder
        Respuesta respuesta = listaRespuestas.get(position);
        holder.tvNombreUsuarioRestaurante.setText(respuesta.getNombreUsuario());
        holder.tvTextoRespuesta.setText(respuesta.getTextoRespuesta());

        // Formateamos de la fecha (DateTimeFormatter no está disponible en la API level 24 (Android 7.0 Nougat) que es la compatible con dispositivos que ejecuten versiones de Android anteriores a la 8.0)
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = formatter.format(respuesta.getFecha());
        holder.fechaRespuesta.setText(fechaFormateada);

        // Si el usuario tiene una foto, la cargamos; de lo contrario, dejamos la foto por defecto
        if (respuesta.getfotoPerfilRestaurante() != null) {
            Glide.with(holder.iv_fotoPerfilRestaurante.getContext())
                    .load(respuesta.getfotoPerfilRestaurante())
                    .into(holder.iv_fotoPerfilRestaurante);
        } else {
            holder.iv_fotoPerfilRestaurante.setImageResource(R.drawable.foto_perfil_defecto);
        }

        // listener para el evento de pulsar durante unos segundos sobre el item (respuesta) del RecyclerView para eliminarla
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Respuesta")
                    .setMessage("¿Estás seguro de que deseas eliminar esta respuesta?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        ((ComentariosActivity) v.getContext()).eliminarRespuestaEnFirestore(respuesta.getIdRespuesta());
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }


    /**
     * Este método devuelve el tamaño total de la lista de respuestas.
     *
     * @return El número total de respuestas en la lista.
     */
    @Override
    public int getItemCount() {
        return listaRespuestas.size();
    }


    /**
     * Esta es la clase interna RespuestaViewHolder, que extiende RecyclerView.ViewHolder.
     * Se utiliza para contener las vistas que se inflan para cada elemento en el RecyclerView.
     */
    public static class RespuestaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreUsuarioRestaurante;
        TextView fechaRespuesta;
        CircleImageView iv_fotoPerfilRestaurante;
        TextView tvTextoRespuesta;


        /**
         * Este es el constructor de la clase RespuestaViewHolder.
         *
         * @param itemView La vista de elemento de la lista.
         */
        public RespuestaViewHolder(View itemView) {
            super(itemView);
            tvNombreUsuarioRestaurante = itemView.findViewById(R.id.tv_nombreUsuarioRestaurante);
            fechaRespuesta = itemView.findViewById(R.id.tv_fechaRespuesta);
            iv_fotoPerfilRestaurante = itemView.findViewById(R.id.iv_fotoPerfilRestaurante);
            tvTextoRespuesta = itemView.findViewById(R.id.tv_respuestaRestaurante);

        }
    }
}
