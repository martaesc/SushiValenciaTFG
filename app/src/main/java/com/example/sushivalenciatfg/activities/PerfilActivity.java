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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;

import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Esta es la clase PerfilActivity, que extiende AppCompatActivity.
 * Se encarga de gestionar el perfil del usuario.
 */
public class PerfilActivity extends AppCompatActivity {

    // Referencias a los elementos de la vista
    private TextInputLayout lyNombreUsuario;
    private TextInputLayout lyEmail;
    private TextView tvcontrasena;
    private CircleImageView ivfotoPerfil;
    private Button btnGuardarCambios;
    private Button btnVolver;
    private Spinner spinnerTipoUsuario;

    // Referencias a FirebaseAuth, FirebaseFirestore y FirebaseUser
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Declaración de los ActivityResultLauncher
    private ActivityResultLauncher<Intent> mGalleryResultLauncher;
    private ActivityResultLauncher<Intent> mCameraResultLauncher;


    // Getters y setters
    public void setFirebaseAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public void setCurrentUser(FirebaseUser currentUser) {
        this.currentUser = currentUser;
    }

    public TextInputLayout getLyNombreUsuario() {
        return lyNombreUsuario;
    }

    public TextInputLayout getLyEmail() {
        return lyEmail;
    }

    public Spinner getSpinnerTipoUsuario() {
        return spinnerTipoUsuario;
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
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        obtenerReferencias();
        inicializarActivityResultLaunchers();
        inicializarSpinnerTipoUsuario();

        obtenerDatosUsuario();
        abrirDialogoSeleccionImagen();


        btnGuardarCambios.setOnClickListener(v -> actualizarDatosUsuario());
        tvcontrasena.setOnClickListener(v -> enviarCorreoCambioContrasena());
        btnVolver.setOnClickListener(v -> volver());
    }


    /**
     * Este método se encarga de obtener las referencias a los elementos de la vista.
     */
    public void obtenerReferencias() {
        lyNombreUsuario = findViewById(R.id.usernameInputLayout);
        lyEmail = findViewById(R.id.emailInputLayout);
        tvcontrasena = findViewById(R.id.tvContrasena);
        ivfotoPerfil = findViewById(R.id.ivPhotoProfile);
        btnGuardarCambios = findViewById(R.id.saveButton);
        btnVolver = findViewById(R.id.backButton);
        spinnerTipoUsuario = findViewById(R.id.userTypeSpinner);
    }


    /**
     * Este método se encarga de inicializar los ActivityResultLauncher para la galería y la cámara.
     */
    public void inicializarActivityResultLaunchers() {
        mGalleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            ivfotoPerfil.setImageURI(uri);
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
                            ivfotoPerfil.setImageBitmap(imageBitmap);
                        } else {
                            Toast.makeText(this, "Error al obtener la imagen de la cámara", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }


    /**
     * Este método se encarga de inicializar el Spinner para seleccionar el tipo de usuario.
     */
    public void inicializarSpinnerTipoUsuario() {
        // Creamos un ArrayAdapter usando el array de strings que hemos predefinido(res/values/strings.xml) y un layout de spinner proporcionado por Android (vista de los ítem)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_usuario, android.R.layout.simple_spinner_item);

        // especificamos el layout a usar cuando la lista de opciones se despliega (layout proporcionado por Android)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTipoUsuario.setAdapter(adapter);
    }


    /**
     * Este método se encarga de abrir el diálogo de selección de imagen.
     */
    public void abrirDialogoSeleccionImagen() {
        ivfotoPerfil.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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


    /**
     * Este método se encarga de obtener los datos del usuario actual de la base de datos.
     */
    public void obtenerDatosUsuario() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            lyEmail.getEditText().setText(email);

            // Para obtener el nombre de usuario, el tipo de usuario y la URL de la imagen de perfil, necesitamos leerlos de Firestore
            db.collection("usuarios")
                    .whereEqualTo("uid", currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String nombreUsuario = document.getString("nombreUsuario");
                                lyNombreUsuario.getEditText().setText(nombreUsuario);

                                String tipoUsuario = document.getString("tipoUsuario");
                                // Obtenemos el array de tipos de usuario que hemos predefinido y buscamos el indice que coincide con el tipo de usuario devuelto por  Firestore
                                String[] arrayTiposUsuario = getResources().getStringArray(R.array.tipos_usuario);
                                for (int i = 0; i < arrayTiposUsuario.length; i++) {
                                    if (arrayTiposUsuario[i].equals(tipoUsuario)) {
                                        //  establecemmos ese índice como la selección del Spinner (mostrará el tipo de usuario actual del usuario)
                                        spinnerTipoUsuario.setSelection(i);
                                        break;
                                    }
                                }

                                // Comprobamos si el usuario tiene una imagen de perfil
                                if (document.contains("fotoPerfil") && !document.getString("fotoPerfil").isEmpty()) {
                                    String urlImagen = document.getString("fotoPerfil");
                                    // Comprobamos si la actividad aún está en ejecución antes de cargar la imagen
                                    if (!isFinishing()) {
                                        // Cargamos la imagen en el CircleImageView usando Glide
                                        Glide.with(this)
                                                .load(urlImagen)
                                                .into(ivfotoPerfil);
                                    }
                                } else {
                                    // sino establecemos la imagen por defecto
                                    ivfotoPerfil.setImageResource(R.drawable.foto_perfil_defecto);
                                }
                            }
                        } else {
                            Log.e("Firestore", "Error al obtener los datos del usuario: ", task.getException());
                        }
                    });
        } else {
            Log.e("Firebase Auth", "No se ha encontrado el usuario actual");
        }
    }


    /**
     * Este método  se utiliza para actualizar la información de un usuario, con o sin imagen, al pulsar el botón de guardar.
     */
    public void actualizarDatosUsuario() {
        String nombreUsuario = lyNombreUsuario.getEditText().getText().toString();
        String correo = lyEmail.getEditText().getText().toString();
        String tipoUsuario = spinnerTipoUsuario.getSelectedItem().toString();

        // Comprobar si los campos de texto están vacíos
        if (nombreUsuario.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "No puede quedar ningún campo vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si el nombre de usuario ya pertenece a alguien que no sea ek usuario actual (para evitar que salte el error por tener el propio usuario ya ese nombre)
        db.collection("usuarios")
                .whereEqualTo("nombreUsuario", nombreUsuario)
                .whereNotEqualTo("uid", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //si el nombre de usuario no está en uso por otro usuario
                        if (task.getResult().isEmpty()) {
                            //si ha seleccionado una imagen, actualizar el usuario con la imagen
                            if (ivfotoPerfil.getDrawable() != null) {
                                actualizarUsuarioConImagen(nombreUsuario, correo, tipoUsuario);
                            } else {
                                // Si no tiene una imagen, simplemente actualizar los otros campos del usuario
                                actualizarUsuarioEnFirestore(nombreUsuario, correo, tipoUsuario, null);
                            }

                            // si el correo es diferente al actual, actualizar el correo en FirebaseAuth
                            if (!correo.equals(currentUser.getEmail())) {
                                actualizarCorreoEnAuth(correo);
                            }

                        } else {
                            Toast.makeText(this, "El nombre de usuario ya existe, por favor elige otro", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Error al comprobar el nombre de usuario: ", task.getException());
                    }
                });
    }


    /**
     * Este método se encarga de actualizar el usuario con una imagen.
     *
     * @param nombreUsuario El nombre de usuario ingresado por el usuario.
     * @param correo        El correo electrónico ingresado por el usuario.
     * @param tipoUsuario   El tipo de usuario seleccionado por el usuario.
     */
    public void actualizarUsuarioConImagen(String nombreUsuario, String correo, String tipoUsuario) {
        // Convertimos la imagen en un Bitmap para poder comprimirla
        BitmapDrawable drawable = (BitmapDrawable) ivfotoPerfil.getDrawable();
        Bitmap fotoPerfil = drawable.getBitmap();

        // Comprimimos la imagen en un ByteArrayOutputStream (formato requerido por el método putBytes() de Firebase Storage para subir la imagen)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fotoPerfil.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datosImagen = baos.toByteArray();

        // Subimos la imagen a Firebase Storage y obtenemos su URL
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenes_usuario/" + UUID.randomUUID().toString() + ".jpg");
        storageRef.putBytes(datosImagen)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String urlImagen = uri.toString();
                            actualizarUsuarioEnFirestore(nombreUsuario, correo, tipoUsuario, urlImagen);

                            // Actualizamos la foto de perfil en los comentarios que haya hecho el usuario
                            actualizarFotoPerfilComentarios(currentUser.getUid(), urlImagen);

                        })
                        .addOnFailureListener(exception -> {
                            Log.e("Firebase Storage", "Error al obtener la URL de la imagen: " + exception.getMessage());
                            Toast.makeText(this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(exception -> {
                    Log.e("Firebase Storage", "Error al subir la imagen: " + exception.getMessage());
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Este método se encarga de actualizar la foto de perfil en los comentarios que haya hecho el usuario.
     *
     * @param idUsuario    El ID del usuario.
     * @param nuevaUrlFoto La nueva URL de la imagen de perfil.
     */
    public void actualizarFotoPerfilComentarios(String idUsuario, String nuevaUrlFoto) {
        // Buscamos todos los comentarios que el usuario ha hecho
        db.collection("comentarios")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // y actualizamos la foto de perfil en cada comentario
                            db.collection("comentarios").document(document.getId())
                                    .update("fotoPerfil", nuevaUrlFoto);
                        }
                    }
                });
    }


    /**
     * Este método se encarga de actualizar el usuario en Firestore.
     *
     * @param nombreUsuario El nombre de usuario ingresado por el usuario.
     * @param correo        El correo electrónico ingresado por el usuario.
     * @param tipoUsuario   El tipo de usuario seleccionado por el usuario.
     * @param urlImagen     La URL de la imagen del usuario.
     */
    public void actualizarUsuarioEnFirestore(String nombreUsuario, String correo, String tipoUsuario, String urlImagen) {
        //creamos un map con los datos del usuario a actualizar en Firestore
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombreUsuario", nombreUsuario);
        usuario.put("correo", correo);
        usuario.put("tipoUsuario", tipoUsuario);
        if (urlImagen != null) {
            usuario.put("fotoPerfil", urlImagen);
        }

        // Buscamos el documento donde el campo 'uid' es igual al UID del usuario actual
        db.collection("usuarios")
                .whereEqualTo("uid", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Obtenemos el ID del primer documento que coincide con la consulta
                        String documentId = task.getResult().getDocuments().get(0).getId();

                        // y actualizamos el documento con el ID obtenido
                        db.collection("usuarios").document(documentId)
                                .update(usuario)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show();
                                    // Refrescamos los datos del usuario en la interfaz
                                    obtenerDatosUsuario();

                                    // Actualizamos el nombre de usuario en los comentarios que haya hecho el usuario
                                    actualizarNombreUsuarioComentarios(currentUser.getUid(), nombreUsuario);


                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error al actualizar el usuario: " + e.getMessage());
                                    Toast.makeText(this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("Firestore", "No se encontró un documento con el UID del usuario actual");
                    }
                });
    }


    /**
     * Este método se encarga de actualizar el nombre de usuario en los comentarios que haya hecho el usuario.
     *
     * @param idUsuario          El ID del usuario.
     * @param nuevoNombreUsuario El nuevo nombre de usuario.
     */
    public void actualizarNombreUsuarioComentarios(String idUsuario, String nuevoNombreUsuario) {
        // Buscamos todos los comentarios que el usuario ha hecho
        db.collection("comentarios")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // y actualizamos el nombre de usuario en cada comentario
                            db.collection("comentarios").document(document.getId())
                                    .update("nombreUsuario", nuevoNombreUsuario);
                        }
                    }
                });
    }


    /**
     * Este método se encarga de actualizar el correo electrónico en FirebaseAuth.
     *
     * @param correo El correo electrónico ingresado por el usuario.
     */
    public void actualizarCorreoEnAuth(String correo) {
        // Enviar el correo de verificación al nuevo correo
        currentUser.verifyBeforeUpdateEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de verificación enviado. Por favor, revise su bandeja de entrada.", Toast.LENGTH_LONG).show();

                        // Iniciamos LoginActivity para que inicie sesión con el nuevo correo
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("FirebaseAuth", "Error al enviar el correo de verificación: ", task.getException());
                        Toast.makeText(this, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Este método se encarga de enviar un correo para cambiar la contraseña.
     */
    public void enviarCorreoCambioContrasena() {
        mAuth.sendPasswordResetEmail(currentUser.getEmail())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PerfilActivity.this, "Correo para cambiar la contraseña enviado. Por favor, revise su bandeja de entrada.", Toast.LENGTH_LONG).show();

                        // Iniciar LoginActivity para que inicie sesión con la nueva contraseña
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PerfilActivity.this, "Error al enviar el correo para cambiar la contraseña", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Este método se encarga de volver a la actividad principal.
     */
    public void volver() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}