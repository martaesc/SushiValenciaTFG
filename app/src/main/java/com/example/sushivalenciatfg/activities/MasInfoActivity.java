package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MasInfoActivity extends AppCompatActivity {

    private EditText etTelefono;
    private EditText etHorario;
    private EditText etDireccion;

    private TextView tvTelefono;
    private TextView tvHorario;
    private TextView tvDireccion;

    private Button btnEditar;
    private Button btnVolver;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;

    private String restauranteId;

    private boolean isEditing = false;

    private String errorMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mas_info);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //recoger el id del restaurante que se ha pasado desde InfoRestauranteActivity
        restauranteId = getIntent().getStringExtra("idRestaurante");
        if (restauranteId != null) {
            obtenerDatosRestaurante();
        } else {
            Log.e("InfoRestauranteActivity", "No se ha recibido el id del restaurante");
            finish();
        }


        obtenerReferencias();

        habilitarBotonEditar();

        btnEditar.setOnClickListener(v -> edicion());

        tvTelefono.setOnClickListener(v -> llamarTelefono());

        tvDireccion.setOnClickListener(v -> abrirMapa());

        btnVolver.setOnClickListener(v -> volver());
    }

    public void obtenerReferencias() {
        etTelefono = findViewById(R.id.etTelefono);
        etHorario = findViewById(R.id.etHorario);
        etDireccion = findViewById(R.id.etDireccion);

        tvTelefono = findViewById(R.id.tvTelefono);
        tvHorario = findViewById(R.id.tvHorario);
        tvDireccion = findViewById(R.id.tvDireccion);


        btnEditar = findViewById(R.id.btnEditarInfo);
        btnVolver = findViewById(R.id.btnVolverInfoActivity);
    }


    public void obtenerDatosRestaurante() {
        db.collection("restaurante").document(restauranteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String telefono = document.getString("telefono");
                            String horario = document.getString("horario");
                            String direccion = document.getString("direccion");

                            tvTelefono.setText(telefono);
                            tvHorario.setText(horario);
                            tvDireccion.setText(direccion);
                        } else {
                            Log.d("MasInfoActivity", "No se encontró el restaurante");
                        }
                    } else {
                        Log.d("MasInfoActivity", "Error al obtener restaurante", task.getException());
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
                            // Si el tipo de usuario es "Restaurante", entonces comprobar si es el creador del restaurante
                            if ("Restaurante".equals(tipoUsuario)) {
                                // Buscar en la colección "restaurante" un documento donde el campo "creador" coincide con el ID del usuario
                                db.collection("restaurante")
                                        .whereEqualTo("idUsuarioRestaurante", userId)
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                                // Si el usuario es el creador del restaurante, mostrar el botón de editar
                                                btnEditar.setVisibility(View.VISIBLE);
                                            } else {
                                                // Si el usuario no es el creador del restaurante, ocultar el botón de editar
                                                btnEditar.setVisibility(View.GONE);
                                            }
                                        });
                            } else {
                                // Si el tipo de usuario no es "Restaurante", ocultar el botón de editar
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

    public void habilitarEdicionEditText() {
        // Copiar el texto de los TextView a los EditText
        etTelefono.setText(tvTelefono.getText().toString());
        etHorario.setText(tvHorario.getText().toString());
        etDireccion.setText(tvDireccion.getText().toString());

        // Hacer que el TextView sea invisible y que el EditText sea visible
        tvTelefono.setVisibility(View.GONE);
        etTelefono.setVisibility(View.VISIBLE);
        tvHorario.setVisibility(View.GONE);
        etHorario.setVisibility(View.VISIBLE);
        tvDireccion.setVisibility(View.GONE);
        etDireccion.setVisibility(View.VISIBLE);

        // Cambiar el texto del botón a "Guardar" mientras estamos en modo edición
        btnEditar.setText("Guardar");

        isEditing = true;
    }

    public void deshabilitarEdicionEditText() {
        // Hacer que el EditText sea invisible y que el TextView sea visible
        etTelefono.setVisibility(View.GONE);
        tvTelefono.setVisibility(View.VISIBLE);
        etHorario.setVisibility(View.GONE);
        tvHorario.setVisibility(View.VISIBLE);
        etDireccion.setVisibility(View.GONE);
        tvDireccion.setVisibility(View.VISIBLE);

        isEditing = false;

        // Cambiar el texto del botón a "Editar" después de guardar
        btnEditar.setText("Editar");
    }

    private void comprobacionCamposYGuardar() {
        // Recoger los datos de los EditText
        String telefono = etTelefono.getText().toString();
        String horario = etTelefono.getText().toString();
        String direccion = etTelefono.getText().toString();



        // Comprobar si los campos de texto están vacíos
        if (telefono.isEmpty() || horario.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "No puede quedar ningún campo vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si el número de teléfono es válido
        if (!telefono.matches("(\\+34|0034)?[6-9][0-9]{8}")) {
            Toast.makeText(this, "Por favor, introduzca un número de teléfono válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar el restaurante en Firestore
        actualizacionRestaurante(telefono, horario, direccion);

    }

    public void actualizacionRestaurante(String telefono, String horario, String direccion) {

        // Crear un Map para guardar los campos del restaurante
        Map<String, Object> restauranteMap = new HashMap<>();

        // Añadir cada campo del restaurante al Map solo si no es null para evitar problemas al guardar en Firestore
        if (telefono != null) {
            restauranteMap.put("telefono", telefono);
        }
        if (horario != null) {
            restauranteMap.put("horario", horario);
        }
        if (direccion != null) {
            restauranteMap.put("direccion", direccion);
        }

        // Actualizar el restaurante en Firestore
        db.collection("restaurante").document(restauranteId)
                .update(restauranteMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MasInfoActivity.this, "Restaurante actualizado con éxito", Toast.LENGTH_SHORT).show();
                    //refrescamos la interfaz
                    obtenerDatosRestaurante();
                })
                .addOnFailureListener(e -> {
                    // Error al actualizar el restaurante
                    errorMessage = "Error al actualizar el restaurante: " + e.getMessage();
                    Toast.makeText(MasInfoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
    }

    public void edicion() {
        if (!isEditing) {
            habilitarEdicionEditText();
        } else {
            comprobacionCamposYGuardar();
            deshabilitarEdicionEditText();
        }
    }

    public void llamarTelefono() {
    String telefono = etTelefono.getText().toString();

    Intent intent = new Intent(Intent.ACTION_DIAL); // Intent implicito para abrir la aplicación de llamadas
    intent.setData(Uri.parse("tel:" + telefono)); // Agregamos el número de teléfono al Intent

    // Comprobar si hay una actividad que pueda manejar el Intent
    if (intent.resolveActivity(getPackageManager()) != null) {
        startActivity(intent);
    } else {
        Toast.makeText(this, "No se encontró una aplicación de marcado de teléfono", Toast.LENGTH_SHORT).show();
    }
}

    public void abrirMapa() {
        String direccion = etDireccion.getText().toString();
        // Crear un Intent implicito para abrir Google Maps con la dirección del restaurante
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + direccion);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Especificar que se abra con Google Maps

        // Comprobar si hay una actividad que pueda manejar el Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "No se encontró una aplicación de mapas", Toast.LENGTH_SHORT).show();
        }
    }

    public void volver() {
        Intent intent = new Intent(this, InfoRestauranteActivity.class);
        startActivity(intent);
    }


}