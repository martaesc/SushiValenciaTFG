package com.example.sushivalenciatfg;

import android.content.Intent;
import android.widget.Button;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.activities.LoginActivity;
import com.example.sushivalenciatfg.activities.MainActivity;
import com.example.sushivalenciatfg.activities.RegistroActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    private LoginActivity loginActivity;
    private FirebaseAuth mockAuth;

    private FirebaseFirestore mockFirestore;

    @Before
    public void setUp() {
        // creamos una instancia simulada de FirebaseAuth y la configuramos para que el método getCurrentUser() devuelva null.
        mockAuth = mock(FirebaseAuth.class);
        when(mockAuth.getCurrentUser()).thenReturn(null);

        mockFirestore = mock(FirebaseFirestore.class);  // Inicializa mockFirestore

        //luego creamos una instancia de LoginActivity utilizando Robolectric
        loginActivity = Robolectric.buildActivity(LoginActivity.class).create().get();

        // Inyectamos las instancias simuladas de FirebaseAuth y FirebaseFirestore en LoginActivity
        loginActivity.setFirebaseAuth(mockAuth);
        loginActivity.setFirestore(mockFirestore);
    }

    /**
     * Test para verificar que al hacer click en el botón de login se inicie la actividad MainActivity.
     */
    @Test
    public void alHacerClicEnLogin_deberiaIniciarMainActivity() {
        Button button = loginActivity.findViewById(R.id.btnLogin);
        button.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(loginActivity);
        Intent expectedIntent = new Intent(loginActivity, MainActivity.class);
        Intent actualIntent = shadowActivity.getNextStartedActivity();

        assertTrue(expectedIntent.filterEquals(actualIntent));
    }


    /**
     * Test para verificar que al hacer click en el botón de registro se inicie la actividad RegistroActivity.
     */
    @Test
    public void alHacerClicEnRegistro_deberiaIniciarRegistroActivity() {
        Button button = loginActivity.findViewById(R.id.textViewBtnRegistro);
        button.performClick();

        ShadowActivity shadowActivity = Shadows.shadowOf(loginActivity);
        Intent expectedIntent = new Intent(loginActivity, RegistroActivity.class);
        Intent actualIntent = shadowActivity.getNextStartedActivity();

        assertTrue(expectedIntent.filterEquals(actualIntent));
    }


    /**
     * Test para comprobar que al hacer clic en el botón de inicio de sesión con los campos de nombre de usuario y contraseña llenos, se inicia la actividad MainActivity.
     */
    @Test
    public void alHacerClicEnLogin_conCamposLlenos_deberiaIniciarMainActivity() {
        // Obtenemos las referencias a los campos de texto y al botón de inicio de sesión
        TextInputLayout txtLoginNombreOCorreo = loginActivity.findViewById(R.id.txtInputLoginNombreOEmail);
        TextInputLayout txtLoginContrasena = loginActivity.findViewById(R.id.txtInputLoginContraseña);
        Button btnLogin = loginActivity.findViewById(R.id.btnLogin);

        // Establecemos los campos de texto con valores válidos
        txtLoginNombreOCorreo.getEditText().setText("nombreUsuarioValido");
        txtLoginContrasena.getEditText().setText("contrasenaValida");

        // Simulamos un clic en el botón de inicio de sesión
        btnLogin.performClick();

        // Obtenemos la próxima actividad que se inició
        ShadowActivity shadowActivity = Shadows.shadowOf(loginActivity);
        Intent expectedIntent = new Intent(loginActivity, MainActivity.class);
        Intent actualIntent = shadowActivity.getNextStartedActivity();

        // Verificamos que la actividad que se inició es MainActivity
        assertTrue(expectedIntent.filterEquals(actualIntent));
    }


    /**
     * Prueba para verificar que, después de un inicio de sesión exitoso, se inicia la actividad MainActivity y se finaliza
     * la actividad LoginActivity.
     */
    @Test
    public void alIniciarSesionExitosamente_deberiaIniciarMainActivityYFinalizarLoginActivity() {
        // Configura FirebaseAuth para simular un inicio de sesión exitoso
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(Tasks.forResult(null));

        // Llena los campos de nombre de usuario y contraseña
        TextInputLayout txtLoginNombreOCorreo = loginActivity.findViewById(R.id.txtInputLoginNombreOEmail);
        TextInputLayout txtLoginContrasena = loginActivity.findViewById(R.id.txtInputLoginContraseña);
        txtLoginNombreOCorreo.getEditText().setText("nombreUsuarioValido");
        txtLoginContrasena.getEditText().setText("contrasenaValida");

        // Haz clic en el botón de inicio de sesión
        Button btnLogin = loginActivity.findViewById(R.id.btnLogin);
        btnLogin.performClick();

        // Verifica que se inicia MainActivity
        ShadowActivity shadowActivity = Shadows.shadowOf(loginActivity);
        Intent expectedIntent = new Intent(loginActivity, MainActivity.class);
        Intent actualIntent = shadowActivity.getNextStartedActivity();
        assertTrue(expectedIntent.filterEquals(actualIntent));

        // Verifica que LoginActivity se finaliza
        assertTrue(loginActivity.isFinishing());
    }


    /**
     * Prueba para verificar que al hacer clic en el botón de inicio de sesión con el campo de nombre de usuario vacío y el campo de
     * contraseña lleno, se muestra un Toast.
     */
    @Test
    public void alHacerClicEnLogin_conNombreUsuarioVacio_deberiaMostrarToast() {
        TextInputLayout txtLoginNombreOCorreo = loginActivity.findViewById(R.id.txtInputLoginNombreOEmail);
        TextInputLayout txtLoginContrasena = loginActivity.findViewById(R.id.txtInputLoginContraseña);
        txtLoginNombreOCorreo.getEditText().setText("");
        txtLoginContrasena.getEditText().setText("contrasena");

        Button btnLogin = loginActivity.findViewById(R.id.btnLogin);
        btnLogin.performClick();

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals("Por favor, rellene todos los campos", toastText);
    }


    /**
     * Prueba para verificar que al hacer clic en el botón de inicio de sesión con el campo de contraseña vacío y el campo de
     * nombre de usuario lleno, se muestra un Toast.
     */
    @Test
    public void alHacerClicEnLogin_conContrasenaVacia_deberiaMostrarToast() {
        TextInputLayout txtLoginNombreOCorreo = loginActivity.findViewById(R.id.txtInputLoginNombreOEmail);
        TextInputLayout txtLoginContrasena = loginActivity.findViewById(R.id.txtInputLoginContraseña);
        txtLoginNombreOCorreo.getEditText().setText("nombreUsuario");
        txtLoginContrasena.getEditText().setText("");

        Button btnLogin = loginActivity.findViewById(R.id.btnLogin);
        btnLogin.performClick();

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals("Por favor, rellene todos los campos", toastText);
    }


    /**
     * Test para verificar que al hacer clic en el botón de inicio de sesión con credenciales incorrectas, se muestre un mensaje de error.
     */
    @Test
    public void alHacerClicEnLogin_conCredencialesInvalidas_deberiaMostrarError() {
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(Tasks.forException(new FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "The supplied auth credential is malformed or has expired.")));

        TextInputLayout txtLoginNombreOCorreo = loginActivity.findViewById(R.id.txtInputLoginNombreOEmail);
        TextInputLayout txtLoginContrasena = loginActivity.findViewById(R.id.txtInputLoginContraseña);
        txtLoginNombreOCorreo.getEditText().setText("nombreUsuarioInvalido");
        txtLoginContrasena.getEditText().setText("contrasenaInvalida");

        Button btnLogin = loginActivity.findViewById(R.id.btnLogin);
        btnLogin.performClick();

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals("Nombre de usuario/correo electrónico o contraseña incorrectos", toastText);
    }


    /**
     * Test para verificar que al hacer click en el botón de recuperar contraseña, si los campos están vacíos, se muestre un mensaje de rellenar los campos.
     */
    @Test
    public void alHacerClicEnOlvidarContrasena_conCampoVacio_deberiaMostrarToast() {
        TextInputLayout txtLoginNombreOCorreo = loginActivity.findViewById(R.id.txtInputLoginNombreOEmail);
        txtLoginNombreOCorreo.getEditText().setText("");

        Button button = loginActivity.findViewById(R.id.textViewBtnRecuperarContrasena);
        button.performClick();

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals("Por favor, ingrese su nombre de usuario o correo electrónico en el primer campo", toastText);
    }


    /**
     * Prueba para verificar que al hacer clic en el botón de olvidar contraseña con el campo de nombre de usuario lleno,
     * se muestra un Toast con el mensaje correcto (lo que significa que el correo electrónico para cambiar la contraseña se envió correctamente).
     */
    @Test
    public void alHacerClicEnOlvidarContrasena_conCampoLleno_deberiaMostrarToast() {
        TextInputLayout txtLoginNombreOCorreo = loginActivity.findViewById(R.id.txtInputLoginNombreOEmail);
        txtLoginNombreOCorreo.getEditText().setText("nombreUsuario");

        when(mockFirestore.collection("usuarios").whereEqualTo("nombreUsuario", "nombreUsuario").get())
                .thenReturn(Tasks.forResult(mock(QuerySnapshot.class)));

        Button button = loginActivity.findViewById(R.id.textViewBtnRecuperarContrasena);
        button.performClick();

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals("Correo para cambiar la contraseña enviado. Por favor, revise su bandeja de entrada.", toastText);
    }


}
