package com.example.sushivalenciatfg.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.ComentariosActivity;
import com.example.sushivalenciatfg.activities.InfoRestauranteActivity;
import com.example.sushivalenciatfg.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Esta es la clase ComentariosActivityInstrumentedTest, que contiene pruebas instrumentadas para la actividad ComentariosActivity.
 */
@RunWith(AndroidJUnit4.class)
public class ComentariosActivityInstrumentedTest {
    @Rule
    public ActivityScenarioRule<ComentariosActivity> activityRule = new ActivityScenarioRule<>(ComentariosActivity.class);


    /**
     * Prueba que verifica que los elementos de la interfaz de usuario de la actividad ComentariosActivity se muestran correctamente.
     */
    @Test
    public void testElementosIU() {
        onView(withId(R.id.textView2)).check(matches(isDisplayed()));
        onView(withId(R.id.ratingBarPuntuacionUsuario)).check(matches(isDisplayed()));
        onView(withId(R.id.comentarioInputLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.btnPublicar)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerViewComentarios)).check(matches(isDisplayed()));
        onView(withId(R.id.btnVolverAInfoActivity)).check(matches(isDisplayed()));
        onView(withId(R.id.btnVolverAlMenuPrincipal)).check(matches(isDisplayed()));
    }


    /**
     * Prueba que verifica que los elementos de la interfaz de usuario de la actividad ComentariosActivity se muestran correctamente.
     */
    @Test
    public void testInteraccionBotones() {
        onView(withId(R.id.btnPublicar)).perform(click());
        onView(withId(R.id.btnVolverAInfoActivity)).perform(click());
        onView(withId(R.id.btnVolverAlMenuPrincipal)).perform(click());
    }


    /**
     * Prueba que verifica que se muestra un mensaje Toast cuando se intenta publicar una valoración con campos vacíos.
     */
    @Test
    public void publicarValoracion_deberiaMostrarToastSiCamposVacios() {
        activityRule.getScenario().onActivity(activity -> {
            onView(withId(R.id.btnPublicar)).perform(click());

            onView(withText("Para completar la valoración, debes escribir un comentario"))
                    .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.comentarioInputLayout)).perform(typeText("Test comment"));

            onView(withId(R.id.btnPublicar)).perform(click());

            onView(withText("Para completar la valoración, debes puntuar el restaurante"))
                    .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
                    .check(matches(isDisplayed()));
        });
    }


    /**
     * Prueba que verifica que al pulsar el botón para volver a la actividad InfoRestauranteActivity, se inicia dicha actividad.
     */
    @Test
    public void volverAInfoRestaurante_deberiaIniciarInfoRestauranteActivity() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                onView(withId(R.id.btnVolverAInfoActivity)).perform(click());
            });
            intended(hasComponent(InfoRestauranteActivity.class.getName()));
        }

        Intents.release();
    }


    /**
     * Prueba que verifica que al pulsar el botón para volver al menú principal, se inicia la actividad MainActivity.
     */
    @Test
    public void volverAlMenuPrincipal_deberiaIniciarMainActivity() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.volverAlMenuPrincipal();
            });
            intended(hasComponent(MainActivity.class.getName()));
        }

        Intents.release();
    }

}