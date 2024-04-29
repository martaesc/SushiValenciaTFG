package com.example.sushivalenciatfg.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.ComentariosActivity;
import com.example.sushivalenciatfg.activities.InfoRestauranteActivity;
import com.example.sushivalenciatfg.activities.MainActivity;
import com.example.sushivalenciatfg.activities.MasInfoActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Esta es la clase InfoRestauranteActivityInstrumentedTest, que contiene pruebas instrumentadas para la actividad InfoRestauranteActivity.
 */
@RunWith(AndroidJUnit4.class)
public class InfoRestauranteActivityInstrumentedTest {

    /**
     * Regla que proporciona una forma de lanzar una actividad antes de cada prueba. En este caso, se lanza la actividad InfoRestauranteActivity.
     */
    @Rule
    public ActivityScenarioRule<InfoRestauranteActivity> activityRule = new ActivityScenarioRule<>(InfoRestauranteActivity.class);


    /**
     * Prueba para comprobar que al no estar en modo edición y pulsar sobre el botón de editar, se habilitan los campos.
     */
    @Test
    public void edicion_conIsEditingFalse_deberiaHabilitarCampos() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.setIsEditing(false);
                onView(withId(R.id.imageButtonEditar)).perform(click());

                assertTrue(activity.getEtNombreRestaurante().isEnabled());
                assertTrue(activity.getEtDescripcionRestaurante().isEnabled());
                assertEquals(View.VISIBLE, activity.getEtLinkRestaurante().getVisibility());
                assertEquals(View.GONE, activity.getTvRestauranteLink().getVisibility());
                assertTrue(activity.getIsEditing());
            });
        }
    }

    /**
     * Prueba para comprobar que al estar en modo edición y pulsar sobre el botón de guardar, se deshabilitan los campos.
     */
    @Test
    public void edicion_conIsEditingTrue_deberiaDeshabilitarCampos() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.setIsEditing(true);
                onView(withId(R.id.imageButtonEditar)).perform(click());

                assertFalse(activity.getEtNombreRestaurante().isEnabled());
                assertFalse(activity.getEtDescripcionRestaurante().isEnabled());
                assertEquals(View.VISIBLE, activity.getEtLinkRestaurante().getVisibility());
                assertEquals(View.GONE, activity.getTvRestauranteLink().getVisibility());
                assertFalse(activity.getIsEditing());
            });
        }
    }

    /**
     * Prueba para comprobar que al estar en modo edición y pulsar sobre image view de la imagen, se muestra el diálogo.
     */
    @Test
    public void abrirDialogoSeleccionImagen_conModoEdicion_deberiaMostrarDialogo() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.habilitarEdicionEditText();
                activity.abrirDialogoSeleccionImagen();
            });

            // Verificar que se muestra el diálogo
            onView(withText("Elige una opción")).check(matches(isDisplayed()));
        }
    }


    /**
     * Prueba para comprobar que al no estar en modo edición y pulsar sobre image view de la imagen, no se muestra el diálogo.
     */
    @Test
    public void conModoNoEdicion_noDeberiaMostrarDialogoSeleccionImagen() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.deshabilitarEdicionEditText();
                activity.abrirDialogoSeleccionImagen();
            });

            onView(withText("Elige una opción")).check(doesNotExist());
        }
    }


    /**
     * Prueba para comprobar que al elegir la opción de galería en el diálogo, se inicia el intent de galería.
     */
    @Test
    public void abrirDialogoSeleccionImagen_conOpcionGaleria_deberiaIniciarIntentGaleria() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.abrirDialogoSeleccionImagen();
            });

            onView(withText("Galería")).perform(click());
            intended(hasAction(Intent.ACTION_PICK));
        }

        Intents.release();
    }


    /**
     * Prueba para comprobar que al elegir la opción de cámara en el diálogo, se inicia el intent de cámara.
     */
    @Test
    public void abrirDialogoSeleccionImagen_conOpcionCamara_deberiaIniciarIntentCamara() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.abrirDialogoSeleccionImagen();
            });

            onView(withText("Cámara")).perform(click());

            intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));
        }

        Intents.release();
    }


    /**
     * Prueba para comprobar que al pulsar sobre el botón de guardar con campos vacíos, se muestra un toast.
     */
    @Test
    public void comprobacionCamposYGuardar_conCamposVacios_deberiaMostrarToast() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.habilitarEdicionEditText();
                activity.getEtNombreRestaurante().setText("");
                activity.getEtDescripcionRestaurante().setText("");
                activity.getEtLinkRestaurante().setText("");
                activity.comprobacionDatosIngresados();
            });

            onView(withText("No puede quedar ningún campo vacío")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }
    }


    /**
     * Prueba para comprobar que al introducir una descripción con más de 20 líneas, se muestra un toast.
     */
    @Test
    public void comprobacionCamposYGuardar_conDescripcionMasDe20Lineas_deberiaMostrarToast() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.habilitarEdicionEditText();
                activity.getEtNombreRestaurante().setText("Nombre de prueba");
                activity.getEtDescripcionRestaurante().setText("Linea1\nLinea2\nLinea3\nLinea4\nLinea5\nLinea6\nLinea7\nLinea8\nLinea9\nLinea10\nLinea11\nLinea12\nLinea13\nLinea14\nLinea15\nLinea16\nLinea17\nLinea18\nLinea19\nLinea20\nLinea21");
                activity.getEtLinkRestaurante().setText("http://www.example.com");

                Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                activity.getIvImagenRestaurante().setImageBitmap(bitmap);
                activity.comprobacionDatosIngresados();
            });

            onView(withText("La descripción no puede tener más de 20 líneas")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }
    }


    /**
     * Prueba para comprobar que al introducir un enlade no válido, se muestra un toast
     */
    @Test
    public void comprobacionCamposYGuardar_conEnlaceNoValido_deberiaMostrarToast() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.habilitarEdicionEditText();
                activity.getEtNombreRestaurante().setText("Nombre de prueba");
                activity.getEtDescripcionRestaurante().setText("Descripción de prueba");
                activity.getEtLinkRestaurante().setText("Enlace no válido");

                Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                activity.getIvImagenRestaurante().setImageBitmap(bitmap);
                activity.comprobacionDatosIngresados();
            });

            onView(withText("El enlace no es válido")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }
    }


    /**
     * Prueba para comprobar que al pulsar sobre el text view sin enlace, se muestra un toast
     */
    @Test
    public void clickLinkRestaurante_conLinkNoValido_deberiaMostrarToast() {
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.getTvRestauranteLink().setText("El restaurante no tiene web");
                activity.clickLinkRestaurante();
            });

            onView(withText("Este restaurante no tiene página web")).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }

    }


    /**
     * Prueba para comprobar que al pulsar sobre el text view con enlace válido, se inicia un intent
     */
    @Test
    public void clickLinkRestaurante_conLinkValido_deberiaIniciarIntent() {
        Intents.init();
        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.getTvRestauranteLink().setText("http://www.example.com");
                activity.clickLinkRestaurante();
            });

            intended(hasAction(Intent.ACTION_VIEW));
        }

        Intents.release();
    }


    /**
     * Prueba para comprobar que al pulsar sobre el botón de volver, se inicia la actividad principal
     */
    @Test
    public void volverMenu_deberiaIniciarMainActivity() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                activity.volverMenu();
            });

            intended(hasComponent(MainActivity.class.getName()));
        }
        Intents.release();
    }


    /**
     * Prueba para comprobar que al pulsar sobre el botón de Mas Información, se inicia la actividad de MasInfoActivity
     */
    @Test
    public void irAMasInfo_deberiaIniciarMasInfoActivity() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                onView(withId(R.id.btnMasInfo)).perform(click());
            });
            intended(hasComponent(MasInfoActivity.class.getName()));
        }

        Intents.release();
    }


    /**
     * Prueba para comprobar que al pulsar sobre el botón de Comentarios, se inicia la actividad de ComentariosActivity
     */
    @Test
    public void irAComentarios_deberiaIniciarComentariosActivity() {
        Intents.init();

        if (activityRule.getScenario().getState() == Lifecycle.State.RESUMED) {
            activityRule.getScenario().onActivity(activity -> {
                onView(withId(R.id.btnComentarios)).perform(click());
            });
            intended(hasComponent(ComentariosActivity.class.getName()));
        }

        Intents.release();
    }


}