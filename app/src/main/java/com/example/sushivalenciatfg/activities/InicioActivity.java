package com.example.sushivalenciatfg.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sushivalenciatfg.R;
import com.google.android.material.button.MaterialButton;

/**
 * Esta es la clase InicioActivity, que extiende AppCompatActivity.
 * Representa la actividad inicial de la aplicación.
 */
public class InicioActivity extends AppCompatActivity {

    //botón que inicia la actividad
    private MaterialButton btnEmpezar;


    /**
     * Este método se llama cuando la actividad está iniciando.
     * Inicializa la actividad y establece el onClickListener para el botón btnEmpezar.
     *
     * @param savedInstanceState Si la actividad se está reinicializando después de haber sido cerrada previamente
     * entonces este Bundle contiene los datos que suministró más recientemente en onSaveInstanceState(Bundle).
     * Nota: De lo contrario, es nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        btnEmpezar = findViewById(R.id.btnEmpezar);

        btnEmpezar.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
