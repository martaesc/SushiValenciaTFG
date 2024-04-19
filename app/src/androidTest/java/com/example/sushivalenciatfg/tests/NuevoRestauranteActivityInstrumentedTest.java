package com.example.sushivalenciatfg.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static org.hamcrest.Matchers.not;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;

import android.provider.MediaStore;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.NuevoRestauranteActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NuevoRestauranteActivityInstrumentedTest {


    @Rule
    public ActivityScenarioRule<NuevoRestauranteActivity> activityRule = new ActivityScenarioRule<>(NuevoRestauranteActivity.class);


    @Test
    public void clickEnImageView_deberiaAbrirDialogo() {
        // Hacer clic en el ImageView
        onView(withId(R.id.iv_imagenNuevoRestaurante)).perform(click());

        // Verificar que se muestra el diálogo
        onView(withText("Elige una opción")).inRoot(isDialog()).check(matches(isDisplayed()));
    }


    /**
     * Prueba que se intenta abrir la cámara cuando se selecciona la opción de la cámara en el diálogo
     */
    @Test
    public void clickEnImageView_ySeleccionarCamara_deberiaIntentarAbrirCamara() {
        Intents.init();

        // Hacer que todas las intenciones internas sean permitidas por defecto
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.iv_imagenNuevoRestaurante)).perform(click());

        // Hacer clic en la opción "Cámara" en el diálogo
        onView(withText("Cámara")).perform(click());

        // Verificar que se intenta iniciar una actividad con la acción MediaStore.ACTION_IMAGE_CAPTURE (saltará el toast porque no se hace la foto)
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));

        Intents.release();
    }

    /**
     * Prueba que se intenta abrir la galería cuando se selecciona la opción de la galería en el diálogo
     */
    @Test
    public void clickEnImageView_ySeleccionarGaleria_deberiaIntentarAbrirGaleria() {
        Intents.init();

        // Hacer que todas las intenciones internas sean permitidas por defecto (cualquier intención que no sea interna a la aplicación recibirá un resultado RESULT_OK por defecto)
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.iv_imagenNuevoRestaurante)).perform(click());

        // Hacer clic en la opción "Galería" en el diálogo
        onView(withText("Galería")).perform(click());

        // Verificar que se intenta iniciar una actividad con la acción MediaStore.ACTION_IMAGE_CAPTURE (saltará el toast porque no se selecciona ninguna foto)
        intended(hasAction(Intent.ACTION_PICK));

        Intents.release();
    }

    @Test
    public void comprobacionCamposYGuardar_conCamposTextoVacios_deberiaMostrarToast() {
        // Simular campos vacíos
        activityRule.getScenario().onActivity(activity -> {
            activity.getEtNombreRestaurante().setText("");
            activity.getEtDescripcionRestaurante().setText("");
            activity.getEtHorarioRestaurante().setText("");
            activity.getEtTelefonoRestaurante().setText("");
            activity.getEtDireccionRestaurante().setText("");
        });

        // Hacer clic en el botón de guardar restaurante
        onView(withId(R.id.btnGuardarNuevoRestaurante)).perform(click());

        // Verificar que se muestra el Toast correcto
        onView(withText("Por favor, complete todos los campos")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba que verifica que cuando se hace clic en el botón de guardar restaurante con los campos llenos y sin imagen, se muestra un Toast indicando que se debe seleccionar una imagen.
     */
    @Test
    public void comprobacionCamposYGuardar_conCamposLlenosSinImagen_deberiaMostrarToast() {
        activityRule.getScenario().onActivity(activity -> {
            activity.getEtNombreRestaurante().setText("Nombre de prueba");
            activity.getEtDescripcionRestaurante().setText("Descripción de prueba");
            activity.getEtHorarioRestaurante().setText("Horario de prueba");
            activity.getEtTelefonoRestaurante().setText("666666666");
            activity.getEtDireccionRestaurante().setText("Dirección de prueba");
            activity.getIvImagenRestaurante().setImageDrawable(null);
        });

        onView(withId(R.id.btnGuardarNuevoRestaurante)).perform(click());

        onView(withText("Por favor, seleccione una imagen")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba que verifica que cuando se hace clic en el botón de guardar restaurante con una descripción de más de 20 líneas, se muestra un Toast.
     */
    @Test
    public void comprobacionCamposYGuardar_conDescripcionMasDe20Lineas_deberiaMostrarToast() {
        activityRule.getScenario().onActivity(activity -> {
            activity.getEtNombreRestaurante().setText("Nombre de prueba");
            activity.getEtDescripcionRestaurante().setText("Línea 1\nLínea 2\nLínea 3\nLínea 4\nLínea 5\nLínea 6\nLínea 7\nLínea 8\nLínea 9\nLínea 10\nLínea 11\nLínea 12\nLínea 13\nLínea 14\nLínea 15\nLínea 16\nLínea 17\nLínea 18\nLínea 19\nLínea 20\nLínea 21");
            activity.getEtHorarioRestaurante().setText("Horario de prueba");
            activity.getEtTelefonoRestaurante().setText("666666666");
            activity.getEtDireccionRestaurante().setText("Dirección de prueba");

            // Crear un Bitmap vacío
            Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            // Establecer el Bitmap en el ImageView
            activity.getIvImagenRestaurante().setImageBitmap(bitmap);

        });

        onView(withId(R.id.btnGuardarNuevoRestaurante)).perform(click());

        onView(withText("La descripción no puede tener más de 20 líneas")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba que verifica que cuando se hace clic en el botón de guardar restaurante con un número de teléfono no válido, se muestra un Toast.
     */
    @Test
    public void comprobacionCamposYGuardar_conTelefonoNoValido_deberiaMostrarToast() {
        activityRule.getScenario().onActivity(activity -> {
            activity.getEtNombreRestaurante().setText("Nombre de prueba");
            activity.getEtDescripcionRestaurante().setText("Descripción de prueba");
            activity.getEtHorarioRestaurante().setText("Horario de prueba");
            activity.getEtTelefonoRestaurante().setText("123456");
            activity.getEtDireccionRestaurante().setText("Dirección de prueba");

            Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            activity.getIvImagenRestaurante().setImageBitmap(bitmap);
        });

        onView(withId(R.id.btnGuardarNuevoRestaurante)).perform(click());

        onView(withText("Por favor, introduzca un número de teléfono válido")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void comprobacionCamposYGuardar_conEnlaceNoValido_deberiaMostrarToast() {
        // Simular un enlace no válido
        activityRule.getScenario().onActivity(activity -> {
            activity.getEtNombreRestaurante().setText("Nombre de prueba");
            activity.getEtDescripcionRestaurante().setText("Descripción de prueba");
            activity.getEtHorarioRestaurante().setText("Horario de prueba");
            activity.getEtTelefonoRestaurante().setText("666666666");
            activity.getEtDireccionRestaurante().setText("Dirección de prueba");
            activity.getEtLinkRestaurante().setText("Enlace no válido");

            // Crear un Bitmap vacío
            Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            // Establecer el Bitmap en el ImageView
            activity.getIvImagenRestaurante().setImageBitmap(bitmap);
        });

        // Hacer clic en el botón de guardar restaurante
        onView(withId(R.id.btnGuardarNuevoRestaurante)).perform(click());

        // Verificar que se muestra el Toast correcto
        onView(withText("Por favor, introduzca un enlace válido")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

}