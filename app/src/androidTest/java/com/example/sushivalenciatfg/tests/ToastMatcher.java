package com.example.sushivalenciatfg.tests;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Esta es la clase ToastMatcher, que extiende TypeSafeMatcher<Root>.
 * Se utiliza para verificar si un objeto Root es un Toast en Espresso y, en combinación con onView(withText("...")).inRoot(new ToastMatcher()), para verificar si se muestra un mensaje Toast específico en las pruebas.
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {

    /**
     * Este método se utiliza para describir a qué se está haciendo referencia en el matcher.
     *
     * @param description La descripción a la que se añadirá el texto.
     */
    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    /**
     * Este método se utiliza para verificar si un objeto Root dado es un Toast.
     *
     * @param root El objeto Root a verificar.
     * @return Verdadero si el objeto Root es un Toast, falso en caso contrario.
     */
    @Override
    protected boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            return windowToken == appToken;
        }
        return false;
    }
}
