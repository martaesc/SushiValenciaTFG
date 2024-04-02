package com.example.sushivalenciatfg.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.adapters.ComentarioAdapter;
import com.example.sushivalenciatfg.adapters.RestauranteAdapter;
import com.example.sushivalenciatfg.models.Comentario;
import com.example.sushivalenciatfg.models.Restaurante;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComentariosActivity extends AppCompatActivity {

    private RatingBar punuacion;
    private TextInputLayout comentario;
    private Button btnPublicar;
    private RecyclerView rvComentarios;
    private Button btnVolver;
    private Button btnVolverMenu;

    private ComentarioAdapter comentarioAdapter;
    private List<Comentario> listaComentarios;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String restauranteId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        obtenerReferencias();

        //recoger el id del restaurante que se ha pasado desde InfoRestauranteActivity
        restauranteId = getIntent().getStringExtra("idRestaurante");
        if (restauranteId != null) {
            cargarValoraciones();
        } else {
            Log.e("Comentarios Activity", "No se ha recibido el id del restaurante");
            finish();
        }


        btnPublicar.setOnClickListener(v -> publicarValoracion());
        btnVolver.setOnClickListener(v -> volverAInfoRestaurante());
        btnVolverMenu.setOnClickListener(v -> volverAlMenuPrincipal());
    }

    private void obtenerReferencias() {
        punuacion = findViewById(R.id.ratingBarPuntuacionUsuario);
        comentario = findViewById(R.id.comentarioInputLayout);
        btnPublicar = findViewById(R.id.btnPublicar);
        rvComentarios = findViewById(R.id.recyclerViewComentarios);
        btnVolver = findViewById(R.id.btnVolverAInfoActivity);
        btnVolverMenu = findViewById(R.id.btnVolverAlMenuPrincipal);
    }

    private void publicarValoracion() {

        String textoComentario = comentario.getEditText().getText().toString();
        float calificacionFloat = punuacion.getRating();
        int calificacion = (int) calificacionFloat;

        // para poder enviar una puntuación o publicar un comentario, deben estar rellenados ambos campos
        if (textoComentario.isEmpty()) {
            Toast.makeText(this, "Para completar la valoración, debes escribir un comentario", Toast.LENGTH_LONG).show();
            return;
        }

        if (calificacion == 0) {
            Toast.makeText(this, "Para completar la valoración, debes puntuar el restaurante", Toast.LENGTH_SHORT).show();
            return;
        }

        // ID del usuario actual
        String idUsuario = currentUser.getUid();

        //fecha actual (Date en vez de LocalDate porque esta ultima está disponible a partir de la API 26, y quiero que mi aplicación sea compatible en dispositivos con API 24 o superior)
        Date fechaPublicacion = new Date();

        // obtenemos la URL de la imagen del usuario actual desde Firestore
        db.collection("usuario")
                .whereEqualTo("uid", idUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String imagenUsuario = documentSnapshot.getString("urlImagen");
                        String nombreUsuario = documentSnapshot.getString("nombreUsuario");

                        Comentario nuevoComentario = new Comentario(nombreUsuario, calificacion, fechaPublicacion, textoComentario, imagenUsuario, restauranteId, idUsuario);
                        listaComentarios.add(nuevoComentario);

                        comentarioAdapter.notifyDataSetChanged();

                        // Guardamos el nuevo comentario en Firestore
                        db.collection("comentarios")
                                .add(nuevoComentario)
                                .addOnSuccessListener(documentReference -> {
                                    // El comentario se ha guardado correctamente
                                    Toast.makeText(this, "¡Comentario publicado con éxito!", Toast.LENGTH_SHORT).show();

                                    // Guardo el ID del documento creado en el campo idComentario del objeto Comentario
                                    String docId = documentReference.getId();
                                    db.collection("comentarios").document(docId)
                                            .update("idComentario", docId);

                                    limpiarCampos();
                                })
                                .addOnFailureListener(e -> {
                                    // Hubo un error al guardar el comentario
                                    Log.e("ComentariosActivity", e.getMessage());
                                    Toast.makeText(this, "Error al publicar el comentario", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejar el error
                    Log.e("ComentariosActivity", "Error obteniendo datos del usuario", e);
                    Toast.makeText(this, "Error obteniendo datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    public void cargarValoraciones() {
        db.collection("comentarios")
                .whereEqualTo("idRestaurante", restauranteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaComentarios = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Comentario comentario = document.toObject(Comentario.class);
                            listaComentarios.add(comentario);
                        }
                        // Configurar el RecyclerView
                        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
                        comentarioAdapter = new ComentarioAdapter(listaComentarios, this);
                        rvComentarios.setAdapter(comentarioAdapter);
                    } else {
                        Log.d("MainActivity", "Error obteniendo valoraciones: ", task.getException());
                        Toast.makeText(this, "Error obteniendo valoraciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void eliminarComentario(String comentarioId) {
        db.collection("comentarios").document(comentarioId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Comentario comentario = documentSnapshot.toObject(Comentario.class);
                    if (((currentUser != null) && ((currentUser.getUid().equals(comentario.getIdUsuario()))) || (currentUser.getUid().equals(comentario.getIdRestaurante())))) {
                        // El usuario actual es el autor del comentario o el propietario del restaurante
                        db.collection("comentarios").document(comentarioId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Comentario eliminado con éxito", Toast.LENGTH_SHORT).show();
                                    cargarValoraciones();  // Recargar los comentarios
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ComentariosActivity", "Error eliminando comentario", e);
                                    Toast.makeText(this, "Error eliminando comentario", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No tienes permiso para eliminar este comentario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error obteniendo comentario", e);
                    Toast.makeText(this, "Error obteniendo comentario", Toast.LENGTH_SHORT).show();
                });
    }

    public void limpiarCampos() {
        comentario.getEditText().setText("");
        punuacion.setRating(0);
    }

    public void volverAInfoRestaurante() {
        Intent intent = new Intent(this, InfoRestauranteActivity.class);
        startActivity(intent);
    }

    public void volverAlMenuPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}