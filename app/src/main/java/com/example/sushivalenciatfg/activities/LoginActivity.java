package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

/**
 * Esta es la clase LoginActivity, que extiende AppCompatActivity.
 * Se encarga de gestionar el inicio de sesión del usuario.
 */
public class LoginActivity extends AppCompatActivity {

    // Referencias a los elementos de la vista
    private TextInputLayout txtLoginNombreOCorreo;
    private TextInputLayout txtLoginContrasena;
    private MaterialButton btnLogin;
    private TextView tvIrARegistro;
    private TextView tvOlvidarContrasena;

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

    public TextInputLayout getTxtLoginNombreOCorreo() {

        return txtLoginNombreOCorreo;
    }

    public TextInputLayout getTxtLoginContrasena() {

        return txtLoginContrasena;
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


    /**
     * Este método se encarga de obtener las referencias a los elementos de la vista.
     */
    public void obtenerReferencias() {
        txtLoginNombreOCorreo = findViewById(R.id.txtInputLoginNombreOEmail);
        txtLoginContrasena = findViewById(R.id.txtInputLoginContraseña);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrARegistro = findViewById(R.id.textViewBtnRegistro);
        tvOlvidarContrasena = findViewById(R.id.textViewBtnRecuperarContrasena);
    }


    /**
     * Este método se encarga de iniciar la sesión del usuario.
     */
    public void iniciarSesion() {
        String nombreOCorreoUsuario = txtLoginNombreOCorreo.getEditText().getText().toString();
        String contrasenaUsuario = txtLoginContrasena.getEditText().getText().toString();

        if (nombreOCorreoUsuario.isEmpty() || contrasenaUsuario.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            gestionInicioSesion(nombreOCorreoUsuario, contrasenaUsuario);
        }
    }


    /**
     * Este método se encarga de gestionar el inicio de sesión del usuario.
     *
     * @param nombreUsuarioOEmail El nombre de usuario o correo electrónico ingresado por el usuario.
     * @param contrasena          La contraseña ingresada por el usuario.
     */
    public void gestionInicioSesion(String nombreUsuarioOEmail, String contrasena) {
        db.collection("usuarios")
                .whereEqualTo("nombreUsuario", nombreUsuarioOEmail)
                .get()
                .addOnCompleteListener(task -> {
                    //si es el username se busca el correo asociado a ese usuario para iniciar sesión
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String email = task.getResult().getDocuments().get(0).getString("correo");
                        signInWithFirebase(email, contrasena);
                    } else {
                        // Si se escribe el correo, firebase iniciara sesión con él directamente
                        signInWithFirebase(nombreUsuarioOEmail, contrasena);
                    }
                });
    }


    /**
     * Este método se encarga de iniciar sesión con Firebase.
     *
     * @param email    El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     */
    public void signInWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        // Si el inicio de sesión es exitoso, se inicia la actividad principal
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else { // no especificamos si es el correo electrónico o la contraseña, en línea con las mejores prácticas de seguridad
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Las credenciales proporcionadas son incorrectas. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * Este método se encarga de cambiar la contraseña del usuario.
     */
    public void cambioContrasena() {
        String nombreUsuario = txtLoginNombreOCorreo.getEditText().getText().toString();
        if (nombreUsuario.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor, ingrese su nombre de usuario o correo electrónico en el primer campo", Toast.LENGTH_SHORT).show();
        } else {
            // se ha escrito algo en el campo de texto
            db.collection("usuarios")
                    .whereEqualTo("nombreUsuario", nombreUsuario)
                    .get()
                    .addOnCompleteListener(task -> {
                        //si es el username se busca el correo asociado a este para enviar el correo de cambio de contraseña
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String email = task.getResult().getDocuments().get(0).getString("correo");
                            gestionCorreoCambioContrasena(email);
                        } else {
                            // Si se ha escrito el correo, se le envía el mensaje de cambio de contraseña directamente
                            gestionCorreoCambioContrasena(nombreUsuario);
                        }
                    });
        }

    }


    /**
     * Este método se encarga de gestionar el cambio de contraseña del usuario.
     *
     * @param email El correo electrónico del usuario.
     */
 public void gestionCorreoCambioContrasena(String email) {
     // creamos un diálogo de progreso para mostrar al usuario mientras se está enviando el correo de cambio de contraseña (por si el proceso tardara un poco)
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setView(R.layout.layout_dialogo_progreso);
    final AlertDialog dialog = builder.create();

    dialog.show();

    mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Correo para cambiar la contraseña enviado. Por favor, revise su bandeja de entrada.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error al enviar el correo para cambiar la contraseña. Por favor, revise los datos introducidos.", Toast.LENGTH_LONG).show();
                }
            });
}


    /**
     * Este método se encarga de redirigir al usuario a la actividad de registro.
     */
    public void irARegistro() {
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }


}