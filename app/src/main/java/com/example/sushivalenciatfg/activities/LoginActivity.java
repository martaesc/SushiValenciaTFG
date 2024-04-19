package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout txtLoginNombreOCorreo;
    private TextInputLayout txtLoginContrasena;
    private MaterialButton btnLogin;

    private TextView tvIrARegistro;
    private TextView tvOlvidarContrasena;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    //setters útiles para las pruebas, ya que permiten inyectar instancias simuladas de FirebaseAuth y FirebaseFirestore en LoginActivity.
    public void setFirebaseAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public void setFirestore(FirebaseFirestore db) {
        this.db = db;
    }

    //getters para poder acceder a los campos desde el entorno de pruebas
    public TextInputLayout getTxtLoginNombreOCorreo() {
        return txtLoginNombreOCorreo;
    }
    public TextInputLayout getTxtLoginContrasena() {
        return txtLoginContrasena;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        obtenerReferencias();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            iniciarSesion();
        });

        tvIrARegistro.setOnClickListener(v -> {
            irARegistro();
        });


        tvOlvidarContrasena.setOnClickListener(v -> {
            cambioContrasena();
        });


    }

    //método obtener referencia a los elementos de la vista
    public void obtenerReferencias() {
        txtLoginNombreOCorreo = findViewById(R.id.txtInputLoginNombreOEmail);
        txtLoginContrasena = findViewById(R.id.txtInputLoginContraseña);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrARegistro = findViewById(R.id.textViewBtnRegistro);
        tvOlvidarContrasena = findViewById(R.id.textViewBtnRecuperarContrasena);
    }

    public void iniciarSesion() {
        String nombreOCorreoUsuario = txtLoginNombreOCorreo.getEditText().getText().toString();
        String contrasenaUsuario = txtLoginContrasena.getEditText().getText().toString();

        if (nombreOCorreoUsuario.isEmpty() || contrasenaUsuario.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            gestionInicioSesion(nombreOCorreoUsuario, contrasenaUsuario);
        }
    }

    //Cuando un usuario intenta iniciar sesión, primero buscamos el documento con el nombre de usuario ingresado,
    // obtenemos el correo electrónico de ese documento y luego usar ese correo electrónico para iniciar sesión con Firebase Authentication.
    public void gestionInicioSesion(String nombreUsuarioOEmail, String contrasena) {
        db.collection("usuarios")
                .whereEqualTo("nombreUsuario", nombreUsuarioOEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String email = task.getResult().getDocuments().get(0).getString("correo");
                        signInWithFirebase(email, contrasena);
                    } else {
                        // Si se escribe el correo, firebase iniciara sesión con él directamente
                        signInWithFirebase(nombreUsuarioOEmail, contrasena);
                    }
                });
    }

    public void signInWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        // Si el inicio de sesión es exitoso, se inicia la actividad principal
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(LoginActivity.this, "No existe una cuenta con este nombre de usuario/correo electrónico", Toast.LENGTH_LONG).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Nombre de usuario/correo electrónico o contraseña incorrectos", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //se tiene en cuenta si el usuario ha rellenado el campo con el nombre de usuario o con el correo electrónico
    public void cambioContrasena() {
        String nombreUsuario = txtLoginNombreOCorreo.getEditText().getText().toString();
        if (nombreUsuario.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor, ingrese su nombre de usuario o correo electrónico en el primer campo", Toast.LENGTH_SHORT).show();
        } else {
            // Si se ha escrito algo
            db.collection("usuarios")
                    .whereEqualTo("nombreUsuario", nombreUsuario)
                    .get()
                    .addOnCompleteListener(task -> {
                        //si es el username se busca el correo asociado a ese usuario
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String email = task.getResult().getDocuments().get(0).getString("correo");
                            gestionCorreoCambioContrasena(email);
                        } else {
                            // Si se ha escrito el correo directamente
                            gestionCorreoCambioContrasena(nombreUsuario);
                        }
                    });
        }

    }

    public void gestionCorreoCambioContrasena(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Correo para cambiar la contraseña enviado. Por favor, revise su bandeja de entrada.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al enviar el correo para cambiar la contraseña", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void irARegistro() {
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }


}