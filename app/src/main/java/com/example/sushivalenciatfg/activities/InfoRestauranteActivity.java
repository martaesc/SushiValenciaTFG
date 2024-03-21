package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoRestauranteActivity extends AppCompatActivity {

    private ImageView ivImagenRestaurante;
    private TextView tvNombreRestaurante;
    private TextView tvDescripcionRestaurante;
    private TextView tvPuntuacionRestaurante;
    private TextView tvLinkRestaurante;

    private Button btnVolver;
    private Button btnComentarios;
    private Button btnMasInfo;

    private ImageButton btnEditar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;

    private String restauranteId;

    private boolean isEditing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_restaurante);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        restauranteId = getIntent().getStringExtra("idRestaurante");
        if (restauranteId != null) {
            obtenerDatosRestaurante();
        } else {
            Log.e("InfoRestauranteActivity", "No se ha recibido el id del restaurante");
            finish();
        }

        obtenerReferencias();

        obtenerDatosRestaurante();

        habilitarBotonEditar();


        btnEditar.setOnClickListener(v -> {
            if (!isEditing) {
                habilitarEdicionTextViews();
            } else {
                guardarCambios();
            }
        });

        btnVolver.setOnClickListener(v -> volverMenu());
        btnMasInfo.setOnClickListener(v -> irAMasInfo());
    }

    public void obtenerReferencias() {
        ivImagenRestaurante = findViewById(R.id.restauranteImg);
        tvNombreRestaurante = findViewById(R.id.restauranteNombre);
        tvDescripcionRestaurante = findViewById(R.id.restauranteDescripcion);
        tvPuntuacionRestaurante = findViewById(R.id.restaurantePuntuacion);
        tvLinkRestaurante = findViewById(R.id.restauranteLink);

        btnVolver = findViewById(R.id.btnVolver);
        btnComentarios = findViewById(R.id.btnComentarios);
        btnMasInfo = findViewById(R.id.btnMasInfo);

        btnEditar = findViewById(R.id.imageButtonEditar);
    }

    public void obtenerDatosRestaurante() {
        db.collection("restaurante").document(restauranteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nombre = document.getString("nombre");
                            String descripcion = document.getString("descripcion");
                            Double puntuacion = document.getDouble("puntuacion");
                            String link = document.getString("linkRestaurante");
                            String imagenUrl = document.getString("imagenRestaurante");

                            tvNombreRestaurante.setText(nombre);
                            tvDescripcionRestaurante.setText(descripcion);
                            tvLinkRestaurante.setText(link);

                            // Comprobar si 'puntuacion' es null antes de convertirlo a String
                            if (puntuacion != null) {
                                tvPuntuacionRestaurante.setText(puntuacion.toString());
                            } else {
                                tvPuntuacionRestaurante.setText("0");
                            }


                            // Para cargar la imagen desde una URL en un ImageView usamos Glide
                            Glide.with(this)
                                    .load(imagenUrl)
                                    .into(ivImagenRestaurante);
                        } else {
                            Log.d("InfoRestauranteActivity", "No se encontró el restaurante");
                        }
                    } else {
                        Log.d("InfoRestauranteActivity", "Error al obtener restaurante", task.getException());
                    }
                });
    }


    public void habilitarBotonEditar() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // ID de usuario actual
            String userId = currentUser.getUid();
            // Buscar en la colección "usuario" un documento donde el campo "uid" coincide con el ID del usuario
            db.collection("usuario")
                    .whereEqualTo("uid", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Obtener el tipo de usuario del documento
                            String tipoUsuario = task.getResult().getDocuments().get(0).getString("tipoUsuario");
                            // Mostrar u ocultar el botón de editar según el tipo de usuario
                            if ("Restaurante".equals(tipoUsuario)) {
                                btnEditar.setVisibility(View.VISIBLE);
                            } else {
                                btnEditar.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("InfoRestauranteActivity", "No se encontró un documento de usuario con el uid: " + userId);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("InfoRestauranteActivity", "Error obteniendo el tipo de usuario", e);
                    });
        } else {
            Log.d("InfoRestauranteActivity", "El usuario actual es nulo");
        }
    }


    public void habilitarEdicionTextViews() {
        // Habilitar la edición de los TextView
        tvNombreRestaurante.setEnabled(true);
        tvDescripcionRestaurante.setEnabled(true);
        tvPuntuacionRestaurante.setEnabled(true);
        tvLinkRestaurante.setEnabled(true);

        // Cambia el icono de ediciónn al de "Guardar" para indicar que se pueden guardar los cambios
        btnEditar.setImageResource(R.drawable.icono_guardar);

        isEditing = true;
    }

    public void guardarCambios() {
        // Recoger los datos de los TextView
        String nombre = tvNombreRestaurante.getText().toString();
        String descripcion = tvDescripcionRestaurante.getText().toString();
        String puntuacion = tvPuntuacionRestaurante.getText().toString();
        String link = tvLinkRestaurante.getText().toString();

        // Actualizar el documento del restaurante en Firestore
        db.collection("restaurante").document(restauranteId)
                .update("nombre", nombre,
                        "descripcion", descripcion,
                        "puntuacion", puntuacion,
                        "linkRestaurante", link)
                .addOnSuccessListener(aVoid -> Log.d("InfoRestauranteActivity", "Restaurante actualizado con éxito"))
                .addOnFailureListener(e -> Log.w("InfoRestauranteActivity", "Error al actualizar restaurante", e));

        // Deshabilitar la edición de los TextView
        tvNombreRestaurante.setEnabled(false);
        tvDescripcionRestaurante.setEnabled(false);
        tvPuntuacionRestaurante.setEnabled(false);
        tvLinkRestaurante.setEnabled(false);

        // Cambiar el icono de guardar a editar para indicar que se pueden volver a editar los datos
        btnEditar.setImageResource(R.drawable.icono_editar);

        isEditing = false;
    }

    public void volverMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void irAMasInfo() {
        Intent intent = new Intent(this, MasInfoActivity.class);
        intent.putExtra("idRestaurante", restauranteId);
        startActivity(intent);
    }
}