package com.example.sushivalenciatfg.models;

import java.util.Date;
import java.util.List;

/**
 * Esta es la clase Comentario, que representa un comentario hecho por un usuario en un restaurante.
 */
public class Comentario {
    private String idComentario;
    private String nombreUsuario;
    private int puntuacion;
    private Date fecha;
    private String textoComentario;
    private List<Respuesta> respuestasRestaurante;
    private String fotoPerfil;
    private String idRestaurante;
    private String idUsuario;


    /**
     * Constructor vacío de la clase Comentario.
     */
    public Comentario() {
    }

    /**
     * Constructor para crear una nueva instancia de la clase Comentario.
     *
     * @param nombreUsuario El nombre del usuario que hizo el comentario.
     * @param puntuacion La puntuación dada al restaurante.
     * @param fecha La fecha en que se hizo el comentario.
     * @param textoComentario El texto del comentario.
     * @param fotoPerfil La foto de perfil del usuario.
     * @param idRestaurante El ID del restaurante al que se refiere el comentario.
     * @param idUsuario El ID del usuario que hizo el comentario.
     */
    public Comentario(String nombreUsuario, int puntuacion, Date fecha, String textoComentario, String fotoPerfil, String idRestaurante, String idUsuario) {
        this.nombreUsuario = nombreUsuario;
        this.puntuacion = puntuacion;
        this.fecha = fecha;
        this.textoComentario = textoComentario;
        this.fotoPerfil = fotoPerfil;
        this.idRestaurante = idRestaurante;
        this.idUsuario = idUsuario;
    }

    // Métodos getter y setter
    public String getIdComentario() {
        return idComentario;
    }
    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    public int getPuntuacion() {
        return puntuacion;
    }
    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }
    public Date getFecha() {
        return fecha;
    }
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    public String getTextoComentario() {
        return textoComentario;
    }
    public void setTextoComentario(String textoComentario) {
        this.textoComentario = textoComentario;
    }
    public String getIdRestaurante() {
        return idRestaurante;
    }
    public void setIdRestaurante(String idRestaurante) {
        this.idRestaurante = idRestaurante;
    }
    public String getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
    public List<Respuesta> getRespuestasRestaurante() {
        return respuestasRestaurante;
    }
    public void setRespuestasRestaurante(List<Respuesta> respuestasRestaurante) {
        this.respuestasRestaurante = respuestasRestaurante;
    }
    public String getfotoPerfil() {
        return fotoPerfil;
    }
    public void setfotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }


    /**
     * Este método se utiliza para obtener una representación en cadena de la instancia de Comentario.
     *
     * @return Una cadena que representa la instancia de Comentario.
     */
    @Override
    public String toString() {
        return "Comentario{" +
                "idComentario='" + getIdComentario() + '\'' +
                ", nombreUsuario='" + getNombreUsuario() + '\'' +
                ", puntuacion=" + getPuntuacion() +
                ", fecha=" + getFecha() +
                ", textoComentario='" + getTextoComentario() + '\'' +
                ", respuestasRestaurante=" + getRespuestasRestaurante() +
                ", fotoPerfil='" + getfotoPerfil() + '\'' +
                ", idRestaurante='" + getIdRestaurante() + '\'' +
                ", idUsuario='" + getIdUsuario() + '\'' +
                '}';
    }
}
