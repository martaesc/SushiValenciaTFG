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

public class RestauranteAdapter extends RecyclerView.Adapter<RestauranteAdapter.RestauranteViewHolder> {
    private List<Restaurante> restaurantes;
    private Context context;

    public RestauranteAdapter(List<Restaurante> restaurantes, Context context) {
        this.restaurantes = restaurantes;
        this.context = context;
    }

    @NonNull
    @Override
    public RestauranteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        return new RestauranteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestauranteViewHolder holder, int position) {
        Restaurante restaurante = restaurantes.get(position);
        holder.tvNombre.setText(restaurante.getNombre());
        holder.ratingBar.setRating((float) restaurante.getPuntuacion());
        holder.tvDescripcion.setText(restaurante.getDescripcion());

        String imageUrl = restaurante.getImagenRestaurante();

        // Cargar la imagen en el ImageView con Glide
        Glide.with(context)
                .load(imageUrl)
                .into(holder.ivImagen);

        // listener para el evento de pulsar durante unos segundos sobre el item del RecyclerView para eliminar el restaurante
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

        // listener para el evento de clic en el item del RecyclerView para mostrar los datos del restaurante
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, InfoRestauranteActivity.class);
            intent.putExtra("idRestaurante", restaurante.getIdRestaurante());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantes.size();
    }

    // clase interna estática que se utiliza para contener las vistas que se inflan para cada elemento en el RecyclerView
    public static class RestauranteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        RatingBar ratingBar;
        TextView tvDescripcion;
        ImageView ivImagen;

        public RestauranteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombreRestaurante);
            ratingBar = itemView.findViewById(R.id.rb_puntuacion);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
            ivImagen = itemView.findViewById(R.id.iv_imagen_restaurante);
        }
    }
}
