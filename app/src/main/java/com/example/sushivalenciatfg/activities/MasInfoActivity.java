package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Esta es la clase MasInfoActivity, que extiende de AppCompatActivity.
 * Esta clase se encarga de mostrar y editar la información adicional de un restaurante específico.
 */
public class MasInfoActivity extends AppCompatActivity {

    // Referencias a los elementos de la interfaz de usuario
    private EditText etTelefono;
    private EditText etHorario;
    private EditText etDireccion;
    private TextView tvTelefono;
    private TextView tvHorario;
    private TextView tvDireccion;
    private Button btnEditar;
    private Button btnVolver;
    private Button btnVolverMenuPrincipal;

    // Referencia a la base de datos Firestore, a la autenticación de Firebase y al usuario actual
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String restauranteId;
    private boolean isEditing;


    // Getters y setters útiles para las pruebas instrumentadas
    public EditText getEtTelefono() {
        return etTelefono;
    }

    public EditText getEtHorario() {
        return etHorario;
    }

    public EditText getEtDireccion() {
        return etDireccion;
    }

    public TextView getTvTelefono() {
        return tvTelefono;
    }

    public TextView getTvHorario() {
        return tvHorario;
    }

    public TextView getTvDireccion() {
        return tvDireccion;
    }

    public Button getBtnEditar() {
        return btnEditar;
    }

    public void setIsEditing(boolean b) {
        isEditing = b;
    }

    public boolean getIsEditing() {
        return isEditing;
    }


    /**
     * Este método se llama cuando la actividad está iniciando.
     * Inicializa la actividad y establece los onClickListener para los botones.
     *
     * @param savedInstanceState Si la actividad se está reinicializando después de haber sido cerrada previamente
     *                           entonces este Bundle contiene los datos que suministró más recientemente en onSaveInstanceState(Bundle).
     *                           Nota: De lo contrario, es nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mas_info);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        isEditing = false;

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
        btnVolverMenuPrincipal.setOnClickListener(v -> volverMenuPrincipal());
    }


    /**
     * Este método se encarga de obtener las referencias a los elementos de la vista.
     */
    public void obtenerReferencias() {
        etTelefono = findViewById(R.id.etTelefono);
        etHorario = findViewById(R.id.etHorario);
        etDireccion = findViewById(R.id.etDireccion);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvHorario = findViewById(R.id.tvHorario);
        tvDireccion = findViewById(R.id.tvDireccion);
        btnEditar = findViewById(R.id.btnEditarInfo);
        btnVolver = findViewById(R.id.btnVolverInfoActivity);
        btnVolverMenuPrincipal = findViewById(R.id.btnVolverMenuPrincipal);
    }


    /**
     * Este método se encarga de obtener los datos del restaurante de  Firestore y mostrarlos en la vista.
     */
    public void obtenerDatosRestaurante() {
        db.collection("restaurantes").document(restauranteId)
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


    /**
     * Este método se encarga de habilitar el botón de edición si el usuario actual es el creador del restaurante que se quiere editar.
     */
    public void habilitarBotonEditar() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Buscamos en la colección "restaurantes" un documento donde el campo "idUsuarioRestaurante" coincida con el ID del usuario actual
            // y el campo idRestaurante con el ID del restaurante actual
            db.collection("restaurantes")
                    .whereEqualTo("idUsuarioRestaurante", userId)
                    .whereEqualTo("idRestaurante", restauranteId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            btnEditar.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            Log.e("MasInfoActivity", "El usuario actual es nulo");
        }
    }


    /**
     * Este método se encarga de habilitar la edición de los campos de texto.
     */
    public void habilitarEdicionEditText() {
        // Copiamos el texto de los TextView a los EditText
        etTelefono.setText(tvTelefono.getText().toString());
        etHorario.setText(tvHorario.getText().toString());
        etDireccion.setText(tvDireccion.getText().toString());

        // Hacemos que los TextView sean invisibles y que los EditText sean visibles
        tvTelefono.setVisibility(View.GONE);
        etTelefono.setVisibility(View.VISIBLE);
        tvHorario.setVisibility(View.GONE);
        etHorario.setVisibility(View.VISIBLE);
        tvDireccion.setVisibility(View.GONE);
        etDireccion.setVisibility(View.VISIBLE);

        // Cambiamos el texto del botón a "Guardar" mientras estamos en modo edición
        btnEditar.setText("Guardar");

        isEditing = true;


    }


    /**
     * Este método se encarga de deshabilitar la edición de los campos de texto.
     */
    public void deshabilitarEdicionEditText() {
        // Hacemos que los EditText sean invisibles y que los TextView sean visibles
        etTelefono.setVisibility(View.GONE);
        tvTelefono.setVisibility(View.VISIBLE);
        etHorario.setVisibility(View.GONE);
        tvHorario.setVisibility(View.VISIBLE);
        etDireccion.setVisibility(View.GONE);
        tvDireccion.setVisibility(View.VISIBLE);

        // Cambiamos el texto del botón a "Editar" después de guardar
        btnEditar.setText("Editar");

        isEditing = false;
    }


    /**
     * Este método se encarga de comprobar si los campos de texto están vacíos y si el número de teléfono es válido.
     * Si todo está correcto, actualiza el restaurante en Firestore.
     */
    public void comprobacionCamposYActuaizar() {
        String telefono = etTelefono.getText().toString();
        String horario = etHorario.getText().toString();
        String direccion = etDireccion.getText().toString();


        // Comprobamos si los campos de texto están vacíos
        if (telefono.isEmpty() || horario.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "No puede quedar ningún campo vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobamos si el número de teléfono es válido (número de 9 dígitos que empiece con un dígito entre 6 y 9, con o sin prefijo +34 o 0034)
        if (!telefono.replaceAll("\\s", "").matches("(\\+34|0034)?[6-9][0-9]{8}")) {
            Toast.makeText(this, "Por favor, introduzca un número de teléfono español válido", Toast.LENGTH_SHORT).show();
            return;
        }

        actualizacionRestaurante(telefono, horario, direccion);

    }


    /**
     * Este método se encarga de la lógica para actualizar los datos del restaurante en Firestore.
     *
     * @param telefono  El nuevo número de teléfono del restaurante.
     * @param horario   El nuevo horario del restaurante.
     * @param direccion La nueva dirección del restaurante.
     */
    public void actualizacionRestaurante(String telefono, String horario, String direccion) {

        // Creamos un Map para guardar los datos del restaurante (solo si no son null para evitar problemas al guardar en Firestore)
        Map<String, Object> restauranteMap = new HashMap<>();
        if (telefono != null) {
            restauranteMap.put("telefono", telefono);
        }
        if (horario != null) {
            restauranteMap.put("horario", horario);
        }
        if (direccion != null) {
            restauranteMap.put("direccion", direccion);
        }

        // Actualizamos el restaurante en Firestore
        db.collection("restaurantes").document(restauranteId)
                .update(restauranteMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Restaurante actualizado con éxito", Toast.LENGTH_SHORT).show();
                    //refrescamos la interfaz
                    obtenerDatosRestaurante();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al actualizar el restaurante: " + e.getMessage());
                    Toast.makeText(this, "Error al actualizar el restaurante", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Este método se encarga de manejar el proceso de edición de la información del restaurante.
     */
    public void edicion() {
        if (!isEditing) {
            habilitarEdicionEditText();
        } else {
            comprobacionCamposYActuaizar();
            deshabilitarEdicionEditText();
        }
    }


    /**
     * Este método se encarga de abrir la aplicación de llamadas del dispositivo para iniciar una llamada telefónica al
     * número de teléfono del restaurante.
     */
    public void llamarTelefono() {
        String telefono = etTelefono.getText().toString();
        // Intent implícito para abrir la aplicación de llamadas con el número de teléfono del restaurante
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + telefono));

        // Comprobamos si hay alguna aplicación en el dispositivo que pueda manejar el Intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No se encontró una aplicación de marcado de teléfono", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Este método se encarga de abrir la aplicación de mapas del dispositivo para mostrar la ubicación del restaurante.
     */
    public void abrirMapa() {
        String direccion = tvDireccion.getText().toString();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + direccion);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Comprobamos si hay alguna aplicación en el dispositivo que pueda manejar el Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "No se encontró ninguna aplicación de mapas en el dispositivo", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Este método se encarga de volver a la actividad InfoRestauranteActivity.
     */
    public void volver() {
        Intent intent = new Intent(this, InfoRestauranteActivity.class);
        intent.putExtra("idRestaurante", restauranteId);
        startActivity(intent);
    }


    /**
     * Este método se encarga de volver al menú principal de la aplicación (MainActivity).
     */
    public void volverMenuPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}