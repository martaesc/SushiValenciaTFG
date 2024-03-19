package com.example.sushivalenciatfg.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout txtLoginNombreOCorreo, txtLoginContraseña;
    private MaterialButton btnLogin;

    private TextView tvIrARegistro;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String tipoUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        obtenerReferencias();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String nombreOCorreoUsuario = txtLoginNombreOCorreo.getEditText().getText().toString();
            String contraseñaUsuario = txtLoginContraseña.getEditText().getText().toString();

            if (nombreOCorreoUsuario.isEmpty() || contraseñaUsuario.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                login(nombreOCorreoUsuario, contraseñaUsuario);
            }
        });

        tvIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
        });

    }

    //método obtener referencia a los elementos de la vista
    public void obtenerReferencias() {
        txtLoginNombreOCorreo = findViewById(R.id.txtInputLoginNombreOEmail);
        txtLoginContraseña = findViewById(R.id.txtInputLoginContraseña);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrARegistro = findViewById(R.id.textViewBtnRegistro);
    }

    //Cuando un usuario intenta iniciar sesión, primero buscamos el documento con el nombre de usuario ingresado,
    // obtenemos el correo electrónico de ese documento y luego usar ese correo electrónico para iniciar sesión con Firebase Authentication.
    public void login(String nombreUsuarioOEmail, String contraseña) {
        db.collection("usuario")
                .whereEqualTo("nombreUsuario", nombreUsuarioOEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String email = task.getResult().getDocuments().get(0).getString("correo");
                        signInWithFirebase(email, contraseña);
                    } else {
                        // Si se escribe el correo, firebase iniciara sesión con él directamente
                        signInWithFirebase(nombreUsuarioOEmail, contraseña);
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
                            Toast.makeText(LoginActivity.this, "No existe una cuenta con este nombre de usuario/correo electrónico", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Nombre de usuario/correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}