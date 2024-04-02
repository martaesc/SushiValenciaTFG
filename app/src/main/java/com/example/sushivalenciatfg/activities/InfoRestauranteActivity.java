package com.example.sushivalenciatfg.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.models.Restaurante;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InfoRestauranteActivity extends AppCompatActivity {

    private ImageView ivImagenRestaurante;
    private EditText etNombreRestaurante;
    private EditText etDescripcionRestaurante;
    private EditText etLinkRestaurante;

    private TextView tvPuntuacionRestaurante;
    private TextView tvRestauranteLink;

    private Button btnVolver;
    private Button btnComentarios;
    private Button btnMasInfo;

    private ImageButton btnEditar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String restauranteId;

    private boolean isEditing = false;



    // Declaración de los ActivityResultLauncher
    private ActivityResultLauncher<Intent> mGalleryResultLauncher;
    private ActivityResultLauncher<Intent> mCameraResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_restaurante);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //recoger el id del restaurante que se ha pasado desde el RestauranteAdapter
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

        inicializarActivityResultLaunchers();

        btnEditar.setOnClickListener(v -> edicion());

        tvRestauranteLink.setOnClickListener(v -> clickLinkRestaurante());

        btnVolver.setOnClickListener(v -> volverMenu());
        btnMasInfo.setOnClickListener(v -> irAMasInfo());
        btnComentarios.setOnClickListener(v -> irAComentarios(v));
    }

    public void obtenerReferencias() {
        ivImagenRestaurante = findViewById(R.id.restauranteImg);
        etNombreRestaurante = findViewById(R.id.restauranteNombre);
        etDescripcionRestaurante = findViewById(R.id.restauranteDescripcion);
        tvPuntuacionRestaurante = findViewById(R.id.restaurantePuntuacion);
        etLinkRestaurante = findViewById(R.id.restauranteLink);
        tvRestauranteLink = findViewById(R.id.tvRestauranteLink);

        btnVolver = findViewById(R.id.btnVolver);
        btnComentarios = findViewById(R.id.btnComentarios);
        btnMasInfo = findViewById(R.id.btnMasInfo);

        btnEditar = findViewById(R.id.imageButtonEditar);
    }

    private void inicializarActivityResultLaunchers() {
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

    private void abrirDialogoSeleccionImagen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InfoRestauranteActivity.this);
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
                            Integer puntuacion = document.getLong("puntuacion").intValue();
                            String link = document.getString("linkRestaurante");
                            String imagenUrl = document.getString("imagenRestaurante");

                            etNombreRestaurante.setText(nombre);
                            etDescripcionRestaurante.setText(descripcion);
                            tvPuntuacionRestaurante.setText(String.valueOf(puntuacion));
                            tvRestauranteLink.setText(link);

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
        // Habilitar la edición de los EditText
        etNombreRestaurante.setEnabled(true);
        etDescripcionRestaurante.setEnabled(true);

        // Hacer que el TextView sea invisible y que el EditText sea visible
        tvRestauranteLink.setVisibility(View.GONE);
        etLinkRestaurante.setVisibility(View.VISIBLE);

        // Copiar el texto del TextView a al EditText
        etLinkRestaurante.setText(tvRestauranteLink.getText().toString());


        // Cambia el icono de edición al de "Guardar" para indicar que se pueden guardar los cambios
        btnEditar.setImageResource(R.drawable.icono_guardar);

        // Habilitar la selección de una imagen
        ivImagenRestaurante.setOnClickListener(v -> abrirDialogoSeleccionImagen());

        isEditing = true;
    }

    public void deshabilitarEdicionEditText() {
        // Deshabilitar la edición de los EditText
        etNombreRestaurante.setEnabled(false);
        etDescripcionRestaurante.setEnabled(false);

        // Hacer que el TextView sea visible y que el EditText sea invisible
        tvRestauranteLink.setVisibility(View.GONE);
        etLinkRestaurante.setVisibility(View.VISIBLE);

        // Cambiar el icono del botón de editar de nuevo al icono de editar
        btnEditar.setImageResource(R.drawable.icono_editar);

        isEditing = false;
    }




    /**
     * Este método recoge los datos de los EditText, realiza varias comprobaciones para asegurarse de que los datos son válidos,
     * y luego llama al método subirImagenFirebaseStorage().
     */
    private void comprobacionCamposYGuardar() {
        // Recoger los datos de los EditText
        String nombre = etNombreRestaurante.getText().toString();
        String descripcion = etDescripcionRestaurante.getText().toString();
        String link = etLinkRestaurante.getText().toString();

        // Comprobar si el ImageView tiene una imagen establecida
        if (ivImagenRestaurante.getDrawable() == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la imagen seleccionada como un bitmap
        BitmapDrawable drawable = (BitmapDrawable) ivImagenRestaurante.getDrawable();
        Bitmap imagenRestaurante = drawable.getBitmap();

        // Comprobar si los campos de texto están vacíos
        if (nombre.isEmpty() || descripcion.isEmpty() || link.isEmpty()) {
            Toast.makeText(this, "No puede quedar ningún campo vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si la descripción tiene más de 20 líneas
        if (descripcion.split("\n").length > 20) {
            Toast.makeText(this, "La descripción no puede tener más de 20 líneas", Toast.LENGTH_SHORT).show();
            return;
        }


        // Comprobar si el enlace es válido
        if (!Patterns.WEB_URL.matcher(link).matches()) {
            Toast.makeText(this, "El enlace no es válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si todas las comprobaciones son correctas, se sube la imagen a Firebase Storage, se obtiene la URL de la imagen, se le añade al restaurante y este se guarda en Firestore
        subirImagenFirebaseStorage(imagenRestaurante, nombre, descripcion, link);
    }

    /**
     * Este método sube la imagen del restaurante a Firebase Storage. Una vez que la imagen se ha subido con éxito,
     * llama al método obtenerURLImagenFirebaseStorage().
     */
    public void subirImagenFirebaseStorage(Bitmap imagenRestaurante, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante) {
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
                    obtenerURLImagenFirebaseStorage(storageRef, nombreRestaurante, descripcionRestaurante, linkRestaurante);
                })
                .addOnFailureListener(exception -> {
                    Log.e("InfoRestauranteActivity", "Error al subir la imagen a Firebase Storage: " + exception.getMessage());
                    Toast.makeText(InfoRestauranteActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Este método obtiene la URL de la imagen que se acaba de subir a Firebase Storage.
     * Una vez que se ha obtenido la URL de la imagen, llama al método actualizacionRestaurante().
     */
    public void obtenerURLImagenFirebaseStorage(StorageReference storageRef, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante) {
        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // URL de la imagen obtenida con éxito, actualizar el restaurante en Firestore
                    String urlImagen = uri.toString();
                    actualizacionRestaurante(nombreRestaurante, descripcionRestaurante, linkRestaurante, urlImagen);
                })
                .addOnFailureListener(exception -> {
                    Log.e("InfoRestauranteActivity", "Error al obtener la URL de la imagen de Firebase Storage: " + exception.getMessage());
                    Toast.makeText(InfoRestauranteActivity.this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Este método actualiza los datos del restaurante en Firestore. Crea un nuevo Map con los nuevos datos del restaurante y
     * luego actualiza el documento del restaurante en Firestore con estos datos.
     */
    public void actualizacionRestaurante(String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String urlImagen) {

        // Crear un nuevo objeto Map para guardar los campos del restaurante
        Map<String, Object> restauranteMap = new HashMap<>();

        // Añadir cada campo del restaurante al Map solo si no es null para evitar problemas al guardar en Firestore
        if (nombreRestaurante != null) {
            restauranteMap.put("nombre", nombreRestaurante);
        }
        if (descripcionRestaurante != null) {
            restauranteMap.put("descripcion", descripcionRestaurante);
        }
        if (linkRestaurante != null) {
            restauranteMap.put("linkRestaurante", linkRestaurante);
        }
        if (urlImagen != null) {
            restauranteMap.put("imagenRestaurante", urlImagen);
        }

        // Actualizar el restaurante en Firestore
        db.collection("restaurante").document(restauranteId)
                .update(restauranteMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(InfoRestauranteActivity.this, "Restaurante actualizado con éxito", Toast.LENGTH_SHORT).show();
                    //refrescamos la interfaz
                    obtenerDatosRestaurante();
                })
                .addOnFailureListener(e -> {
                    Log.e("InfoRestauranteActivity", "Error al actualizar el restaurante: " + e.getMessage());
                    Toast.makeText(InfoRestauranteActivity.this, "Error al actualizar el restaurante:", Toast.LENGTH_SHORT).show();
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

    public void clickLinkRestaurante(){
        String url = tvRestauranteLink.getText().toString();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
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

    public void irAComentarios(View view) {
        Intent intent = new Intent(this, ComentariosActivity.class);
        intent.putExtra("idRestaurante", restauranteId);
        startActivity(intent);
    }
}