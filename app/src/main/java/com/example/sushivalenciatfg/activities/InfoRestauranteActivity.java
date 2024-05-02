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

/**
 * Esta es la clase InfoRestauranteActivity, que extiende de AppCompatActivity.
 * Esta clase se encarga de mostrar y editar la información de un restaurante específico.
 */
public class InfoRestauranteActivity extends AppCompatActivity {

    // Referencias a los elementos de la interfaz de usuario
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

    // Referencia a la base de datos Firestore, a la autenticación de Firebase y al usuario actual
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Launchers para las actividades de resultado de la galería
    private ActivityResultLauncher<Intent> mGalleryResultLauncher;
    private ActivityResultLauncher<Intent> mCameraResultLauncher;

    private String restauranteId;
    private boolean isEditing;


    // Getters y Setters para las pruebas de instrumentación
    public ImageView getIvImagenRestaurante() {
        return ivImagenRestaurante;
    }

    public EditText getEtNombreRestaurante() {
        return etNombreRestaurante;
    }

    public EditText getEtDescripcionRestaurante() {
        return etDescripcionRestaurante;
    }

    public EditText getEtLinkRestaurante() {
        return etLinkRestaurante;
    }

    public TextView getTvRestauranteLink() {
        return tvRestauranteLink;
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
        setContentView(R.layout.activity_info_restaurante);

        isEditing = false;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //recogemos el id del restaurante que se ha pasado desde el RestauranteAdapter (cuando se ha clickado en un restaurante)
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
        btnComentarios.setOnClickListener(v -> irAComentarios());
    }


    /**
     * Este método se encarga de obtener las referencias a los elementos de la interfaz de usuario.
     */
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


    /**
     * Este método se encarga de inicializar los ActivityResultLauncher para la galería y la cámara.
     */
    public void inicializarActivityResultLaunchers() {
        mGalleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            ivImagenRestaurante.setImageURI(uri);
                        }
                    }
                }
        );
        mCameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            if (imageBitmap != null) {
                                ivImagenRestaurante.setImageBitmap(imageBitmap);
                            }
                        }
                    }
                }
        );
    }

    /**
     * Este método se encarga de abrir un diálogo para que el usuario seleccione una imagen de la galería o la cámara.
     */
    public void abrirDialogoSeleccionImagen() {
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


    /**
     * Este método se encarga de obtener los datos del restaurante de Firestore y mostrarlos en la interfaz de usuario.
     */
    public void obtenerDatosRestaurante() {
        if (restauranteId != null) {
            db.collection("restaurantes").document(restauranteId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            //si encontramos el restaurante en la bbdd, obtenemos sus datos y los mostramos en la interfaz
                            if (document != null && document.exists()) {
                                String nombre = document.getString("nombre");
                                String descripcion = document.getString("descripcion");
                                Double puntuacionPromedio = document.getDouble("puntuacion");
                                String link = document.getString("linkRestaurante");
                                String imagenUrl = document.getString("imagenRestaurante");

                                if (nombre != null) {
                                    etNombreRestaurante.setText(nombre);
                                }
                                if (descripcion != null) {
                                    etDescripcionRestaurante.setText(descripcion);
                                }
                                if (puntuacionPromedio != null) {
                                    tvPuntuacionRestaurante.setText(String.format("%.1f", puntuacionPromedio));
                                }
                                if (link != null) {
                                    tvRestauranteLink.setText(link);
                                }
                                // Para cargar la imagen desde una URL en un ImageView usamos la biblioteca Glide
                                if (imagenUrl != null) {
                                    Glide.with(this)
                                            .load(imagenUrl)
                                            .into(ivImagenRestaurante);
                                }
                            } else {
                                Log.e("InfoRestauranteActivity", "No se encontró el restaurante");
                            }
                        } else {
                            Log.e("InfoRestauranteActivity", "Error al obtener restaurante", task.getException());
                        }
                    });
        } else {
            Log.e("InfoRestauranteActivity", "El id del restaurante es nulo");
        }
    }

    /**
     * Este método se encarga de habilitar el botón de editar en la pantalla si el usuario actual es el creador del restaurante.
     */
    public void habilitarBotonEditar() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Buscamos en la colección "restaurantes" un documento donde el campo "idUsuarioRestaurante" coincide con el ID del usuario actual
            // y el campo idRestaurante coincide con el ID del restaurante actual
            db.collection("restaurantes")
                    .whereEqualTo("idUsuarioRestaurante", userId)
                    .whereEqualTo("idRestaurante", restauranteId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Si el usuario es el creador del restaurante, mostramos el botón de editar
                            btnEditar.setVisibility(View.VISIBLE);
                        } else {
                            // Si el usuario no es el creador del restaurante, lo ocultamos
                            btnEditar.setVisibility(View.GONE);
                        }
                    });
        } else {
            Log.d("InfoRestauranteActivity", "El usuario actual es nulo");
        }
    }


    /**
     * Este método se encarga de habilitar la edición de los campos de texto y la selección de una imagen cuando el usuario hace clic en el botón de editar.
     */
    public void habilitarEdicionEditText() {
        // Habilitamos la edición de los EditText
        etNombreRestaurante.setEnabled(true);
        etDescripcionRestaurante.setEnabled(true);

        // Cambiamos la visibilidad de los TextView y EditText del enlace del restaurante
        tvRestauranteLink.setVisibility(View.GONE);
        etLinkRestaurante.setVisibility(View.VISIBLE);

        // copiamos el texto del TextView al EditText
        etLinkRestaurante.setText(tvRestauranteLink.getText().toString());

        // Cambiamos el icono de edición por el de guardar
        btnEditar.setImageResource(R.drawable.icono_guardar);

        ivImagenRestaurante.setOnClickListener(v -> abrirDialogoSeleccionImagen());

        isEditing = true; // estamos en modo edición
    }


    /**
     * Este método se encarga de deshabilitar la edición de los campos de texto y la selección de una imagen cuando el usuario hace clic en el botón de guardar.
     */
    public void deshabilitarEdicionEditText() {
        etNombreRestaurante.setEnabled(false);
        etDescripcionRestaurante.setEnabled(false);

        tvRestauranteLink.setVisibility(View.VISIBLE);
        etLinkRestaurante.setVisibility(View.GONE);

        // volvemos a cambiar el icono de guardar por el de editar
        btnEditar.setImageResource(R.drawable.icono_editar);

        // deshabilitamos la selección de imagen al hacer clic en el ImageView
        ivImagenRestaurante.setOnClickListener(null);

        isEditing = false; // ya no estamos en modo edición
    }


    /**
     * Este método se encarga de comprobar si los datos ingresados por el usuario para editar el restaurante son correctos.
     */
    public void comprobacionDatosIngresados() {
        String nombre = etNombreRestaurante.getText().toString();
        String descripcion = etDescripcionRestaurante.getText().toString();
        String link = etLinkRestaurante.getText().toString();

        // Comprobamos si los campos de texto están vacíos
        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "No puede quedar ningún campo vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobamos si el ImageView tiene una imagen establecida
        if (ivImagenRestaurante.getDrawable() == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        // y si la tiene, obtenemos el objeto Drawble que la representa y lo convertimos a Bitmap
        BitmapDrawable drawable = (BitmapDrawable) ivImagenRestaurante.getDrawable();
        Bitmap imagenRestaurante = drawable.getBitmap();

        // Comprobamos si la descripción tiene más de 20 líneas
        if (descripcion.split("\n").length > 20) {
            Toast.makeText(this, "La descripción no puede tener más de 20 líneas", Toast.LENGTH_SHORT).show();
            return;
        }

        // comprobamos que, si el campo del link no está vacío, sea una URL válido o el texto "El restaurante no tiene web" (se añade esta última condición para
        // evitar problemas al guardar los nuevos datos en el caso de que el campo del enlace se deje como estaba originalmente y contuviera dicho texto en lugar de una URL)
        if ((!link.isEmpty() && (!Patterns.WEB_URL.matcher(link).matches() && !link.equals("El restaurante no tiene web")))) {
            Toast.makeText(this, "Por favor, introduzca un enlace válido", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (link.isEmpty()) {
                link = null;
            }
        }

        // Si todas las comprobaciones son correctas, se sube la imagen a Firebase Storage
        subirImagenFirebaseStorage(imagenRestaurante, nombre, descripcion, link);
    }

    /**
     * Este método se encarga de subir la nueva imagen del restaurante a Firebase Storage.
     *
     * @param imagenRestaurante      Bitmap de la imagen del restaurante a subir.
     * @param nombreRestaurante      Nombre del restaurante.
     * @param descripcionRestaurante Descripción del restaurante.
     * @param linkRestaurante        Enlace del restaurante.
     */
    public void subirImagenFirebaseStorage(Bitmap imagenRestaurante, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante) {
        // Creamos una referencia única para la imagen en Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenes_restaurante/" + UUID.randomUUID().toString() + ".jpg");

        // Convertimos la imagen del restaurante (que es un objeto Bitmap) en un array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagenRestaurante.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datosImagen = baos.toByteArray();

        // y lo subimos a  Firebase Storage
        storageRef.putBytes(datosImagen)
                .addOnSuccessListener(taskSnapshot -> {
                    // si la imagen se sube con éxito, obtenemos su URL y se la añadimos al restaurante
                    obtenerURLImagenFirebaseStorage(storageRef, nombreRestaurante, descripcionRestaurante, linkRestaurante);
                })
                .addOnFailureListener(exception -> {
                    Log.e("InfoRestauranteActivity", "Error al subir la imagen a Firebase Storage: " + exception.getMessage());
                    Toast.makeText(InfoRestauranteActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Este método se encarga de obtener la URL de la imagen que se acaba de subir a Firebase Storage y añadir la imagen al restaurante.
     *
     * @param storageRef             Referencia de almacenamiento de Firebase donde se encuentra la imagen.
     * @param nombreRestaurante      Nombre del restaurante.
     * @param descripcionRestaurante Descripción del restaurante.
     * @param linkRestaurante        Enlace del restaurante.
     */
    public void obtenerURLImagenFirebaseStorage(StorageReference storageRef, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante) {
        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // si obtenemos la URL de la imagen con éxito, actualizamos el restaurante con los nuevos datos y lo guardamos en Firestore
                    String urlImagen = uri.toString();
                    actualizacionRestaurante(nombreRestaurante, descripcionRestaurante, linkRestaurante, urlImagen);
                })
                .addOnFailureListener(exception -> {
                    Log.e("InfoRestauranteActivity", "Error al obtener la URL de la imagen de Firebase Storage: " + exception.getMessage());
                    Toast.makeText(InfoRestauranteActivity.this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Este método se encarga de actualizar los datos del restaurante en Firestore con los datos ingresados por el usuario.
     *
     * @param nombreRestaurante      Nombre del restaurante.
     * @param descripcionRestaurante Descripción del restaurante.
     * @param linkRestaurante        Enlace del restaurante.
     * @param urlImagen              URL de la imagen del restaurante.
     */
    public void actualizacionRestaurante(String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String urlImagen) {

        // Creamos un nuevo objeto Map para guardar los datos del restaurante (solo si no son null para evitar problemas al guardar en Firestore)
        Map<String, Object> restauranteMap = new HashMap<>();
        if (nombreRestaurante != null) {
            restauranteMap.put("nombre", nombreRestaurante);
        }
        if (descripcionRestaurante != null) {
            restauranteMap.put("descripcion", descripcionRestaurante);
        }
        // si linkRestaurante es null, se añade al Map con el valor "El restaurante no tiene web"
        if (linkRestaurante != null) {
            restauranteMap.put("linkRestaurante", linkRestaurante);
        } else {
            restauranteMap.put("linkRestaurante", "El restaurante no tiene web");
        }
        if (urlImagen != null) {
            restauranteMap.put("imagenRestaurante", urlImagen);
        }

        // Actualizamos el restaurante en Firestore
        db.collection("restaurantes").document(restauranteId)
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

    /**
     * Este método se encarga de manejar el proceso de edición de la información del restaurante.
     */
    public void edicion() {
        if (!isEditing) {
            habilitarEdicionEditText();
        } else {
            comprobacionDatosIngresados();
            deshabilitarEdicionEditText();

        }
    }


    /**
     * Este método se encarga de manejar el evento de clic en el enlace del restaurante.
     */
    public void clickLinkRestaurante() {
        String url = tvRestauranteLink.getText().toString();
        // para evitar el ActivityNotFoundException al clickar sobre el textview cuando no es un enlace
        if (url.equals("El restaurante no tiene web")) {
            Toast.makeText(this, "Este restaurante no tiene página web", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }


    /**
     * Este método se encarga de volver al menú principal de la aplicación.
     */
    public void volverMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /**
     * Este método se encarga de navegar a la pantalla de más información del restaurante.
     */
    public void irAMasInfo() {
        Intent intent = new Intent(this, MasInfoActivity.class);
        intent.putExtra("idRestaurante", restauranteId);
        startActivity(intent);
    }


    /**
     * Este método se encarga de navegar a la pantalla de comentarios del restaurante.
     */
    public void irAComentarios() {
        Intent intent = new Intent(this, ComentariosActivity.class);
        intent.putExtra("idRestaurante", restauranteId);
        startActivity(intent);
    }


}