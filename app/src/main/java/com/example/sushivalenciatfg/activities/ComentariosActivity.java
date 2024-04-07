package com.example.sushivalenciatfg.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sushivalenciatfg.R;
import com.example.sushivalenciatfg.adapters.ComentarioAdapter;
import com.example.sushivalenciatfg.models.Comentario;
import com.example.sushivalenciatfg.models.Respuesta;
import com.example.sushivalenciatfg.models.Restaurante;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComentariosActivity extends AppCompatActivity {


    private TextView subtitulo;
    private RatingBar punuacion;
    private TextInputLayout textoComentario;
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
    private double puntuacionPromedio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();


        //recoger el id del restaurante que se ha pasado desde InfoRestauranteActivity
        restauranteId = getIntent().getStringExtra("idRestaurante");
        if (restauranteId != null) {
            cargarValoraciones();
        } else {
            Log.e("Comentarios Activity", "No se ha recibido el id del restaurante");
            finish();
        }

        obtenerReferencias();
        visibilidadComponentesParaRestaurante();


        btnPublicar.setOnClickListener(v -> publicarValoracion());
        btnVolver.setOnClickListener(v -> volverAInfoRestaurante());
        btnVolverMenu.setOnClickListener(v -> volverAlMenuPrincipal());
    }

    private void obtenerReferencias() {
        subtitulo = findViewById(R.id.textView2);
        punuacion = findViewById(R.id.ratingBarPuntuacionUsuario);
        textoComentario = findViewById(R.id.comentarioInputLayout);
        btnPublicar = findViewById(R.id.btnPublicar);
        rvComentarios = findViewById(R.id.recyclerViewComentarios);
        btnVolver = findViewById(R.id.btnVolverAInfoActivity);
        btnVolverMenu = findViewById(R.id.btnVolverAlMenuPrincipal);
    }

    public void visibilidadComponentesParaRestaurante() {
        // // Ocultar por defecto los componentes (para evitar el problema de que tarden unos segundos en desaparecer al entrar a la activity)
        subtitulo.setVisibility(View.GONE);
        punuacion.setVisibility(View.GONE);
        textoComentario.setVisibility(View.GONE);
        btnPublicar.setVisibility(View.GONE);

        if (currentUser != null) {
            // ID de usuario actual
            String userId = currentUser.getUid();

            // Obtener el usuario actual
            obtenerUsuario(userId, documentSnapshot -> {
                // Obtener el tipo de usuario del documento
                String tipoUsuario = documentSnapshot.getString("tipoUsuario");

                // Si el tipo de usuario es "Restaurante", entonces comprobar si es el creador del restaurante
                if ("Restaurante".equals(tipoUsuario)) {
                    // Buscar en la colección "restaurantes" un documento donde el campo "idUsuarioRestaurante" coincide con el ID del usuario
                    db.collection("restaurantes")
                            .whereEqualTo("idUsuarioRestaurante", userId)
                            .get()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                    // Si el usuario es el creador del restaurante, ocultar los componentes para publicar una valoración
                                    subtitulo.setVisibility(View.GONE);
                                    punuacion.setVisibility(View.GONE);
                                    textoComentario.setVisibility(View.GONE);
                                    btnPublicar.setVisibility(View.GONE);
                                } else {
                                    // Si el usuario es un restaurante pero no el creador del restaurante, mostrar los componentes para publicar una valoración
                                    subtitulo.setVisibility(View.VISIBLE);
                                    punuacion.setVisibility(View.VISIBLE);
                                    textoComentario.setVisibility(View.VISIBLE);
                                    btnPublicar.setVisibility(View.VISIBLE);
                                }
                            });
                } else {
                    // Si el tipo de usuario no es "Restaurante", mostrar los componentes para publicar una valoración
                    subtitulo.setVisibility(View.VISIBLE);
                    punuacion.setVisibility(View.VISIBLE);
                    textoComentario.setVisibility(View.VISIBLE);
                    btnPublicar.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Log.d("ComentariosActivity", "El usuario actual es nulo");
        }
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

    public void obtenerUsuario(String usuarioId, OnSuccessListener<DocumentSnapshot> onSuccessListener) {
        db.collection("usuarios")
                .whereEqualTo("uid", usuarioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        onSuccessListener.onSuccess(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error obteniendo datos del usuario", e);
                    Toast.makeText(this, "Error obteniendo datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    public void obtenerRestaurante(String restauranteId, OnSuccessListener<Restaurante> onSuccessListener) {
        db.collection("restaurantes").document(restauranteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Restaurante restaurante = documentSnapshot.toObject(Restaurante.class);
                    onSuccessListener.onSuccess(restaurante);
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error obteniendo datos del restaurante", e);
                    Toast.makeText(this, "Error obteniendo datos del restaurante", Toast.LENGTH_SHORT).show();
                });
    }

    private void publicarValoracion() {
        String textoComentario = this.textoComentario.getEditText().getText().toString();
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

        // Obtenemos el nombre y la imagen del usuario
        obtenerUsuario(idUsuario, documentSnapshot -> {
            String fotoPerfil = documentSnapshot.getString("fotoPerfil");
            if (fotoPerfil == null) {
                Log.e("ComentariosActivity", "Error obteniendo foto de perfil del usuario");
            }


            String nombreUsuario = documentSnapshot.getString("nombreUsuario");

            Comentario nuevoComentario = new Comentario(nombreUsuario, calificacion, fechaPublicacion, textoComentario, fotoPerfil, restauranteId, idUsuario);
            listaComentarios.add(nuevoComentario);

            comentarioAdapter.notifyDataSetChanged();

            // Guardamos el nuevo comentario en Firestore
            guardarComentarioEnFirestore(nuevoComentario, documentReference -> {
                Toast.makeText(this, "¡Comentario publicado con éxito!", Toast.LENGTH_SHORT).show();

                // Actualizo el objeto nuevoComentario con el id ya asignado en el campo idComentario, así al añadirlo después al array en firestore, no saldrá null
                nuevoComentario.setIdComentario(documentReference.getId());

                // Añadimos el objeto del comentario a la lista de comentarios del restaurante
                db.collection("restaurantes").document(restauranteId)
                        .update("listaComentarios", FieldValue.arrayUnion(nuevoComentario.getIdComentario()));

                limpiarCampos();

                // Actualizar la puntuación del restaurante para enviarselo a InfoRestauranteActivity
                actualizarPuntuacionPromedio();
            });
        });
    }


    public void guardarComentarioEnFirestore(Comentario nuevoComentario, OnSuccessListener<DocumentSnapshot> onSuccessListener) {
        db.collection("comentarios")
                .add(nuevoComentario)
                .addOnSuccessListener(documentReference -> {
                    // Guardo el ID del documento creado en el campo idComentario del objeto Comentario
                    String docId = documentReference.getId();
                    db.collection("comentarios").document(docId)
                            .update("idComentario", docId)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    documentReference.get()
                                            .addOnSuccessListener(onSuccessListener)
                                            .addOnFailureListener(e -> {
                                                Log.e("ComentariosActivity", "Error obteniendo comentario", e);
                                            });
                                } else {
                                    Log.e("ComentariosActivity", "Error actualizando idComentario", task.getException());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", e.getMessage());
                    Toast.makeText(this, "Error al publicar el comentario", Toast.LENGTH_SHORT).show();
                });
    }

    public void eliminarComentarioDeFirestore(String comentarioId) {
        // ID del usuario actual
        String idUsuarioActual = currentUser.getUid();

        // Obtengo el documento del comentario de Firestore
        db.collection("comentarios").document(comentarioId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Comentario comentario = documentSnapshot.toObject(Comentario.class);

                    // Obtengo el restaurante correspondiente al comentario
                    obtenerRestaurante(comentario.getIdRestaurante(), restaurante -> {
                        // Compruebo si el usuario actual es el propietario del restaurante o el usuario que publicó el comentario
                        if (idUsuarioActual.equals(comentario.getIdUsuario()) || idUsuarioActual.equals(restaurante.getIdUsuarioRestaurante())) {
                            // Elimino el comentario del array de comentarios del restaurante
                            eliminarComentarioDelArray(restaurante.getIdRestaurante(), comentarioId);

                            // Elimino el comentario de la collección comentarios
                            db.collection("comentarios").document(comentarioId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // si el comentario tiene respuestas, las elimino también
                                        if (comentario.getRespuestasRestaurante() != null) {
                                            for (Respuesta respuesta : comentario.getRespuestasRestaurante()) {
                                                eliminarRespuestaEnFirestore(respuesta.getIdRespuesta());
                                            }
                                        }
                                        // Actualizar la puntuación promedio después de eliminar el comentario
                                        actualizarPuntuacionPromedio();
                                        // Refrescar la interfaz
                                        cargarValoraciones();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ComentariosActivity", "Error eliminando comentario", e);
                                        Toast.makeText(this, "Error eliminando comentario", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "No tienes permiso para eliminar este comentario", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error obteniendo comentario", e);
                });
    }

    // Método para eliminar el idCmentario de la lista de comentarios de un restaurante
    public void eliminarComentarioDelArray(String restauranteId, String comentarioId) {
        Log.d("ComentariosActivity", "comentarioId: " + comentarioId + ", restauranteId: " + restauranteId);
        // Actualizar el documento del restaurante eliminando el id del comentario del array
        db.collection("restaurantes").document(restauranteId)
                .update("listaComentarios", FieldValue.arrayRemove(comentarioId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Comentario eliminado con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error actualizando listaComentarios", e);
                });
    }


    public void responderComentario(String comentarioId, String textoRespuesta) {
        // ID del usuario actual
        String idUsuarioActual = mAuth.getCurrentUser().getUid();

        // Fecha actual
        Date fechaRespuesta = new Date();

        //obtengo el documento del comentario al que se va a responder
        obtenerComentario(comentarioId, comentario -> {
            //y el documento del restaurante al que pertenece el comentario
            obtenerRestaurante(comentario.getIdRestaurante(), restaurante -> {
                // Compruebo si el usuario actual es el propietario del restaurante
                if (!idUsuarioActual.equals(restaurante.getIdUsuarioRestaurante())) {
                    Toast.makeText(this, "No tienes permisos para responder a este comentario", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Obtenemos el nombre y la imagen del usuario
                obtenerUsuario(idUsuarioActual, documentSnapshot -> {
                    String fotoPerfilRestaurante = documentSnapshot.getString("fotoPerfil");
                    String nombreUsuario = documentSnapshot.getString("nombreUsuario");

                    Respuesta nuevaRespuesta = new Respuesta(nombreUsuario, textoRespuesta, fechaRespuesta, fotoPerfilRestaurante, idUsuarioActual, comentarioId);
                    // Añadir la nueva respuesta a la lista de respuestas del comentario
                    comentario.getRespuestasRestaurante().add(nuevaRespuesta);

                    comentarioAdapter.notifyDataSetChanged();

                    // Guardamos la nueva respuesta en Firestore
                    guardarRespuestaEnFirestore(nuevaRespuesta, comentarioId, aVoid -> {
                        Toast.makeText(this, "¡Respuesta publicada con éxito!", Toast.LENGTH_SHORT).show();
                        cargarValoraciones();  // Recargar los comentarios
                    });
                });
            });
        });
    }

    public void obtenerComentario(String comentarioId, OnSuccessListener<Comentario> onSuccessListener) {
        db.collection("comentarios").document(comentarioId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Comentario comentario = documentSnapshot.toObject(Comentario.class);

                    // Verificamod e inicializamos la lista de respuestas en caso de que sea null(porquw aún no había respuestas)
                    if (comentario.getRespuestasRestaurante() == null) {
                        comentario.setRespuestasRestaurante(new ArrayList<>());
                    }

                    onSuccessListener.onSuccess(comentario);
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error obteniendo datos del comentario", e);
                });
    }


    public void guardarRespuestaEnFirestore(Respuesta nuevaRespuesta, String comentarioId, OnSuccessListener<Void> onSuccessListener) {
        // Guardamos la nueva respuesta en Firestore
        db.collection("respuestas")
                .add(nuevaRespuesta)
                .addOnSuccessListener(documentReference -> {
                    // Guardo el ID del documento creado en el campo idRespuesta del objeto Respuesta
                    String docId = documentReference.getId();
                    db.collection("respuestas").document(docId)
                            .update("idRespuesta", docId)
                            .addOnSuccessListener(aVoid -> {
                                // Actualizo el objeto nuevaRespuesta con el id ya asignado en el campo idRespuesta, así al añadirlo después al array en firestore, no saldrá null
                                nuevaRespuesta.setIdRespuesta(docId);

                                // Añado el objeto de la respuesta a la lista de respuestas del comentario
                                db.collection("comentarios").document(comentarioId)
                                        .update("respuestasRestaurante", FieldValue.arrayUnion(nuevaRespuesta))
                                        .addOnSuccessListener(onSuccessListener)
                                        .addOnFailureListener(e -> {
                                            Log.e("ComentariosActivity", "Error guardando respuesta en la lista respuestas del comentario", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ComentariosActivity", "Error actualizando idRespuesta", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error publicando respuesta", e);
                    Toast.makeText(this, "Error publicando respuesta", Toast.LENGTH_SHORT).show();
                });
    }


    public void eliminarRespuestaEnFirestore(String respuestaId) {
        // ID del usuario actual
        String idUsuarioActual = currentUser.getUid();

        // Obtengo el documento de la respuesta de Firestore
        Task<DocumentSnapshot> getRespuestaTask = db.collection("respuestas").document(respuestaId).get();

        getRespuestaTask.addOnSuccessListener(documentSnapshot -> {
                    Respuesta respuesta = documentSnapshot.toObject(Respuesta.class);

                    // Compruebo si el usuario actual es el propietario del restaurante
                    if (idUsuarioActual.equals(respuesta.getIdUsuarioRestaurante())) {
                        // Elimino la respuesta del array de respuestas del comentario en Firestore
                        Task<Void> eliminarRespuestaArrayTask = eliminarRespuestaDelArray(respuesta.getIdComentario(), respuesta);

                        // Elimino la respuesta de la colección respuestas
                        Task<Void> eliminarRespuestaDocTask = db.collection("respuestas").document(respuestaId).delete();

                        // usamos Tasks.whenAllSuccess() para esperar a que ambas tareas de eliminación se completen antes de llamar a cargarValoraciones()
                        Tasks.whenAllSuccess(eliminarRespuestaArrayTask, eliminarRespuestaDocTask)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Respuesta eliminada con éxito", Toast.LENGTH_SHORT).show();
                                    // Refrescar la interfaz después de completar ambas eliminaciones
                                    cargarValoraciones();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ComentariosActivity", "Error eliminando respuesta", e);
                                    Toast.makeText(this, "Error eliminando respuesta", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No tienes permiso para eliminar esta respuesta", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error obteniendo respuesta", e);
                });
    }

    public Task<Void> eliminarRespuestaDelArray(String comentarioId, Respuesta respuesta) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        // Obtener el documento del comentario
        DocumentReference comentarioRef = db.collection("comentarios").document(comentarioId);

        // transacción para actualizar el array de respuestas (ya que firestore no deja eliminar el objeto directamente)
        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(comentarioRef);
                    Comentario comentario = snapshot.toObject(Comentario.class);

                    // si el comentario existe y tiene respuestas
                    if (comentario != null && comentario.getRespuestasRestaurante() != null) {
                        // Obtengo la lista de respuestas
                        List<Respuesta> respuestas = comentario.getRespuestasRestaurante();

                        // Busco el índice de la respuesta que quiero eliminar
                        int index = -1;
                        for (int i = 0; i < respuestas.size(); i++) {
                            if (respuestas.get(i).getIdRespuesta().equals(respuesta.getIdRespuesta())) {
                                index = i;
                                break;
                            }
                        }

                        // y si lo encuentro lo elimino del array
                        if (index != -1) {
                            respuestas.remove(index);
                            // Actualizo el documento de comentarios con la lista de respuestas actualizada
                            transaction.set(comentarioRef, comentario);
                        }
                    }

                    return null;
                })
                .addOnSuccessListener(result -> {
                    Log.d("ComentariosActivity", "Respuesta eliminada del array con éxito");
                    taskCompletionSource.setResult(null); // Marcar la tarea como completada
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error eliminando respuesta del array", e);
                    taskCompletionSource.setException(e); // Marcar la tarea como fallida
                });

        return taskCompletionSource.getTask();
    }



    // método para calcular el promedio de las puntuaciones (sumar todas las puntuaciones y luego dividir esa suma por el número total de puntuaciones que se han realizado)
    public void actualizarPuntuacionPromedio() {
        // Obtener todos los comentarios del restaurante
        db.collection("comentarios")
                .whereEqualTo("idRestaurante", restauranteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalPuntuacion = 0;
                        int numComentarios = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Comentario comentario = document.toObject(Comentario.class);
                            totalPuntuacion += comentario.getPuntuacion();
                            numComentarios++;
                        }
                        //Si numComentarios es cero, entonces puntuacionPromedio se establece en cero (para evitar que el resultado de la división sea NaN (Not a Number))
                        if (numComentarios != 0) {
                            puntuacionPromedio = (double) totalPuntuacion / numComentarios;
                        } else {
                            puntuacionPromedio = 0;
                        }

                        // Actualizar la puntuación promedio del restaurante en Firestore
                        db.collection("restaurantes").document(restauranteId)
                                .update("puntuacion", puntuacionPromedio)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ComentariosActivity", "Puntuación promedio actualizada con éxito");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ComentariosActivity", "Error actualizando puntuación promedio", e);
                                });
                    } else {
                        Log.d("MainActivity", "Error obteniendo valoraciones: ", task.getException());
                    }
                });
    }


    public void limpiarCampos() {
        textoComentario.getEditText().setText("");
        punuacion.setRating(0);
    }

    public void volverAInfoRestaurante() {
        Intent intent = new Intent(this, InfoRestauranteActivity.class);
        intent.putExtra("idRestaurante", restauranteId);
        startActivity(intent);
    }

    public void volverAlMenuPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}