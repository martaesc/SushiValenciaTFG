package com.example.sushivalenciatfg.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


import androidx.lifecycle.Lifecycle;

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
     * Prueba que verifica que se cierra la actividad al hacer clic en el botón de salir.
     */
    @Test
    public void clickEnBotonSalir_deberiaCerrarLaActividad() {
        onView(withId(R.id.btnSalir)).perform(click());

        //objeto AtomicReference para almacenar el estado de la actividad
        AtomicReference<Lifecycle.State> state = new AtomicReference<>();

        // CountDownLatch para esperar a que se obtenga el estado de la actividad
        CountDownLatch latch = new CountDownLatch(1);

        // Obtener el estado de la actividad en un nuevo hilo
        new Thread(() -> {
            state.set(activityRule.getScenario().getState());
            latch.countDown(); // Decrementar el contador del latch
        }).start();

        // Esperamos a que se obtenga el estado de la actividad
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verificamos que la actividad ya no está en primer plano
        assertThat(state.get(), is(Lifecycle.State.DESTROYED));
    }


    @After
    public void tearDown() throws Exception {
        closeable.close();
    }
}