package com.example.sushivalenciatfg.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.InfoRestauranteActivity;
import com.example.sushivalenciatfg.activities.MainActivity;
import com.example.sushivalenciatfg.models.Restaurante;

import java.util.List;

/**
 * Esta es la clase RestauranteAdapter, que extiende RecyclerView.Adapter.
 * Se utiliza para llenar los elementos de un RecyclerView con los datos de los restaurantes.
 */
public class RestauranteAdapter extends RecyclerView.Adapter<RestauranteAdapter.RestauranteViewHolder> {
    private List<Restaurante> restaurantes;
    private Context context;


    /**
     * Constructor de la clase RestauranteAdapter.
     *
     * @param restaurantes La lista de restaurantes a mostrar.
     * @param context El contexto donde se utiliza este adaptador.
     */
    public RestauranteAdapter(List<Restaurante> restaurantes, Context context) {
        this.restaurantes = restaurantes;
        this.context = context;
    }


    /**
     * Este método se utiliza para inflar (crear) una nueva vista para cada elemento en el RecyclerView.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista después de que esté vinculada a una posición de adaptador.
     * @param viewType El tipo de vista de la nueva vista.
     * @return Un nuevo ViewHolder que contiene una vista para el tipo de vista dado.
     */
    @NonNull
    @Override
    public RestauranteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflamos la vista de cada elemento de la lista a partir del archivo de layout (item_view.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);

        // devuelve una nueva instancia de RestauranteViewHolder, pasando la vista inflada como parámetro
        return new RestauranteViewHolder(view);
    }


    /**
     * Este método se utiliza para vincular los datos de un restaurante específico a un elemento del RecyclerView (un ViewHolder).
     *
     * @param holder El ViewHolder que debe actualizarse para representar el contenido del elemento en la posición dada en el conjunto de datos.
     * @param position La posición del elemento en el conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(@NonNull RestauranteViewHolder holder, int position) {
        // obtenemos el restaurante en la posición actual de la lista de restaurantes y establecemos sus datos en las vistas correspondientes del ViewHolder
        Restaurante restaurante = restaurantes.get(position);
        holder.tvNombre.setText(restaurante.getNombre());
        holder.ratingBar.setRating((float) restaurante.getPuntuacion());
        holder.tvDescripcion.setText(restaurante.getDescripcion());

        String imageUrl = restaurante.getImagenRestaurante();

        // utilizamos la biblioteca Glide para cargar la imagen del restaurante desde una URL en el ImageView
        Glide.with(context)
                .load(imageUrl)
                .into(holder.ivImagen);

        // listener para el evento de pulsar durante unos segundos sobre un item (restaurante) del RecyclerView para eliminarlo
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Restaurante")
                    .setMessage("¿Estás seguro de que deseas eliminar este restaurante?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        ((MainActivity) v.getContext()).eliminarRestaurante(restaurante.getIdRestaurante());
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        // listener para el evento de pulsar sobre un item (restaurante) del RecyclerView para pasar a la pantalla de información del restaurante
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, InfoRestauranteActivity.class);
            intent.putExtra("idRestaurante", restaurante.getIdRestaurante());
            context.startActivity(intent);
        });
    }


    /**
     * Este método devuelve el tamaño total de la lista de restaurantes.
     *
     * @return El número total de restaurantes en la lista.
     */
    @Override
    public int getItemCount() {
        return restaurantes.size();
    }


    /**
     * Esta es la clase interna RestauranteViewHolder, que extiende RecyclerView.ViewHolder.
     * Se utiliza para contener las vistas que se inflan para cada elemento en el RecyclerView.
     */
    public static class RestauranteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        RatingBar ratingBar;
        TextView tvDescripcion;
        ImageView ivImagen;


        /**
         * Constructor de la clase RestauranteViewHolder.
         *
         * @param itemView La vista de elemento de la lista.
         */
        public RestauranteViewHolder(@NonNull View itemView) {
            // llamamos al constructor de la clase padre (RecyclerView.ViewHolder)
            super(itemView);
            // inicializamos las vistas del ViewHolder a partir de sus IDs en el archivo de layout (item_view.xml)
            tvNombre = itemView.findViewById(R.id.tv_nombreRestaurante);
            ratingBar = itemView.findViewById(R.id.rb_puntuacion);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
            ivImagen = itemView.findViewById(R.id.iv_imagen_restaurante);
        }
    }
}
