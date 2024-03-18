package com.example.sushivalenciatfg.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sushivalenciatfg.R;
import com.google.android.material.button.MaterialButton;

public class StartActivity  extends AppCompatActivity {

    private MaterialButton btnEmpezar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        btnEmpezar = findViewById(R.id.btnEmpezar);

        btnEmpezar.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
