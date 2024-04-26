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

/**
 * Esta es la clase ComentariosActivity, que extiende de AppCompatActivity.
 * Esta clase se encarga de manejar la funcionalidad de las valoraciones de los restaurantes.
 */
public class ComentariosActivity extends AppCompatActivity {

    // Referencias a los elementos de la interfaz de usuario
    private TextView subtitulo;
    private RatingBar punuacion;
    private TextInputLayout textoComentario;
    private Button btnPublicar;
    private RecyclerView rvComentarios;
    private Button btnVolver;
    private Button btnVolverMenu;

    // Referencia a la base de datos Firestore, a la autenticación de Firebase y al usuario actual
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Referencia al adaptador para el RecyclerView de comentarios
    private ComentarioAdapter comentarioAdapter;

    private List<Comentario> listaComentarios;
    private String restauranteId;
    private double puntuacionPromedio;


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
        visibilidadComponentesInterfaz();

        btnPublicar.setOnClickListener(v -> publicarValoracion());
        btnVolver.setOnClickListener(v -> volverAInfoRestaurante());
        btnVolverMenu.setOnClickListener(v -> volverAlMenuPrincipal());
    }


    /**
     * Este método se encarga de obtener las referencias a los elementos de la interfaz de usuario.
     */
    private void obtenerReferencias() {
        subtitulo = findViewById(R.id.textView2);
        punuacion = findViewById(R.id.ratingBarPuntuacionUsuario);
        textoComentario = findViewById(R.id.comentarioInputLayout);
        btnPublicar = findViewById(R.id.btnPublicar);
        rvComentarios = findViewById(R.id.recyclerViewComentarios);
        btnVolver = findViewById(R.id.btnVolverAInfoActivity);
        btnVolverMenu = findViewById(R.id.btnVolverAlMenuPrincipal);
    }

    /**
     * Este método se encarga de controlar la visibilidad de algunos de los componentes de la interfaz de usuario en función del tipo de usuario.
     */
    public void visibilidadComponentesInterfaz() {
        // Ocultamos por defecto algunos componentes (para evitar el problema de que tarden unos segundos en desaparecer al entrar a la pantalla)
        subtitulo.setVisibility(View.GONE);
        punuacion.setVisibility(View.GONE);
        textoComentario.setVisibility(View.GONE);
        btnPublicar.setVisibility(View.GONE);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            obtenerUsuario(userId, documentSnapshot -> {
                // comprobamos el tipo de usuario que es el usuario actual
                String tipoUsuario = documentSnapshot.getString("tipoUsuario");
                // Si es tipo "Restaurante", entonces comprobamos si es el creador del restaurante al que pertenecen las valoraciones
                if ("Restaurante".equals(tipoUsuario)) {
                    // Buscamos en la colección "restaurantes" un documento donde el campo "idUsuarioRestaurante" coincide con el ID del usuario actual
                    db.collection("restaurantes")
                            .whereEqualTo("idUsuarioRestaurante", userId)
                            .get()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                    // Si el usuario es el creador del restaurante, ocultamos los componentes relacionados con la publicación de valoraciones
                                    subtitulo.setVisibility(View.GONE);
                                    punuacion.setVisibility(View.GONE);
                                    textoComentario.setVisibility(View.GONE);
                                    btnPublicar.setVisibility(View.GONE);
                                } else {
                                    // Si el usuario es un restaurante, pero no el creador del restaurante, mostramos los componentes para publicar una valoración
                                    subtitulo.setVisibility(View.VISIBLE);
                                    punuacion.setVisibility(View.VISIBLE);
                                    textoComentario.setVisibility(View.VISIBLE);
                                    btnPublicar.setVisibility(View.VISIBLE);
                                }
                            });
                } else {
                    // Si el tipo de usuario es "Cliente", mostramos los componentes para publicar una valoración
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


    /**
     * Este método se encarga de cargar las valoraciones de un restaurante específico en la interfaz de usuario desde la base de datos Firestore.
     */
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
                        // mostrar los comentarios en el RecyclerView
                        rvComentarios.setLayoutManager(new LinearLayoutManager(this));
                        comentarioAdapter = new ComentarioAdapter(listaComentarios, this);
                        rvComentarios.setAdapter(comentarioAdapter);
                    } else {
                        Log.d("MainActivity", "Error obteniendo valoraciones: ", task.getException());
                        Toast.makeText(this, "Error obteniendo valoraciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Este método se encarga de obtener los datos de un usuario específico desde la base de datos Firestore.
     *
     * @param usuarioId El ID del usuario a obtener.
     * @param onSuccessListener El listener que se llama cuando se obtiene el documento del usuario con éxito.
     */
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


    /**
     * Este método se encarga de obtener los datos de un restaurante específico desde la base de datos Firestore.
     *
     * @param restauranteId El ID del restaurante a obtener.
     * @param onSuccessListener El listener que se llama cuando se obtiene el objeto Restaurante con éxito.
     */
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

    /**
     * Este método se encarga de guardar la valoración de un restaurante en Firestore, mostrarla en la interfaz de usuario y actualizar la puntuación promedio del restaurante.
     */
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

        String idUsuario = currentUser.getUid();
        // Date en vez de LocalDate porque esta ultima está disponible a partir de la API 26, y quiero que mi aplicación sea compatible en dispositivos con API 24 o superior)
        Date fechaPublicacion = new Date();

        // Obtenemos el nombre y la imagen del usuario actual
        obtenerUsuario(idUsuario, documentSnapshot -> {
            String fotoPerfil = documentSnapshot.getString("fotoPerfil");
            if (fotoPerfil == null) {
                Log.e("ComentariosActivity", "Error obteniendo foto de perfil del usuario");
            }
            String nombreUsuario = documentSnapshot.getString("nombreUsuario");
            //creamos el comentario con los datos obtenidos y lo añadimos a la lista de comentarios del restaurante
            Comentario nuevoComentario = new Comentario(nombreUsuario, calificacion, fechaPublicacion, textoComentario, fotoPerfil, restauranteId, idUsuario);
            listaComentarios.add(nuevoComentario);

            // Notificamos al adaptador del RecyclerView que se ha añadido un nuevo comentario para que se actualice la interfaz
            comentarioAdapter.notifyDataSetChanged();

            // Guardamos el nuevo comentario en Firestore
            guardarComentarioEnFirestore(nuevoComentario, documentReference -> {
                Toast.makeText(this, "¡Comentario publicado con éxito!", Toast.LENGTH_SHORT).show();
                // Añadimos el comentario al array de comentarios del restaurante
                db.collection("restaurantes").document(restauranteId)
                        .update("listaComentarios", FieldValue.arrayUnion(nuevoComentario.getIdComentario()));

                limpiarCampos();
                // Actualizamos la puntuación del restaurante para enviarselo a InfoRestauranteActivity
                actualizarPuntuacionPromedio();
            });
        });
    }


    /**
     * Este método se encarga de la lógica de guardar un nuevo comentario en la base de datos Firestore.
     *
     * @param nuevoComentario El nuevo comentario a guardar en Firestore.
     * @param onSuccessListener El listener que se llama cuando se obtiene el documento del comentario con éxito.
     */
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

    /**
     * Este método se encarga de la lógica de obtener un comentario específico de la base de datos Firestore.
     *
     * @param comentarioId El ID del comentario a obtener.
     * @param onSuccessListener El listener que se llama cuando se obtiene el objeto `Comentario` con éxito.
     */
    public void obtenerComentario(String comentarioId, OnSuccessListener<Comentario> onSuccessListener) {
        db.collection("comentarios").document(comentarioId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Comentario comentario = documentSnapshot.toObject(Comentario.class);
                    // si el comentario aún no tiene respuestas, se inicializa la lista de respuestas con una nueva lista vacía.
                    // De esta manera, se pueden añadir respuestas a esta lista más adelante sin encontrarse con un NullPointerException.
                    if (comentario.getRespuestasRestaurante() == null) {
                        comentario.setRespuestasRestaurante(new ArrayList<>());
                    }
                    onSuccessListener.onSuccess(comentario);
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error obteniendo datos del comentario", e);
                });
    }


    /**
     * Este método se encarga de eliminar un comentario específico de la base de datos Firestore.
     *
     * @param comentarioId El ID del comentario a eliminar.
     */
    public void eliminarComentarioDeFirestore(String comentarioId) {
        String idUsuarioActual = currentUser.getUid();

        // Obtengo el documento que corresponde al ID del comentario que se va a eliminar
        db.collection("comentarios").document(comentarioId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Comentario comentario = documentSnapshot.toObject(Comentario.class);
                    // Obtengo el restaurante al que pertenece el comentario
                    obtenerRestaurante(comentario.getIdRestaurante(), restaurante -> {
                        // si el usuario actual es el propietario del restaurante o el que publicó el comentario
                        if (idUsuarioActual.equals(comentario.getIdUsuario()) || idUsuarioActual.equals(restaurante.getIdUsuarioRestaurante())) {
                            // Elimino el comentario del array de comentarios del restaurante
                            eliminarComentarioDelArray(restaurante.getIdRestaurante(), comentarioId);
                            // y de la collección comentarios
                            db.collection("comentarios").document(comentarioId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // si el comentario tiene respuestas aociadas, se eliminan también
                                        if (comentario.getRespuestasRestaurante() != null) {
                                            for (Respuesta respuesta : comentario.getRespuestasRestaurante()) {
                                                eliminarRespuestaEnFirestore(respuesta.getIdRespuesta());
                                            }
                                        }
                                        // Actualizamos la puntuación promedio después de eliminar el comentario y refrescamos la interfaz
                                        actualizarPuntuacionPromedio();
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


    /**
     * Este método se encarga de la lógica de eliminar un comentario del array de comentarios de un restaurante específico en Firestore.
     *
     * @param comentarioId El ID del comentario a eliminar.
     */
    public void eliminarComentarioDelArray(String restauranteId, String comentarioId) {
        Log.d("ComentariosActivity", "comentarioId: " + comentarioId + ", restauranteId: " + restauranteId);

        db.collection("restaurantes").document(restauranteId)
                .update("listaComentarios", FieldValue.arrayRemove(comentarioId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Comentario eliminado con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error actualizando listaComentarios", e);
                });
    }


    /**
     * Este método se encarga de publicar una respuesta a un comentario existente.
     *
     * @param comentarioId El ID del comentario al que se va a responder.
     * @param textoRespuesta El texto de la respuesta.
     */
    public void responderComentario(String comentarioId, String textoRespuesta) {
        String idUsuarioActual = mAuth.getCurrentUser().getUid();
        Date fechaRespuesta = new Date();

        //obtenemos el documento del comentario al que se va a responder
        obtenerComentario(comentarioId, comentario -> {
            //y el documento del restaurante al que pertenece el comentario
            obtenerRestaurante(comentario.getIdRestaurante(), restaurante -> {
                // si el usuario actual no es el propietario del restaurante, no puede responder al comentario
                if (!idUsuarioActual.equals(restaurante.getIdUsuarioRestaurante())) {
                    Toast.makeText(this, "No tienes permisos para responder a este comentario", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Si sí que es el propietario del reataurante, obtenemos el nombre de usuario y la imagen
                obtenerUsuario(idUsuarioActual, documentSnapshot -> {
                    String fotoPerfilRestaurante = documentSnapshot.getString("fotoPerfil");
                    String nombreUsuario = documentSnapshot.getString("nombreUsuario");
                    Respuesta nuevaRespuesta = new Respuesta(nombreUsuario, textoRespuesta, fechaRespuesta, fotoPerfilRestaurante, idUsuarioActual, comentarioId);
                    // Añadimos la nueva respuesta a la lista de respuestas del comentario
                    comentario.getRespuestasRestaurante().add(nuevaRespuesta);

                    // Notificamos al adaptador del RecyclerView que se ha añadido una nueva respuesta para que se actualice la interfaz
                    comentarioAdapter.notifyDataSetChanged();

                    // Guardamos la nueva respuesta en Firestore
                    guardarRespuestaEnFirestore(nuevaRespuesta, comentarioId, aVoid -> {
                        Toast.makeText(this, "¡Respuesta publicada con éxito!", Toast.LENGTH_SHORT).show();
                        cargarValoraciones();
                    });
                });
            });
        });
    }


    /**
     * Este método se encarga de la lógica de guardar una nueva respuesta en la base de datos Firestore.
     *
     * @param nuevaRespuesta La nueva respuesta a guardar en Firestore.
     * @param comentarioId El ID del comentario al que pertenece la respuesta.
     * @param onSuccessListener El listener que se llama cuando se completa la operación con éxito.
     */
    public void guardarRespuestaEnFirestore(Respuesta nuevaRespuesta, String comentarioId, OnSuccessListener<Void> onSuccessListener) {
        db.collection("respuestas")
                .add(nuevaRespuesta)
                .addOnSuccessListener(documentReference -> {
                    // Guardamos el ID del documento creado (la respuesta) en su campo idRespuesta
                    String docId = documentReference.getId();
                    db.collection("respuestas").document(docId)
                            .update("idRespuesta", docId)
                            .addOnSuccessListener(aVoid -> {
                                // Añadimos la respuesta al array de respuestas del comentario
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

    /**
     * Este método se encarga de la lógica de eliminar una respuesta existente de la base de datos Firestore.
     *
     * @param respuestaId El ID de la respuesta a eliminar.
     */
    public void eliminarRespuestaEnFirestore(String respuestaId) {
        String idUsuarioActual = currentUser.getUid();

        // Obtengemos el documento que corresponde al ID de la respuesta que se va a eliminar
        Task<DocumentSnapshot> getRespuestaTask = db.collection("respuestas").document(respuestaId).get();
        getRespuestaTask.addOnSuccessListener(documentSnapshot -> {
                    Respuesta respuesta = documentSnapshot.toObject(Respuesta.class);
                    // si el usuario actual es el propietario del restaurante
                    if (idUsuarioActual.equals(respuesta.getIdUsuarioRestaurante())) {
                        // eliminamos la respuesta del array de respuestas del comentario en Firestore
                        Task<Void> eliminarRespuestaArrayTask = eliminarRespuestaDelArray(respuesta.getIdComentario(), respuesta);
                        // y de la colección respuestas
                        Task<Void> eliminarRespuestaDocTask = db.collection("respuestas").document(respuestaId).delete();
                        // usamos Tasks.whenAllSuccess() para esperar a que ambas tareas de eliminación se completen antes de llamar a cargarValoraciones()
                        Tasks.whenAllSuccess(eliminarRespuestaArrayTask, eliminarRespuestaDocTask)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Respuesta eliminada con éxito", Toast.LENGTH_SHORT).show();
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

    /**
     * Este método se encarga de la lógica de eliminar una respuesta del array de respuestas de un comentario específico en Firestore.
     *
     * @param comentarioId El ID del comentario al que pertenece la respuesta.
     * @param respuesta La respuesta a eliminar del array de respuestas.
     * @return La tarea que se completa cuando se ha eliminado la respuesta del array de respuestas.
     */
    public Task<Void> eliminarRespuestaDelArray(String comentarioId, Respuesta respuesta) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        // Obtenemos el documento del comentario al que pertenece la respuesta
        DocumentReference comentarioRef = db.collection("comentarios").document(comentarioId);
        // transacción para actualizar el array de respuestas de manera atómica (firestore no deja eliminar un objeto directamente de un array, hay que sobreescribir el array entero)
        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(comentarioRef);
                    Comentario comentario = snapshot.toObject(Comentario.class);
                    // si el comentario existe y tiene una lista de respuestas, busco la respuesta que quiero eliminar y la elimino
                    if (comentario != null && comentario.getRespuestasRestaurante() != null) {
                        List<Respuesta> respuestas = comentario.getRespuestasRestaurante();
                        int index = -1;
                        for (int i = 0; i < respuestas.size(); i++) {
                            // si encuentro la respuesta en la lista, me guardo su índice para pasarselo a remove()
                            if (respuestas.get(i).getIdRespuesta().equals(respuesta.getIdRespuesta())) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            respuestas.remove(index);
                            // Actualizo el documento de comentarios con la lista de respuestas actualizada
                            transaction.set(comentarioRef, comentario);
                        }
                    }
                    return null; // null para satisfacer el requisito de que la función de la transacción debe devolver un valor, pero no necesitamos devolver nada en especifico
                })
                .addOnSuccessListener(result -> {
                    Log.d("ComentariosActivity", "Respuesta eliminada del array con éxito");
                    taskCompletionSource.setResult(null); // Marcamosla tarea como completada
                })
                .addOnFailureListener(e -> {
                    Log.e("ComentariosActivity", "Error eliminando respuesta del array", e);
                    taskCompletionSource.setException(e); // Marcamos la tarea como fallida
                });

        return taskCompletionSource.getTask();
    }


    /**
     * Este método se encarga de la lógica de actualizar la puntuación promedio de un restaurante en la base de datos Firestore.
     */
    public void actualizarPuntuacionPromedio() {
        // Obtenemos todos los comentarios del restaurante
        db.collection("comentarios")
                .whereEqualTo("idRestaurante", restauranteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalPuntuacion = 0;
                        int numComentarios = 0;
                        // y sumamos todas las puntuaciones
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Comentario comentario = document.toObject(Comentario.class);
                            totalPuntuacion += comentario.getPuntuacion();
                            numComentarios++;
                        }
                       // Si hay comentarios, dividimos la suma de las puntuaciones entre el número de comentarios para obtener la puntuación promedio
                        if (numComentarios != 0) {
                            puntuacionPromedio = (double) totalPuntuacion / numComentarios;
                        } else {
                            puntuacionPromedio = 0;
                        }

                        // Actualizamos la puntuación promedio del restaurante en Firestore
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


    /**
     * Este método se encarga de limpiar los campos de texto y la puntuación de la interfaz de usuario.
     */
    public void limpiarCampos() {
        textoComentario.getEditText().setText("");
        punuacion.setRating(0);
    }


    /**
     * Este método se encarga de navegar a la actividad de información del restaurante (InfoRestauranteActivity)
     */
    public void volverAInfoRestaurante() {
        Intent intent = new Intent(this, InfoRestauranteActivity.class);
        intent.putExtra("idRestaurante", restauranteId);
        startActivity(intent);
    }


    /**
     * Este método se encarga de navegar a la actividad principal (MainActivity)
     */
    public void volverAlMenuPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}