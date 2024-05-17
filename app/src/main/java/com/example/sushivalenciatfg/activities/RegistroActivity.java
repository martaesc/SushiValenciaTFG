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

/**
 * Esta es la clase RegistroActivity, que extiende AppCompatActivity.
 * Se encarga de gestionar el registro de un nuevo usuario.
 */
public class RegistroActivity extends AppCompatActivity {

    // Referencias a los elementos de la vista
    private TextInputLayout txtRegistroUsuario, txtRegistroCorreo, txtLoginContraseña, txtLoginContraseña2;
    private RadioGroup radioGroup;
    private MaterialButton btnRegistro;

    // Referencias a FirebaseAuth y FirebaseFirestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    //setters útiles para las pruebas (permiten inyectar instancias simuladas de FirebaseAuth y FirebaseFirestore)
    public void setFirebaseAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public void setFirestore(FirebaseFirestore db) {
        this.db = db;
    }


    /**
     * Este método se llama cuando la actividad está iniciando.
     * Inicializa la actividad y establece el onClickListener para el botón de registro.
     *
     * @param savedInstanceState Si la actividad se está reinicializando después de haber sido cerrada previamente
     *                           entonces este Bundle contiene los datos que suministró más recientemente en onSaveInstanceState(Bundle).
     *                           Nota: De lo contrario, es nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        obtenerReferencias();

        btnRegistro.setOnClickListener(v -> {
            registroUsuario();
        });
    }


    /**
     * Este método se encarga de obtener las referencias a los elementos de la vista.
     */
    public void obtenerReferencias() {
        txtRegistroUsuario = findViewById(R.id.textInputLayoutNombre);
        txtRegistroCorreo = findViewById(R.id.textInputLayoutCorreo);
        txtLoginContraseña = findViewById(R.id.textInputLayoutContraseña);
        txtLoginContraseña2 = findViewById(R.id.textInputLayoutContraseña2);
        radioGroup = findViewById(R.id.radioGroupTipoUsuario);
        btnRegistro = findViewById(R.id.btnRegistro);
    }


    /**
     * Este método se encarga de registrar un nuevo usuario.
     */
    public void registroUsuario() {
        String nombreUsuario = txtRegistroUsuario.getEditText().getText().toString();
        String correo = txtRegistroCorreo.getEditText().getText().toString();
        String contraseña = txtLoginContraseña.getEditText().getText().toString();
        String contraseña2 = txtLoginContraseña2.getEditText().getText().toString();

        // Primero, verificamos si los campos de texto están vacíos
        if (nombreUsuario.isEmpty() || correo.isEmpty() || contraseña.isEmpty() || contraseña2.isEmpty()) {
            Toast.makeText(RegistroActivity.this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int usuarioSeleccionado = radioGroup.getCheckedRadioButtonId();

        // Luego, verificamos si se ha seleccionado un tipo de usuario
        if (usuarioSeleccionado == -1) {
            Toast.makeText(RegistroActivity.this, "Debe seleccionar un tipo de usuario", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton radioButton = findViewById(usuarioSeleccionado);
        String tipoUsuarioString = radioButton.getText().toString();

        // Finalmente, verificamos si las contraseñas coinciden
        if (!contraseña.equals(contraseña2)) {
            Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        } else {
            // Verificamos si el nombre de usuario ya está en uso
            db.collection("usuarios")
                    .whereEqualTo("nombreUsuario", nombreUsuario)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            Toast.makeText(RegistroActivity.this, "El nombre de usuario ya está en uso. Por favor, introduzca uno diferente.", Toast.LENGTH_LONG).show();
                        } else {
                            gestionRegistroUsuario(nombreUsuario, correo, contraseña, tipoUsuarioString);
                        }
                    });
        }
    }

    /**
     * Este método se encarga de gestionar el registro de un nuevo usuario en Firebase.
     *
     * @param nombreUsuario El nombre de usuario ingresado por el usuario.
     * @param correo        El correo electrónico ingresado por el usuario.
     * @param contraseña    La contraseña ingresada por el usuario.
     * @param tipoUsuario   El tipo de usuario seleccionado por el usuario.
     */
    public void gestionRegistroUsuario(String nombreUsuario, String correo, String contraseña, String tipoUsuario) {
        // llamamos al metodo createUserWithEmailAndPassword de FirebaseAuth para registrar al usuario
        mAuth.createUserWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(this, task -> {
                    // si el registro se completa con éxito, guardamos los datos del usuario en la colección "usuarios" de Firestore
                    if (task.isSuccessful()) {
                        String idUsuario = mAuth.getCurrentUser().getUid();
                        Usuario user = new Usuario(idUsuario, nombreUsuario, correo, tipoUsuario);
                        db.collection("usuarios")
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(this, LoginActivity.class);
                                    startActivity(intent);
                                    finish(); // Cerramos la actividad actual
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