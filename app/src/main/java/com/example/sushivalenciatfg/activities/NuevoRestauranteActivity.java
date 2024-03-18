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
        btnGuardarRestaurante.setOnClickListener(v -> obtencionDatosCampos());


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

    // Método para obtener los datos de los campos y subir la imagen a Firebase Storage
    public void obtencionDatosCampos() {

        String nombreRestaurante = etNombreRestaurante.getText().toString();
        String descripcionRestaurante = etDescripcionRestaurante.getText().toString();
        String linkRestaurante = etLinkRestaurante.getText().toString();
        String horario = etHorarioRestaurante.getText().toString();
        String telefono = etTelefonoRestaurante.getText().toString();
        String direccion = etDireccionRestaurante.getText().toString();

        //Obtenemos la imagen seleccionada como un bitmap
        BitmapDrawable drawable = (BitmapDrawable) ivImagenRestaurante.getDrawable();
        Bitmap imagenRestaurante = drawable.getBitmap();

        // Comprobar que los campos no estén vacíos
        if (nombreRestaurante.isEmpty() || descripcionRestaurante.isEmpty() || linkRestaurante.isEmpty() || horario.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || imagenRestaurante == null) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            // Subir la imagen a Firebase Storage y obtener la URL de la imagen
            subirImagenFirebaseStorage(imagenRestaurante, nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion);
        }
    }

    // Método para subir la imagen a Firebase Storage y obtener la URL
    private void subirImagenFirebaseStorage(Bitmap imagenRestaurante, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion) {
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
                    obtenerURLImagenFirebaseStorage(storageRef, nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion);
                })
                .addOnFailureListener(exception -> {
                    // Error al subir la imagen a Firebase Storage
                    Toast.makeText(NuevoRestauranteActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para obtener la URL de la imagen de Firebase Storage
    private void obtenerURLImagenFirebaseStorage(StorageReference storageRef, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion) {
        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // URL de la imagen obtenida con éxito, guardar el restaurante en Firestore
                    String urlImagen = uri.toString();
                    guardarRestauranteEnFirestore(nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion, urlImagen);
                })
                .addOnFailureListener(exception -> {
                    // Error al obtener la URL de la imagen
                    Toast.makeText(NuevoRestauranteActivity.this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para guardar el restaurante en Firestore con la URL de la imagen
    private void guardarRestauranteEnFirestore(String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion, String urlImagen) {
        // Obtener el ID del usuario que está creando el nuevo restaurante
        String idUsuario = mAuth.getCurrentUser().getUid();

        // Crear un nuevo objeto Restaurante con los valores recolectados
        Restaurante restaurante = new Restaurante(nombreRestaurante, descripcionRestaurante, direccion, telefono, horario, linkRestaurante, urlImagen, idUsuario);

        // Guardar el restaurante en Firestore
        db.collection("restaurantes")
                .add(restaurante)
                .addOnSuccessListener(documentReference -> {
                    // Éxito al guardar el restaurante
                    String idRestaurante = documentReference.getId(); // ID del restaurante
                    restaurante.setIdRestaurante(idRestaurante);
                    Toast.makeText(NuevoRestauranteActivity.this, "Restaurante guardado con éxito", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    // Error al guardar el restaurante
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



}