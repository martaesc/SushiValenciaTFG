package com.example.sushivalenciatfg.tests;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.LoginActivity;
import com.example.sushivalenciatfg.activities.RegistroActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import androidx.test.espresso.intent.Intents;

/**
 * Esta es la clase LoginActivityInstrumentedTest, que contiene pruebas instrumentadas para la actividad LoginActivity.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityInstrumentedTest {
    @Mock
    private FirebaseAuth mockAuth;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private Query mockQuery;
    private AutoCloseable closeable;

    /**
     * Método que se ejecuta antes de cada prueba. Inicializa los objetos simulados y configura su comportamiento.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        when(mockFirestore.collection(any(String.class))).thenReturn(mockCollectionReference);
        when(mockCollectionReference.whereEqualTo(any(String.class), any(String.class))).thenReturn(mockQuery);

        activityRule.getScenario().onActivity(activity -> {
            activity.setFirebaseAuth(mockAuth);
            activity.setFirestore(mockFirestore);
        });
    }

    /**
     * Prueba que verifica que cuando los campos de nombre de usuario y contraseña están vacíos y se hace clic en el botón de inicio de sesión, no se intenta iniciar sesión con Firebase.
     */
    @Test
    public void clickEnBotonLogin_conCamposVacios_deberiaMostrarToast() {
        // Simulamos campos vacíos
        activityRule.getScenario().onActivity(activity -> {
            activity.getTxtLoginNombreOCorreo().getEditText().setText("");
            activity.getTxtLoginContrasena().getEditText().setText("");
        });

        onView(withId(R.id.btnLogin)).perform(click());

        // Verificamos que se muestra el Toast correcto
        onView(withText("Por favor, rellene todos los campos")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));

        // Verificamos que no se intenta iniciar sesión con FirebaseAuth
        verify(mockAuth, never()).signInWithEmailAndPassword(anyString(), anyString());
    }


    /**
     * Prueba que verifica que cuando se hace clic en "Olvidé mi contraseña" con el campo de nombre de usuario vacío, no se intenta enviar un correo electrónico de restablecimiento de contraseña.
     */
    @Test
    public void clickEnEnlaceOlvidarContrasena_conCampoVacio_deberiaMostrarToast() {
        // Simulamos campo de nombre de usuario vacío
        activityRule.getScenario().onActivity(activity -> {
            activity.getTxtLoginNombreOCorreo().getEditText().setText("");
        });

        onView(withId(R.id.textViewBtnRecuperarContrasena)).perform(click());

        // Verificamos que se muestra el Toast correcto
        onView(withText("Por favor, ingrese su nombre de usuario o correo electrónico en el primer campo"))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));

        // y que no se intenta enviar un correo electrónico de restablecimiento de contraseña
        verify(mockAuth, never()).sendPasswordResetEmail(anyString());
    }


    /**
     * Prueba que verifica que cuando se hace clic en el textview de registro, se inicia una intención para ir a RegistroActivity.
     */
    @Test
    public void clickEnEnlaceRegistro_deberiaLlamarAIrARegistro() {
        Intents.init();

        onView(withId(R.id.textViewBtnRegistro)).perform(click());

        // Verificamos que se ha iniciado una intención para ir a RegistroActivity
        intended(hasComponent(RegistroActivity.class.getName()));

        Intents.release();
    }

    /**
     * Método que se llama después de que se ejecuten todas las pruebas, cerrando los recursos utilizados.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }
}