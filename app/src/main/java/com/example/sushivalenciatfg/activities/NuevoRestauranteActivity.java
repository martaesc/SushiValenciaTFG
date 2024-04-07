package com.example.sushivalenciatfg.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.models.Restaurante;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NuevoRestauranteActivity extends AppCompatActivity {

    private ImageView ivImagenRestaurante;
    private EditText etNombreRestaurante;
    private EditText etDescripcionRestaurante;
    private EditText etLinkRestaurante;
    private EditText etHorarioRestaurante;
    private EditText etTelefonoRestaurante;
    private EditText etDireccionRestaurante;
    private Button btnGuardarRestaurante;
    private Button btnVolerMenu;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    // Declaración de los ActivityResultLauncher
    private ActivityResultLauncher<Intent> mGalleryResultLauncher;
    private ActivityResultLauncher<Intent> mCameraResultLauncher;


    Restaurante restaurante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_restaurante);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        obtenerReferencias();


        inicializarActivityResultLaunchers();
        abrirDialogoSeleccionImagen();

        // Llamar al método para guardar un restaurante cuando se haga clic en el botón correspondiente
        btnGuardarRestaurante.setOnClickListener(v -> comprobacionCamposYGuardar());

        // Llamar al método para volver al menú principal cuando se haga clic en el botón correspondiente
        btnVolerMenu.setOnClickListener(v -> volverMenu());


    }

    public void obtenerReferencias() {
        ivImagenRestaurante = findViewById(R.id.iv_imagenNuevoRestaurante);
        etNombreRestaurante = findViewById(R.id.et_nombreNuevoRestaurante);
        etDescripcionRestaurante = findViewById(R.id.et_descripcionNuevoRestaurante);
        etTelefonoRestaurante = findViewById(R.id.et_telefonoNuevoRestaurante);
        etDireccionRestaurante = findViewById(R.id.et_ubicacionNuevoRestaurante);
        etHorarioRestaurante = findViewById(R.id.et_horarioNuevoRestaurante);
        etLinkRestaurante = findViewById(R.id.et_linkNuevoRestaurante);
        btnGuardarRestaurante = findViewById(R.id.btnGuardarNuevoRestaurante);
        btnVolerMenu = findViewById(R.id.btnVolverMenu);
    }

    public void inicializarActivityResultLaunchers() {
        // Inicialización de los ActivityResultLauncher para la galería y la cámara
        mGalleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        ivImagenRestaurante.setImageURI(uri);
                    }
                }
        );

        mCameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivImagenRestaurante.setImageBitmap(imageBitmap);
                    }
                }
        );
    }

    // Método para abrir el diálogo de selección de imagen
    public void abrirDialogoSeleccionImagen() {
        ivImagenRestaurante.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(NuevoRestauranteActivity.this);
            builder.setTitle("Elige una opción");
            builder.setPositiveButton("Galería", (dialog, which) -> {
                // Inicia la actividad de la galería
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mGalleryResultLauncher.launch(intent);
            });
            builder.setNegativeButton("Cámara", (dialog, which) -> {
                // Inicia la actividad de la cámara
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mCameraResultLauncher.launch(intent);
            });
            builder.show();
        });
    }

    // Método para comprobar que todos los campos son correctos y guardar el restaurante en Firestore
    public void comprobacionCamposYGuardar() {

        String nombreRestaurante = etNombreRestaurante.getText().toString();
        String descripcionRestaurante = etDescripcionRestaurante.getText().toString();
        String linkRestaurante = etLinkRestaurante.getText().toString();
        String horario = etHorarioRestaurante.getText().toString();
        String telefono = etTelefonoRestaurante.getText().toString();
        String direccion = etDireccionRestaurante.getText().toString();

        // Comprobar si el ImageView tiene una imagen establecida
        if (ivImagenRestaurante.getDrawable() == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la imagen seleccionada como un bitmap
        BitmapDrawable drawable = (BitmapDrawable) ivImagenRestaurante.getDrawable();
        Bitmap imagenRestaurante = drawable.getBitmap();

        // Comprobar si los campos de texto están vacíos
        if (nombreRestaurante.isEmpty() || descripcionRestaurante.isEmpty() || linkRestaurante.isEmpty() || horario.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si la descripción tiene más de 20 líneas
        if (descripcionRestaurante.split("\n").length > 20) {
            Toast.makeText(this, "La descripción no puede tener más de 20 líneas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si el número de teléfono es válido
        if (!telefono.matches("(\\+34|0034)?[6-9][0-9]{8}")) {
            Toast.makeText(this, "Por favor, introduzca un número de teléfono válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si el enlace es válido
        if (!Patterns.WEB_URL.matcher(linkRestaurante).matches()) {
            Toast.makeText(this, "Por favor, introduzca un enlace válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si todas las comprobaciones son correctas, se sube la imagen a Firebase Storage, se obtiene la URL de la imagen, se le añade al restaurante y este se guarda en Firestore
        subirImagenFirebaseStorage(imagenRestaurante, nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion);
    }

    // Método para subir la imagen a Firebase Storage y obtener la URL
    public void subirImagenFirebaseStorage(Bitmap imagenRestaurante, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion) {
        // Crear una referencia única para la imagen en Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenes_restaurante/" + UUID.randomUUID().toString() + ".jpg");

        // Convertir la imagen a bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagenRestaurante.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datosImagen = baos.toByteArray();

        // Subir la imagen a Firebase Storage
        storageRef.putBytes(datosImagen)
                .addOnSuccessListener(taskSnapshot -> {
                    // Imagen subida con éxito, obtener la URL de la imagen
                    obtenerURLImagenYAñadirAlRestaurante(storageRef, nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion);
                })
                .addOnFailureListener(exception -> {
                    Log.e("NuevoRestauranteActivity", "Error al subir la imagen a Firebase Storage: " + exception.getMessage());
                    Toast.makeText(NuevoRestauranteActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para obtener la URL de la imagen de Firebase Storage
    public void obtenerURLImagenYAñadirAlRestaurante(StorageReference storageRef, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion) {
        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // URL de la imagen obtenida con éxito, guardar el restaurante en Firestore
                    String urlImagen = uri.toString();
                    nuevoRestaurante(nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion, urlImagen);
                })
                .addOnFailureListener(exception -> {;
                    Log.e("NuevoRestauranteActivity", "Error al obtener la URL de la imagen: " + exception.getMessage());
                    Toast.makeText(NuevoRestauranteActivity.this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                });
    }


    // Método para guardar el restaurante en Firestore con la URL de la imagen
    public void nuevoRestaurante(String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion, String urlImagen) {
        // Obtener el ID del usuario que está creando el nuevo restaurante
        String idUsuario = mAuth.getCurrentUser().getUid();

        // Crear un nuevo objeto Restaurante con los valores recolectados
        restaurante = new Restaurante(nombreRestaurante, descripcionRestaurante, direccion, telefono, horario, linkRestaurante, urlImagen, idUsuario);

        // Crear un nuevo objeto Map para guardar los campos del restaurante
        Map<String, Object> restauranteMap = new HashMap<>();

        // Añadir cada campo del restaurante al Map solo si no es null para evitar problemas al guardar en Firestore
        if (nombreRestaurante != null){
            restauranteMap.put("nombre", nombreRestaurante);
        }
        if (descripcionRestaurante != null){
            restauranteMap.put("descripcion", descripcionRestaurante);
        }
        if (direccion != null) {
            restauranteMap.put("direccion", direccion);
        }
        if (telefono != null){
            restauranteMap.put("telefono", telefono);
        }
        if (horario != null){
            restauranteMap.put("horario", horario);
        }
        if (linkRestaurante != null){
            restauranteMap.put("linkRestaurante", linkRestaurante);
        }
        if (urlImagen != null) {
            restauranteMap.put("imagenRestaurante", urlImagen);
        }
        if (idUsuario != null) {
            restauranteMap.put("idUsuarioRestaurante", idUsuario);
        }

        // Establecer la puntuación en 0 porque al crearlo el restaurante no tiene puntuaciones
        double puntuacion = 0.0;
        restauranteMap.put("puntuacion", puntuacion);

        //int puntuacion = 0;
        //restauranteMap.put("puntuacion", puntuacion);


        // Guardar el restaurante en Firestore
        db.collection("restaurantes")
                .add(restauranteMap)
                .addOnSuccessListener(documentReference -> {
                    // Éxito al guardar el restaurante
                    String idRestaurante = documentReference.getId(); // ID del restaurante
                    documentReference.update("idRestaurante", idRestaurante); // Actualizar el campo idRestaurante en Firestore
                    Toast.makeText(NuevoRestauranteActivity.this, "Restaurante guardado con éxito", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    // Navegar de nuevo a MainActivity
                    Intent intent = new Intent(NuevoRestauranteActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("NuevoRestauranteActivity", "Error al guardar el restaurante: " + e.getMessage());
                    Toast.makeText(NuevoRestauranteActivity.this, "Error al guardar el restaurante", Toast.LENGTH_SHORT).show();
                });
    }


    public void limpiarCampos() {
        ivImagenRestaurante.setImageDrawable(null);
        etNombreRestaurante.setText("");
        etDescripcionRestaurante.setText("");
        etLinkRestaurante.setText("");
        etHorarioRestaurante.setText("");
        etTelefonoRestaurante.setText("");
        etDireccionRestaurante.setText("");
    }

    // Método para volver al menú principal
    public void volverMenu() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }


}