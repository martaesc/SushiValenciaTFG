package com.example.sushivalenciatfg.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.adapters.RestauranteAdapter;
import com.example.sushivalenciatfg.models.Restaurante;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;

    //setters útiles para las pruebas, ya que permiten inyectar instancias simuladas de FirebaseAuth y FirebaseFirestore en LoginActivity.
    public void setFirebaseAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public void setFirestore(FirebaseFirestore db) {
        this.db = db;
    }

    public void setCurrentUser(FirebaseUser mockUser) {
        this.currentUser = mockUser;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializar instancia de Firebase
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        obtenerReferencias();

        obtenerRestaurantes();

        visibilidadBotonAñadirRestaurante();

        configurarBusqueda();


        btnAñadirRestaurante.setOnClickListener(v -> añadirRestaurante(v));

        btnPerfil.setOnClickListener(v -> irPerfil(v));

        btnSalir.setOnClickListener(v -> salir(v));


    }


    public void obtenerReferencias() {
        btnPerfil = findViewById(R.id.btnPerfil);
        btnSalir = findViewById(R.id.btnSalir);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.lista_restaurantes);
        btnAñadirRestaurante = findViewById(R.id.btnAñadirRestaurante);
    }

    // configurar el SearchView
    public void configurarBusqueda() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarRestaurante(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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
                            // Configurar el RecyclerView
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            adapter = new RestauranteAdapter(restaurantes, MainActivity.this);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d("MainActivity", "Error obteniendo restaurantes: ", task.getException());
                            Toast.makeText(MainActivity.this, "Error obteniendo restaurantes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void visibilidadBotonAñadirRestaurante() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si el usuario ha iniciado sesión, obtener su ID de usuario
            String userId = currentUser.getUid();
            // Buscar en la colección "usuarios" un documento donde el campo "uid" coincide con el ID del usuario
            db.collection("usuarios")
                    .whereEqualTo("uid", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Obtener el tipo de usuario del documento
                            String tipoUsuario = task.getResult().getDocuments().get(0).getString("tipoUsuario");
                            // Mostrar u ocultar el botón de añadir restaurante según el tipo de usuario
                            if ("Restaurante".equals(tipoUsuario)) {
                                btnAñadirRestaurante.setVisibility(View.VISIBLE);
                            } else {
                                btnAñadirRestaurante.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("MainActivity", "No se encontró un documento de usuario con el uid: " + userId);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error obteniendo el tipo de usuario", e);
                    });
        } else {
            Log.d("MainActivity", "El usuario actual es nulo");
        }
    }

    public void buscarRestaurante(String nombre) {
        List<Restaurante> resultadosBusqueda = new ArrayList<>();
        for (Restaurante restaurante : restaurantes) {
            if (restaurante.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                resultadosBusqueda.add(restaurante);
            }
        }
        // Actualizar el RecyclerView con los resultados de la búsqueda
        adapter = new RestauranteAdapter(resultadosBusqueda, MainActivity.this);
        recyclerView.setAdapter(adapter);
    }

    public void eliminarRestaurante(String restauranteId) {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("restaurantes")
                    .document(restauranteId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String creadorId = document.getString("idUsuarioRestaurante");
                                if (userId.equals(creadorId)) {
                                    db.collection("restaurantes").document(restauranteId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("MainActivity", "Restaurante eliminado con éxito");
                                                Toast.makeText(MainActivity.this, "Restaurante eliminado con éxito", Toast.LENGTH_SHORT).show();
                                                // Refrescar MainActivity
                                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("MainActivity", "Error al eliminar restaurante", e);
                                                Toast.makeText(MainActivity.this, "Error al eliminar restaurante", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Log.d("MainActivity", "El usuario no tiene permiso para eliminar este restaurante");
                                    Toast.makeText(MainActivity.this, "No tienes permiso para eliminar este restaurante", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("MainActivity", "No se encontró el restaurante");
                            }
                        } else {
                            Log.e("MainActivity", "Error al obtener restaurante", task.getException());
                        }
                    });
        } else {
            Log.e("MainActivity", "El usuario actual es nulo");
        }
    }

    //añadir restaurante
    public void añadirRestaurante(View view) {
        Intent intent = new Intent(this, NuevoRestauranteActivity.class);
        startActivity(intent);
    }

    //para ir al perfil
    public void irPerfil(View view) {
        Intent intent = new Intent(this, PerfilActivity.class);
        startActivity(intent);
    }

    //salir
    public void salir(View view) {
        finishAffinity();
    }



}