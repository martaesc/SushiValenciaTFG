package com.example.sushivalenciatfg.tests;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.activities.RegistroActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class RegistroActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<RegistroActivity> activityRule = new ActivityScenarioRule<>(RegistroActivity.class);

    @Mock
    private FirebaseAuth mockAuth;
    @Mock
    private FirebaseFirestore mockFirestore;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        activityRule.getScenario().onActivity(activity -> {
            activity.setFirebaseAuth(mockAuth);
            activity.setFirestore(mockFirestore);
        });
    }


    /**
     * Prueba que verifica que se muestra un Toast si se intenta registrar un usuario con campos vacíos.
     */
    @Test
    public void registroUsuario_conCamposVacios_deberiaMostrarToast() {
        onView(withId(R.id.btnRegistro)).perform(click());

        // Verificar que se muestra el Toast correcto
        onView(withText("Por favor, rellene todos los campos")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba que verifica que se muestra un Toast si se intenta registrar un usuario sin seleccionar un tipo de usuario.
     */
    @Test
    public void registroUsuario_sinSeleccionarTipoUsuario_deberiaMostrarToast() {
        onView(withId(R.id.txtRegisterUsername)).perform(replaceText("nomreUsuarioTest"), closeSoftKeyboard());
        onView(withId(R.id.txtRegisterEmail)).perform(replaceText("correoTest"), closeSoftKeyboard());
        onView(withId(R.id.txtLoginPassword)).perform(replaceText("contraTest"), closeSoftKeyboard());
        onView(withId(R.id.txtLoginPassword2)).perform(replaceText("contraTest"), closeSoftKeyboard());

        onView(withId(R.id.btnRegistro)).perform(click());

        // Verificar que se muestra el Toast correcto
        onView(withText("Debe seleccionar un tipo de usuario")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    /**
     * Prueba que verifica que se muestra un Toast si se intenta registrar un usuario con contraseñas no coincidentes.
     */
    @Test
    public void registroUsuario_conContrasenasNoCoincidentes_deberiaMostrarToast() {
        onView(withId(R.id.txtRegisterUsername)).perform(replaceText("nombreUsuarioTest"), closeSoftKeyboard());
        onView(withId(R.id.txtRegisterEmail)).perform(replaceText("correoTest"), closeSoftKeyboard());
        onView(withId(R.id.txtLoginPassword)).perform(replaceText("contraTest"), closeSoftKeyboard());
        onView(withId(R.id.txtLoginPassword2)).perform(replaceText("contraTestDiferente"), closeSoftKeyboard());

        onView(withId(R.id.usuarioRestaurante)).perform(click());

        onView(withId(R.id.btnRegistro)).perform(click());

        // Verificar que se muestra el Toast correcto
        onView(withText("Las contraseñas no coinciden")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }


    @After
    public void tearDown() throws Exception {
        closeable.close();
    }
}