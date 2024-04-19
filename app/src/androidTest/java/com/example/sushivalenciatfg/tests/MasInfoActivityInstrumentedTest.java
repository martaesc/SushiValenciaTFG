package com.example.sushivalenciatfg.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;

import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import static org.junit.Assert.*;

import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.InfoRestauranteActivity;
import com.example.sushivalenciatfg.activities.MainActivity;
import com.example.sushivalenciatfg.activities.MasInfoActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MasInfoActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MasInfoActivity> activityRule = new ActivityScenarioRule<>(MasInfoActivity.class);


    /**
     * Prueba para comprobar que al pulsar el botón de editar, se habilitan los campos de edición y se cambia el texto del botón a "Guardar"
     */
    @Test
    public void edicion_conIsEditingFalse_deberiaHabilitarCampos() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.setIsEditing(false);
                onView(withId(R.id.btnEditarInfo)).perform(click());

                assertTrue(activity.getEtTelefono().isEnabled());
                assertTrue(activity.getEtHorario().isEnabled());
                assertTrue(activity.getEtDireccion().isEnabled());
                assertEquals(View.VISIBLE, activity.getEtTelefono().getVisibility());
                assertEquals(View.VISIBLE, activity.getEtHorario().getVisibility());
                assertEquals(View.VISIBLE, activity.getEtDireccion().getVisibility());
                assertEquals(View.GONE, activity.getTvTelefono().getVisibility());
                assertEquals(View.GONE, activity.getTvHorario().getVisibility());
                assertEquals(View.GONE, activity.getTvDireccion().getVisibility());
                assertEquals("Guardar", activity.getBtnEditar().getText().toString());
                assertTrue(activity.getIsEditing());
            });
        }
    }


    /**
     * Prueba para comprobar que al pulsar el botón de guardar, se deshabilitan los campos de edición y se cambia el texto del botón a "Editar"
     */
    @Test
    public void edicion_conIsEditingTrue_deberiaDeshabilitarCampos() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.setIsEditing(true);
                onView(withId(R.id.btnEditarInfo)).perform(click());

                assertFalse(activity.getEtTelefono().isEnabled());
                assertFalse(activity.getEtHorario().isEnabled());
                assertFalse(activity.getEtDireccion().isEnabled());
                assertEquals(View.GONE, activity.getEtTelefono().getVisibility());
                assertEquals(View.GONE, activity.getEtHorario().getVisibility());
                assertEquals(View.GONE, activity.getEtDireccion().getVisibility());
                assertEquals(View.VISIBLE, activity.getTvTelefono().getVisibility());
                assertEquals(View.VISIBLE, activity.getTvHorario().getVisibility());
                assertEquals(View.VISIBLE, activity.getTvDireccion().getVisibility());
                assertEquals("Editar", activity.getBtnEditar().getText().toString());
                assertFalse(activity.getIsEditing());
            });
        }
    }


    /**
     * Prueba para comprobar que al intentar guardar los cambios, los campos estén llenos.
     */
    @Test
    public void comprobacionCamposYGuardar_conCamposVacios_deberiaMostrarToast() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.habilitarEdicionEditText();
                activity.getEtTelefono().setText("");
                activity.getEtHorario().setText("");
                activity.getEtDireccion().setText("");
                activity.comprobacionCamposYGuardar();
            });

            onView(withText("No puede quedar ningún campo vacío")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }
    }


    /**
     * Prueba para comprobar que al intentar guardar los cambios, el teléfono sea válido.
     */
    @Test
    public void comprobacionCamposYGuardar_conTelefonoNoValido_deberiaMostrarToast() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.habilitarEdicionEditText();
                activity.getEtTelefono().setText("123456");
                activity.getEtHorario().setText("Horario de prueba");
                activity.getEtDireccion().setText("Dirección de prueba");
                activity.comprobacionCamposYGuardar();
            });

            onView(withText("Por favor, introduzca un número de teléfono válido")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }
    }


    /**
     * Prueba para comprobar que al pulsar encima del número de teléfono, si el dispositivo no tiene una aplicación de marcado de teléfono, salte un toast.
     */
    @Test
    public void llamarTelefono_DispositivosinAplicacionDial_deberiaMostrarToast() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.getEtTelefono().setText("123456789");
                activity.llamarTelefono();
            });

            onView(withText("No se encontró una aplicación de marcado de teléfono")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }
    }


    /**
     * Prueba para comprobar que al pulsar encima de la dirección, si el dispositivo no tiene una aplicación de mapas, salte un toast.
     */
    @Test
    public void abrirMapa_DispositivosinAplicacionMaps_deberiaMostrarToast() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.getTvDireccion().setText("Valencia, España");
                activity.abrirMapa();
            });

            onView(withText("No se encontró una aplicación de mapas")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }

        Intents.release();
    }


/**
     * Prueba para comprobar que al pulsar encima del botón de volver, se inicie la actividad InfoRestauranteActivity.
     */
    @Test
    public void volver_deberiaIniciarInfoRestauranteActivity() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                onView(withId(R.id.btnVolverInfoActivity)).perform(click());
            });
            intended(hasComponent(InfoRestauranteActivity.class.getName()));
        }

        Intents.release();
    }


    /**
     * Prueba para comprobar que al pulsar encima del botón de volver al menú principal, se inicie la actividad MainActivity.
     */
    @Test
    public void volverMenuPrincipal_deberiaIniciarMainActivity() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                onView(withId(R.id.btnVolverMenuPrincipal)).perform(click());
            });
            intended(hasComponent(MainActivity.class.getName()));
        }

        Intents.release();
    }

}