package com.example.sushivalenciatfg.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;


import androidx.lifecycle.Lifecycle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.MainActivity;
import com.example.sushivalenciatfg.activities.NuevoRestauranteActivity;
import com.example.sushivalenciatfg.activities.PerfilActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Esta es la clase MainActivityInstrumentedTest, que contiene pruebas instrumentadas para la actividad MainActivity.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);
    @Mock
    private FirebaseAuth mockAuth;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private FirebaseUser mockUser;
    private AutoCloseable closeable;


    /**
     * Método que se ejecuta antes de cada prueba. Se utiliza para inicializar los objetos simulados y configurar su comportamiento.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        activityRule.getScenario().onActivity(activity -> {
            activity.setFirebaseAuth(mockAuth);
            activity.setFirestore(mockFirestore);
            activity.setCurrentUser(mockUser);
        });
    }


    /**
     * Prueba que verifica que se inicia NuevoRestauranteActivity al hacer clic en el botón de añadir restaurante.
     */
    @Test
    public void clickEnBotonAñadirRestaurante_deberiaIniciarNuevoRestauranteActivity() {
        Intents.init();

        onView(withId(R.id.btnAñadirRestaurante)).perform(click());

        intended(hasComponent(NuevoRestauranteActivity.class.getName()));

        Intents.release();
    }


    /**
     * Prueba que verifica que se inicia PerfilActivity al hacer clic en el botón de perfil.
     */
    @Test
    public void clickEnBotonPerfil_deberiaIniciarPerfilActivity() {
        Intents.init();

        onView(withId(R.id.btnPerfil)).perform(click());

        intended(hasComponent(PerfilActivity.class.getName()));

        Intents.release();
    }

    /**
     * Prueba que salta un diálogo al hacer clic en el botón de salir.
     */
    @Test
    public void salir_deberiaMostrarDialogo() {
        // hacemos clic en el botón de salir
        onView(withId(R.id.btnSalir)).perform(click());

        // Verificamos que el título y el mensaje del diálogo se muestran correctamente
        onView(withText("Confirmar acción")).check(matches(isDisplayed()));
        onView(withText("¿Qué quieres hacer?")).check(matches(isDisplayed()));
    }

    /**
     * Prueba que verifica que se cierra la actividad al hacer clic en la opción "Salir de la aplicación" del diálogo.
     */
    @Test
    public void salir_deberiaCerrarLaActividad() {
        // hacemos clic en el botón de salir
        onView(withId(R.id.btnSalir)).perform(click());

        //  hacemos clic en la opción "Salir de la aplicación"
        onView(withText("Salir de la aplicación")).perform(click());

        // Verificamos que la actividad se ha cerrado
        activityRule.getScenario().onActivity(activity -> {
            assertTrue(activity.isFinishing());
        });
    }


    /**
     * Método que se ejecuta después de cada prueba. Se utiliza para limpiar los recursos utilizados durante la prueba.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }
}