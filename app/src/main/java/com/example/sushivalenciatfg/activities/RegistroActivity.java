package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.models.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout txtRegistroUsuario, txtRegistroCorreo, txtLoginContraseña, txtLoginContraseña2;
    private RadioGroup radioGroup;

    private MaterialButton btnRegistro;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //inicializar instancia de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        obtenerReferencias();

        btnRegistro.setOnClickListener(v -> {
            registroUsuario();
        });
    }


    //método obtener referencia a los elementos de la vista
    public void obtenerReferencias() {
        txtRegistroUsuario = findViewById(R.id.textInputLayoutNombre);
        txtRegistroCorreo = findViewById(R.id.textInputLayoutCorreo);
        txtLoginContraseña = findViewById(R.id.textInputLayoutContraseña);
        txtLoginContraseña2 = findViewById(R.id.textInputLayoutContraseña2);
        radioGroup = findViewById(R.id.radioGroupTipoUsuario);
        btnRegistro = findViewById(R.id.btnRegistro);

    }

    public void registroUsuario() {
    String nombreUsuario = txtRegistroUsuario.getEditText().getText().toString();
    String correo = txtRegistroCorreo.getEditText().getText().toString();
    String contraseña = txtLoginContraseña.getEditText().getText().toString();
    String contraseña2 = txtLoginContraseña2.getEditText().getText().toString();
    int usuarioSeleccionado = radioGroup.getCheckedRadioButtonId();

    if (usuarioSeleccionado == -1) {
        Toast.makeText(RegistroActivity.this, "Debe seleccionar un tipo de usuario", Toast.LENGTH_SHORT).show();
        return;
    }

    RadioButton radioButton = findViewById(usuarioSeleccionado);
    String tipoUsuarioString = radioButton.getText().toString();

    if (nombreUsuario.isEmpty() || correo.isEmpty() || contraseña.isEmpty() || contraseña2.isEmpty()) {
        Toast.makeText(RegistroActivity.this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
    } else if (!contraseña.equals(contraseña2)) {
        Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
    } else {
        gestionRegistroUsuario(nombreUsuario, correo, contraseña, tipoUsuarioString);
    }
}

    //método para registrar un usuario en Firebase
    public void gestionRegistroUsuario(String nombreUsuario, String correo, String contraseña, String tipoUsuario) {
        mAuth.createUserWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String idUsuario = mAuth.getCurrentUser().getUid();
                        Usuario user = new Usuario(idUsuario, nombreUsuario, correo, tipoUsuario);
                        db.collection("usuarios")
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    // Start LoginActivity
                                    Intent intent = new Intent(this, LoginActivity.class);
                                    startActivity(intent);
                                    finish(); // Cerrar la actividad actual
                                })
                                .addOnFailureListener(e -> Log.e("RegistroActivity", "Error al registrar en Firestore: " + e.getMessage()));
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegistroActivity.this, "Ya existe una cuenta con este correo electrónico", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(RegistroActivity.this, "La contraseña es demasiado débil", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(RegistroActivity.this, "El correo electrónico es inválido", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistroActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}