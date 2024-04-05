package com.example.sushivalenciatfg.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sushivalenciatfg.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilActivity extends AppCompatActivity {

    private TextInputLayout lyNombreUsuario;
    private TextInputLayout lyEmail;
    private TextView tvcontrasena;
    private TextInputLayout lyTipoUsuario;

    private CircleImageView ivfotoPerfil;

    private Button btnGuardarCambios;
    private Button btnVolver;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Declaración de los ActivityResultLauncher
    private ActivityResultLauncher<Intent> mGalleryResultLauncher;
    private ActivityResultLauncher<Intent> mCameraResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inicializarActivityResultLaunchers();
        obtenerReferencias();
        obtenerDatosUsuario();
        abrirDialogoSeleccionImagen();

        btnGuardarCambios.setOnClickListener(v -> actualizarDatosUsuario());
        tvcontrasena.setOnClickListener(v -> enviarCorreoCambioContrasena());
        btnVolver.setOnClickListener(v -> volver());
    }


    public void obtenerReferencias() {
        lyNombreUsuario = findViewById(R.id.usernameInputLayout);
        lyEmail = findViewById(R.id.emailInputLayout);
        tvcontrasena = findViewById(R.id.tvContrasena);
        lyTipoUsuario = findViewById(R.id.userTypeInputLayout);

        ivfotoPerfil = findViewById(R.id.ivPhotoProfile);

        btnGuardarCambios = findViewById(R.id.saveButton);
        btnVolver = findViewById(R.id.backButton);
    }

    public void inicializarActivityResultLaunchers() {
        // Inicialización de los ActivityResultLauncher para la galería y la cámara
        mGalleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        ivfotoPerfil.setImageURI(uri);
                    }
                }
        );

        mCameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivfotoPerfil.setImageBitmap(imageBitmap);
                    }
                }
        );
    }

    // Método para abrir el diálogo de selección de imagen
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

    // Método para obtener los datos del usuario actual
    // (al estar guardando el nombre de usuario en Firestore cuando el usuario se registra, debo buscarlo Firestore en lugar de FirebaseUser,
    // ya que en este ultimo solamente se guarda el email y la contraseña)
    public void obtenerDatosUsuario() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            lyEmail.getEditText().setText(email);

            // Para obtener el nombre de usuario, el tipo de usuario y la URL de la imagen, necesitamos leerlos de Firestore
            db.collection("usuarios")
                    .whereEqualTo("uid", currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String nombreUsuario = document.getString("nombreUsuario");
                                Log.d("Firestore", "Nombre de usuario obtenido: " + nombreUsuario);
                                lyNombreUsuario.getEditText().setText(nombreUsuario);

                                String tipoUsuario = document.getString("tipoUsuario");
                                lyTipoUsuario.getEditText().setText(tipoUsuario);

                                // Comprobar si el usuario tiene una imagen de perfil
                                if (document.contains("fotoPerfil") && !document.getString("fotoPerfil").isEmpty()) {
                                    String urlImagen = document.getString("fotoPerfil");
                                    // Cargamos la imagen en el CircleImageView usando Glide
                                    Glide.with(this)
                                            .load(urlImagen)
                                            .into(ivfotoPerfil);
                                } else {
                                    // Establecer la imagen por defecto
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

    public void actualizarDatosUsuario() {
        String nombreUsuario = lyNombreUsuario.getEditText().getText().toString();
        String correo = lyEmail.getEditText().getText().toString();
        String tipoUsuario = lyTipoUsuario.getEditText().getText().toString();

        // Comprobar si los campos están vacíos
        if (nombreUsuario.isEmpty() || correo.isEmpty() || tipoUsuario.isEmpty()) {
            Toast.makeText(this, "No puede quedar ningún campo vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Comprobar si el nombre de usuario ya existe y no pertenece al usuario actual (para evitar que salte el error por tener el propio usuario ya ese nombre)
        db.collection("usuarios")
                .whereEqualTo("nombreUsuario", nombreUsuario)
                .whereNotEqualTo("uid", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // El nombre de usuario no existe o pertenece al usuario actual, continuar con la actualización
                            if (ivfotoPerfil.getDrawable() != null) {
                                actualizarUsuarioConImagen(nombreUsuario, correo, tipoUsuario);
                            } else {
                                // Si no tiene una imagen, simplemente actualizar los otros campos del usuario
                                actualizarUsuarioEnFirestore(nombreUsuario, correo, tipoUsuario, null);
                            }

                            // Comprobar si el correo electrónico ha sido modificado
                            if (!correo.equals(currentUser.getEmail())) {
                                // Llamar al método para actualizar el correo electrónico en FirebaseAuth
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

    public void actualizarUsuarioConImagen(String nombreUsuario, String correo, String tipoUsuario) {
        // Convertir la imagen en un Bitmap
        BitmapDrawable drawable = (BitmapDrawable) ivfotoPerfil.getDrawable();
        Bitmap fotoPerfil = drawable.getBitmap();

        // Comprimir la imagen en un ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fotoPerfil.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datosImagen = baos.toByteArray();

        // Subir la imagen a Firebase Storage y obtener la URL
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenes_usuario/" + UUID.randomUUID().toString() + ".jpg");
        storageRef.putBytes(datosImagen)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String urlImagen = uri.toString();
                            actualizarUsuarioEnFirestore(nombreUsuario, correo, tipoUsuario, urlImagen);
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

    public void actualizarUsuarioEnFirestore(String nombreUsuario, String correo, String tipoUsuario, String urlImagen) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombreUsuario", nombreUsuario);
        usuario.put("correo", correo);
        usuario.put("tipoUsuario", tipoUsuario);
        if (urlImagen != null) {
            usuario.put("fotoPerfil", urlImagen);
        }

        // Buscar el documento donde el campo 'uid' es igual al UID del usuario actual
        db.collection("usuarios")
                .whereEqualTo("uid", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Obtener el ID del primer documento que coincide con la consulta
                        String documentId = task.getResult().getDocuments().get(0).getId();

                        // Actualizar el documento con el ID obtenido
                        db.collection("usuarios").document(documentId)
                                .update(usuario)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show();
                                    // Recargar los datos del usuario en la interfaz
                                    obtenerDatosUsuario();

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

    // Método para actualizar el correo electrónico en FirebaseAuth
    public void actualizarCorreoEnAuth(String correo) {
        // Enviar el correo de verificación al nuevo correo
        currentUser.verifyBeforeUpdateEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de verificación enviado. Por favor, revise su bandeja de entrada.", Toast.LENGTH_LONG).show();

                        // Iniciar LoginActivity para que inicie sesión con el nuevo correo
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("FirebaseAuth", "Error al enviar el correo de verificación: ", task.getException());
                        Toast.makeText(this, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Método para enviar un correo para cambiar la contraseña
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

    //para volver a la pantalla anterior
    public void volver() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}