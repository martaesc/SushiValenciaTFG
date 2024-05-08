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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Esta es la clase NuevoRestauranteActivity, que extiende de AppCompatActivity. Se encarga de gestionar la creación de nuevos restaurante en la aplicación.
 */
public class NuevoRestauranteActivity extends AppCompatActivity {

    // Referencias a los elementos de la interfaz de usuario
    private ImageView ivImagenRestaurante;
    private EditText etNombreRestaurante;
    private EditText etDescripcionRestaurante;
    private EditText etLinkRestaurante;
    private EditText etHorarioRestaurante;
    private EditText etTelefonoRestaurante;
    private EditText etDireccionRestaurante;
    private Button btnGuardarRestaurante;
    private Button btnVolerMenu;

    // Referencia a la base de datos Firestore, a la autenticación de Firebase y al usuario actual
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Launchers para las actividades de resultado de la galería, la cámara
    private ActivityResultLauncher<Intent> mGalleryResultLauncher;
    private ActivityResultLauncher<Intent> mCameraResultLauncher;


    Restaurante restaurante;


    // Getters para las pruebas de instrumentación
    public EditText getEtNombreRestaurante() {
        return etNombreRestaurante;
    }

    public EditText getEtDescripcionRestaurante() {
        return etDescripcionRestaurante;
    }

    public EditText getEtHorarioRestaurante() {
        return etHorarioRestaurante;
    }

    public EditText getEtTelefonoRestaurante() {
        return etTelefonoRestaurante;
    }

    public EditText getEtDireccionRestaurante() {
        return etDireccionRestaurante;
    }

    public ImageView getIvImagenRestaurante() {
        return ivImagenRestaurante;
    }

    public EditText getEtLinkRestaurante() {
        return etLinkRestaurante;
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
        setContentView(R.layout.activity_nuevo_restaurante);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        obtenerReferencias();
        inicializarActivityResultLaunchers();
        abrirDialogoSeleccionImagen();

        btnGuardarRestaurante.setOnClickListener(v -> comprobacionDatosIngresados());
        btnVolerMenu.setOnClickListener(v -> volverMenu());

    }


    /**
     * Este método obtiene las referencias a los elementos de la interfaz de usuario.
     */
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


    /**
     * Este método inicializa los launchers para las actividades de resultado de la galería y la cámara.
     */
    public void inicializarActivityResultLaunchers() {
        mGalleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            ivImagenRestaurante.setImageURI(uri);
                        } else {
                            Toast.makeText(this, "Error al obtener la imagen de la galería", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        mCameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bundle extras = data.getExtras();
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            ivImagenRestaurante.setImageBitmap(imageBitmap);
                        } else {
                            Toast.makeText(this, "Error al obtener la imagen de la cámara", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }


    /**
     * Este método abre un diálogo para que el usuario seleccione una imagen desde la galería o la cámara.
     */
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


    // Para entender mejor el flujo de la creación de un nuevo restaurante, la lógica se dividió en varios métodos que se llaman en cascada:
    // comprobacionDatosIngresados() -> subirImagenFirebaseStorage() -> obtenerURLImagenYAñadirAlRestaurante() -> nuevoRestaurante().

    /**
     * Este método se encarga de validar los datos ingresados por el usuario para crear un nuevo restaurante.
     */
    public void comprobacionDatosIngresados() {
        String nombreRestaurante = etNombreRestaurante.getText().toString();
        String descripcionRestaurante = etDescripcionRestaurante.getText().toString();
        String linkRestaurante = etLinkRestaurante.getText().toString();
        String horario = etHorarioRestaurante.getText().toString();
        String telefono = etTelefonoRestaurante.getText().toString();
        String direccion = etDireccionRestaurante.getText().toString();


        // Comprobamos si los campos de texto están vacíos
        if (nombreRestaurante.isEmpty() || descripcionRestaurante.isEmpty() || horario.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobamos si el ImageView tiene una imagen establecida
        if (ivImagenRestaurante.getDrawable() == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        // y si la tiene, obtenemos el objeto Drawble que la representa y lo convertimos a bitmap
        BitmapDrawable drawable = (BitmapDrawable) ivImagenRestaurante.getDrawable();
        Bitmap imagenRestaurante = drawable.getBitmap();


        // Comprobamos si la descripción tiene más de 20 líneas
        if (descripcionRestaurante.split("\n").length > 20) {
            Toast.makeText(this, "La descripción no puede tener más de 20 líneas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobamos si el número de teléfono es válido (número de 9 dígitos que empiece con un dígito entre 6 y 9, con o sin prefijo +34 o 0034 e ignorando los espacios)
        if (!telefono.replaceAll("\\s", "").matches("(\\+34|0034)?[6-9][0-9]{8}")) {
            Toast.makeText(this, "Por favor, introduzca un número de teléfono español válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobamos si el enlace es válido (si no está vacío)
        if (!linkRestaurante.isEmpty() && !Patterns.WEB_URL.matcher(linkRestaurante).matches()) {
            Toast.makeText(this, "Por favor, introduzca un enlace válido", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (linkRestaurante.isEmpty()) {
                linkRestaurante = null;
            }
        }

        // Si todas las comprobaciones son correctas, se sube la imagen a Firebase Storage
        subirImagenFirebaseStorage(imagenRestaurante, nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion);
    }


    /**
     * Este método se encarga de subir la imagen del restaurante a Firebase Storage.
     *
     * @param imagenRestaurante      Bitmap de la imagen del restaurante a subir.
     * @param nombreRestaurante      Nombre del restaurante.
     * @param descripcionRestaurante Descripción del restaurante.
     * @param linkRestaurante        Enlace del restaurante.
     * @param horario                Horario del restaurante.
     * @param telefono               Teléfono del restaurante.
     * @param direccion              Dirección del restaurante.
     */
    public void subirImagenFirebaseStorage(Bitmap imagenRestaurante, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion) {
        // Creamos una referencia única para la imagen en Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenes_restaurante/" + UUID.randomUUID().toString() + ".jpg");

        // Convertimos la imagen del restaurante (que es un objeto Bitmap) en un array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagenRestaurante.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datosImagen = baos.toByteArray();

        // y lo subimos a Firebase Storage
        storageRef.putBytes(datosImagen)
                .addOnSuccessListener(taskSnapshot -> {
                    // si la imagen se sube con éxito, obtenemos su URL y se la añadimos al restaurante
                    obtenerURLImagenYAñadirAlRestaurante(storageRef, nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion);
                })
                .addOnFailureListener(exception -> {
                    Log.e("NuevoRestauranteActivity", "Error al subir la imagen a Firebase Storage: " + exception.getMessage());
                    Toast.makeText(NuevoRestauranteActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Este método se encarga de obtener la URL de la imagen que se acaba de subir a Firebase Storage y añadir la imagen al restaurante.
     *
     * @param storageRef             Referencia de almacenamiento de Firebase donde se encuentra la imagen.
     * @param nombreRestaurante      Nombre del restaurante.
     * @param descripcionRestaurante Descripción del restaurante.
     * @param linkRestaurante        Enlace del restaurante.
     * @param horario                Horario del restaurante.
     * @param telefono               Teléfono del restaurante.
     * @param direccion              Dirección del restaurante.
     */
    public void obtenerURLImagenYAñadirAlRestaurante(StorageReference storageRef, String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion) {
        storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // si obtenemos la URL de la imagen con éxito, creamos el nuevo restaurante con todos los datos y lo guardamos en Firestore
                    String urlImagen = uri.toString();
                    nuevoRestaurante(nombreRestaurante, descripcionRestaurante, linkRestaurante, horario, telefono, direccion, urlImagen);
                })
                .addOnFailureListener(exception -> {
                    Log.e("NuevoRestauranteActivity", "Error al obtener la URL de la imagen: " + exception.getMessage());
                    Toast.makeText(NuevoRestauranteActivity.this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Este método se encarga de crear un nuevo restaurante en Firestore con los datos ingresados por el usuario.
     *
     * @param nombreRestaurante      Nombre del restaurante.
     * @param descripcionRestaurante Descripción del restaurante.
     * @param linkRestaurante        Enlace del restaurante.
     * @param horario                Horario del restaurante.
     * @param telefono               Teléfono del restaurante.
     * @param direccion              Dirección del restaurante.
     * @param urlImagen              URL de la imagen del restaurante.
     */
    public void nuevoRestaurante(String nombreRestaurante, String descripcionRestaurante, String linkRestaurante, String horario, String telefono, String direccion, String urlImagen) {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String idUsuario = currentUser.getUid();

            restaurante = new Restaurante(nombreRestaurante, descripcionRestaurante, direccion, telefono, horario, linkRestaurante, urlImagen, idUsuario);

            // Creamos un nuevo objeto Map para guardar los datos del restaurante (solo si no son null para evitar problemas al guardar en Firestore)
            Map<String, Object> restauranteMap = new HashMap<>();
            if (nombreRestaurante != null) {
                restauranteMap.put("nombre", nombreRestaurante);
            }
            if (descripcionRestaurante != null) {
                restauranteMap.put("descripcion", descripcionRestaurante);
            }
            if (direccion != null) {
                restauranteMap.put("direccion", direccion);
            }
            if (telefono != null) {
                restauranteMap.put("telefono", telefono);
            }
            if (horario != null) {
                restauranteMap.put("horario", horario);
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
            restauranteMap.put("idUsuarioRestaurante", idUsuario);

            // Establecemos la puntuación en 0 porque al crearlo el restaurante no tiene puntuaciones
            double puntuacion = 0.0;
            restauranteMap.put("puntuacion", puntuacion);

            // Guardamos el nuevo restaurante en la colección "restaurantes" de Firestore
            db.collection("restaurantes")
                    .add(restauranteMap)
                    .addOnSuccessListener(documentReference -> {
                        String idRestaurante = documentReference.getId();
                        documentReference.update("idRestaurante", idRestaurante); // le damos al campo idRestaurante el valor del ID del documento

                        Toast.makeText(NuevoRestauranteActivity.this, "Restaurante guardado con éxito", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                        // Redirigimos al usuario al menú principal
                        Intent intent = new Intent(NuevoRestauranteActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NuevoRestauranteActivity", "Error al guardar el restaurante: " + e.getMessage());
                        Toast.makeText(NuevoRestauranteActivity.this, "Error al guardar el restaurante", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("NuevoRestauranteActivity", "currentUser es null");
        }

    }


    /**
     * Este método limpia los campos de la interfaz de usuario.
     */
    public void limpiarCampos() {
        ivImagenRestaurante.setImageDrawable(null);
        etNombreRestaurante.setText("");
        etDescripcionRestaurante.setText("");
        etLinkRestaurante.setText("");
        etHorarioRestaurante.setText("");
        etTelefonoRestaurante.setText("");
        etDireccionRestaurante.setText("");
    }


    /**
     * Este método redirige al usuario al menú principal de la aplicación.
     */
    public void volverMenu() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }


}