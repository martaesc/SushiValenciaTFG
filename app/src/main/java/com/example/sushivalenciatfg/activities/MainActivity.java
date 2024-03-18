package com.example.sushivalenciatfg.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.adapters.RestauranteAdapter;
import com.example.sushivalenciatfg.models.Restaurante;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnPerfil;
    private ImageButton btnSalir;
    private SearchView searchView;
    private Button btnAñadirRestaurante;
    private RecyclerView recyclerView;
    private RestauranteAdapter adapter;
    private List<Restaurante> restaurantes;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializar instancia de Firebase
        db = FirebaseFirestore.getInstance();

        //obtener referencia a los elementos de la vista
        obtenerReferencias();

        //obtener restaurantes
        obtenerRestaurantes();

        //visibilidad botón añadir restaurante
        visibilidadBotonAñadirRestaurante();


        // Inicia la actividad NuevoRestaurante
        btnAñadirRestaurante.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NuevoRestauranteActivity.class);
            startActivity(intent);
        });

        // Para cerrar sesión
        btnSalir.setOnClickListener(v -> {
            finishAffinity();
        });
    }

    public void obtenerReferencias() {
        btnPerfil = findViewById(R.id.btnPerfil);
        btnSalir = findViewById(R.id.btnSalir);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.lista_restaurantes);
        btnAñadirRestaurante = findViewById(R.id.btnAñadirRestaurante);
    }

    public void obtenerRestaurantes() {
        db.collection("restaurantes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            restaurantes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Restaurante restaurante = document.toObject(Restaurante.class);
                                restaurantes.add(restaurante);
                            }
                            // Configura el RecyclerView
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            adapter = new RestauranteAdapter(restaurantes, MainActivity.this);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d("MainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void visibilidadBotonAñadirRestaurante() {
        SharedPreferences sharedPreferences = getSharedPreferences("usuario", MODE_PRIVATE);
        String tipoUsuario = sharedPreferences.getString("tipoUsuario", "");

        if (tipoUsuario.equals("RESTAURANTE")) {
            // Si el tipo de usuario es RESTAURANTE, mostrar el botón de añadir restaurante
            btnAñadirRestaurante.setVisibility(View.VISIBLE);
        } else {
            // Si no es RESTAURANTE, ocultar el botón de añadir restaurante
            btnAñadirRestaurante.setVisibility(View.GONE);
        }
    }


}