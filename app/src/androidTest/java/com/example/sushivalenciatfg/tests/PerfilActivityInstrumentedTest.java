package com.example.sushivalenciatfg.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.LoginActivity;
import com.example.sushivalenciatfg.activities.MainActivity;
import com.example.sushivalenciatfg.activities.NuevoRestauranteActivity;
import com.example.sushivalenciatfg.activities.PerfilActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class PerfilActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<PerfilActivity> activityRule = new ActivityScenarioRule<>(PerfilActivity.class);

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private FirebaseAuth mockAuth;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        activityRule.getScenario().onActivity(activity -> {
            activity.setFirebaseAuth(mockAuth);
            activity.setCurrentUser(mockUser);
        });
    }


    /**
     * Prueba para verificar que al hacer clic en el ImageView de la foto de perfil se abre un diálogo con dos opciones: Cámara y Galería.
     */
    @Test
    public void clickEnImageView_deberiaAbrirDialogo() {
        onView(withId(R.id.ivPhotoProfile)).perform(click());

        onView(withText("Elige una opción")).inRoot(isDialog()).check(matches(isDisplayed()));
    }


    /**
     * Prueba para verificar que al hacer clic en el ImageView de la foto de perfil y seleccionar la opción Cámara, se intenta abrir la cámara.
     */
    @Test
    public void clickEnImageView_ySeleccionarCamara_deberiaIntentarAbrirCamara() {
        Intents.init();

        // Hacer que todas las intenciones internas sean permitidas por defecto
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.ivPhotoProfile)).perform(click());

        onView(withText("Cámara")).perform(click());

        // Verificar que se intenta iniciar una actividad con la acción MediaStore.ACTION_IMAGE_CAPTURE (saltará el toast porque no se hace la foto)
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));

        Intents.release();
    }


    /**
     * Prueba para verificar que al hacer clic en el ImageView de la foto de perfil y seleccionar la opción Galería, se intenta abrir la galería.
     */
    @Test
    public void clickEnImageView_ySeleccionarGaleria_deberiaIntentarAbrirGaleria() {
        Intents.init();

        // Hacer que todas las intenciones internas sean permitidas por defecto (cualquier intención que no sea interna a la aplicación recibirá un resultado RESULT_OK por defecto)
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.ivPhotoProfile)).perform(click());

        onView(withText("Galería")).perform(click());

        // Verificar que se intenta iniciar una actividad con la acción MediaStore.ACTION_IMAGE_CAPTURE (saltará el toast porque no se selecciona ninguna foto)
        intended(hasAction(Intent.ACTION_PICK));

        Intents.release();
    }


    /**
     * Prueba para verificar que al hacer clic en el botón de guardar con campos vacíos se muestra un Toast.
     */
    @Test
    public void actualizarDatosUsuario_conCamposVacios_deberiaMostrarToast() {
        activityRule.getScenario().onActivity(activity -> {
            activity.getLyNombreUsuario().getEditText().setText("");
            activity.getLyEmail().getEditText().setText("");
            activity.getLyTipoUsuario().getEditText().setText("");
        });

        onView(withId(R.id.saveButton)).perform(click());

        onView(withText("No puede quedar ningún campo vacío")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba para la operación exitosa de enviar un correo para actualizar el correo electrónico del usuario.
     */
    @Test
    public void actualizarCorreoEnAuth_conOperacionExitosa_deberiaMostrarToast() {
        // Configurar comportamiento simulado
        when(mockUser.verifyBeforeUpdateEmail(anyString())).thenReturn(Tasks.forResult(null));

        activityRule.getScenario().onActivity(activity -> {
            activity.actualizarCorreoEnAuth("test@example.com");
        });

        onView(withText("Correo de verificación enviado. Por favor, revise su bandeja de entrada.")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba para verificar que al intentar actualizar la dirección de correo electrónico y la operación falla, se muestra un Toast.
     */
    @Test
    public void actualizarCorreoEnAuth_conOperacionFallida_deberiaMostrarToast() {
        // comportamiento simulado
        when(mockUser.verifyBeforeUpdateEmail(anyString())).thenReturn(Tasks.forException(new Exception("Test exception")));

        activityRule.getScenario().onActivity(activity -> {
            activity.actualizarCorreoEnAuth("test@example.com");
        });

        onView(withText("Error al enviar el correo de verificación")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba para verificar que al intentar actualizar la contraseña y la operación es exitosa, se muestra un Toast y se inicia LoginActivity.
     */
    @Test
    public void enviarCorreoCambioContrasena_conOperacionExitosa_deberiaMostrarToastYIniciarLoginActivity() {
        Intents.init();

        //comportamiento simulado
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockAuth.sendPasswordResetEmail(anyString())).thenReturn(Tasks.forResult(null));

        activityRule.getScenario().onActivity(activity -> {
            activity.enviarCorreoCambioContrasena();
        });

        onView(withText("Correo para cambiar la contraseña enviado. Por favor, revise su bandeja de entrada.")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));

        intended(hasComponent(LoginActivity.class.getName()));

        Intents.release();
    }


    /**
     * Prueba para verificar que al intentar actualizar la contraseña y la operación falla, se muestra un Toast.
     */
    @Test
    public void enviarCorreoCambioContrasena_conOperacionFallida_deberiaMostrarToast() {
        //comportamiento simulado
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockAuth.sendPasswordResetEmail(anyString())).thenReturn(Tasks.forException(new Exception("Test exception")));

        activityRule.getScenario().onActivity(activity -> {
            activity.enviarCorreoCambioContrasena();
        });

        onView(withText("Error al enviar el correo para cambiar la contraseña")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    /**
     * Prueba para verificar que al hacer clic en el botón de volver se inicia MainActivity.
     */
    @Test
    public void volver_deberiaIniciarMainActivity() {
        Intents.init();

        activityRule.getScenario().onActivity(activity -> {
            activity.volver();
        });

        intended(hasComponent(MainActivity.class.getName()));

        Intents.release();
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }


}