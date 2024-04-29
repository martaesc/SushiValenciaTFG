package com.example.sushivalenciatfg.models;

import java.util.Date;

/**
 * Esta es la clase Respuesta, que representa una respuesta hecha por un usuario a un comentario en un restaurante.
 * Contiene información como el ID de la respuesta, el nombre del usuario, el texto de la respuesta, la fecha de la respuesta, la foto de perfil del usuario, el ID del usuario del restaurante y el ID del comentario al que se responde.
 */
public class Respuesta {
    private String idRespuesta;
    private String nombreUsuario;
    private String textoRespuesta;
    private Date fecha;
    private String fotoPerfil;
    private String idUsuarioRestaurante;
    private String idComentario;


    /**
     * Constructor vacío de la clase Respuesta.
     */
    public Respuesta() {
    }

    /**
     * Constructor para crear una nueva instancia de la clase Respuesta.
     *
     * @param nombreUsuario El nombre del usuario que hizo la respuesta.
     * @param textoRespuesta El texto de la respuesta.
     * @param fecha La fecha en que se hizo la respuesta.
     * @param fotoPerfilRestaurante La foto de perfil del usuario.
     * @param idUsuarioRestaurante El ID del usuario del restaurante.
     * @param idComentario El ID del comentario al que se responde.
     */
    public Respuesta(String nombreUsuario, String textoRespuesta, Date fecha, String fotoPerfilRestaurante, String idUsuarioRestaurante, String idComentario) {
        this.nombreUsuario = nombreUsuario;
        this.textoRespuesta = textoRespuesta;
        this.fecha = fecha;
        this.fotoPerfil = fotoPerfilRestaurante;
        this.idUsuarioRestaurante = idUsuarioRestaurante;
        this.idComentario = idComentario;
    }

    // Métodos getter y setter...
    public String getIdRespuesta() {
        return idRespuesta;
    }
    public void setIdRespuesta(String idRespuesta) {
        this.idRespuesta = idRespuesta;
    }
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    public String getTextoRespuesta() {
        return textoRespuesta;
    }
    public void setTextoRespuesta(String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
    }
    public Date getFecha() {
        return fecha;
    }
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    public String getIdUsuarioRestaurante() {
        return idUsuarioRestaurante;
    }
    public void setIdUsuarioRestaurante(String idUsuarioRestaurante) {
        this.idUsuarioRestaurante = idUsuarioRestaurante;
    }
    public String getIdComentario() {
        return idComentario;
    }
    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }
    public String getfotoPerfilRestaurante() {
        return fotoPerfil;
    }
    public void setfotoPerfilRestaurante(String fotoPerfilRestaurante) {
        this.fotoPerfil = fotoPerfilRestaurante;
    }


    /**
     * Este método se utiliza para obtener una representación en cadena de la instancia de Respuesta.
     *
     * @return Una cadena que representa la instancia de Respuesta.
     */
    @Override
    public String toString() {
        return "Respuesta{" +
                "idRespuesta='" + getIdRespuesta() + '\'' +
                ", textoRespuesta='" + getTextoRespuesta() + '\'' +
                ", fecha=" + getFecha() +
                ", fotoPerfil='" + getfotoPerfilRestaurante() + '\'' +
                ", idUsuarioRestaurante='" + getIdUsuarioRestaurante() + '\'' +
                ", idComentario='" + getIdComentario() + '\'' +
                '}';
    }
}
