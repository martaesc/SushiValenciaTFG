package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AppCompatActivity;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta es la clase MainActivity, que extiende AppCompatActivity.
 * Se encarga de gestionar la pantalla principal de la aplicación.
 */
public class MainActivity extends AppCompatActivity {

    // Referencias a los elementos de la vista
    private ImageButton btnPerfil;
    private ImageButton btnSalir;
    private SearchView searchView;
    private Button btnAñadirRestaurante;
    private RecyclerView recyclerView;

    // Adaptador para el RecyclerView
    private RestauranteAdapter adapter;

    // Lista de restaurantes
    private List<Restaurante> restaurantes;

    // Referencias a FirebaseAuth, FirebaseFirestore y FirebaseUser
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    //setters útiles para las pruebas (permiten inyectar instancias simuladas de FirebaseAuth, FirebaseFirestore y FirebaseUser)
    public void setFirebaseAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public void setFirestore(FirebaseFirestore db) {
        this.db = db;
    }

    public void setCurrentUser(FirebaseUser mockUser) {
        this.currentUser = mockUser;
    }


    /**
     * Este método se llama cuando la actividad está iniciando.
     * Inicializa la actividad y establece los onClickListener para los botones.
     *
     * @param savedInstanceState Si la actividad se está reinicializando después de haber sido cerrada previamente
     *                           entonces este Bundle contiene los datos que suministró más recientemente en onSaveInstanceState(Bundle).
     *                           Nota: De lo contrario, es nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        obtenerReferencias();
        obtenerRestaurantes();
        visibilidadBotonAñadirRestaurante();
        configurarBusquedaEnSearchView();

        btnAñadirRestaurante.setOnClickListener(v -> añadirRestaurante(v));
        btnPerfil.setOnClickListener(v -> irPerfil(v));
        btnSalir.setOnClickListener(v -> salir(v));
    }


    /**
     * Este método se encarga de obtener las referencias a los elementos de la vista.
     */
    public void obtenerReferencias() {
        btnPerfil = findViewById(R.id.btnPerfil);
        btnSalir = findViewById(R.id.btnSalir);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.lista_restaurantes);
        btnAñadirRestaurante = findViewById(R.id.btnAñadirRestaurante);
    }


    /**
     * Este método se encarga de configurar el SearchView para que realice la búsqueda de restaurantes.
     */
    public void configurarBusquedaEnSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //se llama cuando el usuario envía la consulta de búsqueda al presionar el botón de búsqueda en el teclado
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    buscarRestaurante(query);
                } else {
                    // Restablecer la lista de restaurantes a su estado original
                    adapter = new RestauranteAdapter(restaurantes, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                }
                return true;
            }

            //se llama cuando el texto de la consulta de búsqueda cambia (los resultados de la búsqueda se actualizan en tiempo real a medida que el usuario escribe)
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    buscarRestaurante(newText);
                } else {
                    adapter = new RestauranteAdapter(restaurantes, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                }
                return true;
            }
        });
    }


    /**
     * Este método se encarga de la lógica de buscar un restaurante por su nombre.
     *
     * @param nombre El nombre del restaurante a buscar.
     */
    public void buscarRestaurante(String nombre) {
        List<Restaurante> resultadosBusqueda = new ArrayList<>();
        for (Restaurante restaurante : restaurantes) {
            if (restaurante.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                resultadosBusqueda.add(restaurante);
            }
        }

        if (resultadosBusqueda.isEmpty()) {
            Toast.makeText(MainActivity.this, "No se encontraron restaurantes que coincidan con la búsqueda", Toast.LENGTH_SHORT).show();
        } else {
            // Actualizar el RecyclerView con los resultados de la búsqueda
            adapter = new RestauranteAdapter(resultadosBusqueda, MainActivity.this);
            recyclerView.setAdapter(adapter);
        }
    }


    /**
     * Este método se encarga de obtener todos los restaurantes de la base de datos Firestore.
     */
    public void obtenerRestaurantes() {
        db.collection("restaurantes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //si la tarea se completó con éxito: obtenemos los restaurantes, los convertimos de documento a restaurante y los añadimos a la lista
                        restaurantes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Restaurante restaurante = document.toObject(Restaurante.class);
                            restaurantes.add(restaurante);
                        }
                        // mostramos en el RecyclerView la lista de restaurantes utilizando el RestauranteAdapter para llenarlo
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        adapter = new RestauranteAdapter(restaurantes, MainActivity.this);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.d("MainActivity", "Error obteniendo restaurantes: ", task.getException());
                        Toast.makeText(MainActivity.this, "Error obteniendo restaurantes", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Este método se encarga de controlar la visibilidad  del botón de añadir restaurante en la interfaz en función del tipo de usuario que ha iniciado sesión.
     */
    public void visibilidadBotonAñadirRestaurante() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Buscamos en la colección "usuarios" un documento donde el campo "uid" coincide con el ID del usuario actual
            db.collection("usuarios")
                    .whereEqualTo("uid", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Obtenemos el tipo de usuario que es consultando el campo "tipoUsuario" del documento
                            String tipoUsuario = task.getResult().getDocuments().get(0).getString("tipoUsuario");
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


    /**
     * Este método se encarga de eliminar un restaurante, tanto de la base de datos como de la interfaz.
     *
     * @param restauranteId El ID del restaurante a eliminar.
     */
    public void eliminarRestaurante(String restauranteId) {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Buscamos el restaurante en la colección "restaurantes" con el ID del restaurante a eliminar
            db.collection("restaurantes")
                    .document(restauranteId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            //si el restaurante (documento) existe en la bbdd, comprobamos si el usuario actual es su creador
                            if (document.exists()) {
                                // comparando el ID del usuario actual con el campo "idUsuarioRestaurante" del documento
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


    /**
     * Este método se encarga de abrir la pantalla para añadir un nuevo restaurante.
     *
     * @param view La vista que fue clickeada.
     */
    public void añadirRestaurante(View view) {
        Intent intent = new Intent(this, NuevoRestauranteActivity.class);
        startActivity(intent);
    }


    /**
     * Este método se encarga de ir a la pantalla del perfil del usuario.
     *
     * @param view La vista que fue clickeada.
     */
    public void irPerfil(View view) {
        Intent intent = new Intent(this, PerfilActivity.class);
        startActivity(intent);
    }


    /**
     * Este método se encarga de salir de la aplicación.
     *
     * @param view La vista que fue clickeada.
     */
    public void salir(View view) {
        finishAffinity();
    }

}