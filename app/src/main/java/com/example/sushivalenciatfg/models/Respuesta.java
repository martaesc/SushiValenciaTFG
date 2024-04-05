package com.example.sushivalenciatfg.models;

import java.util.Date;

public class Respuesta {
    private String idRespuesta;
    private String nombreUsuario;
    private String textoRespuesta;
    private Date fecha;
    private String fotoPerfil;
    private String idUsuarioRestaurante;
    private String idComentario;

    public Respuesta(String idRespuesta, String nombreUsuario, String textoRespuesta, Date fecha, String fotoPerfilRestaurante, String idUsuarioRestaurante, String idComentario) {
        this.idRespuesta = idRespuesta;
        this.nombreUsuario = nombreUsuario;
        this.textoRespuesta = textoRespuesta;
        this.fecha = fecha;
        this.fotoPerfil = fotoPerfilRestaurante;
        this.idUsuarioRestaurante = idUsuarioRestaurante;
        this.idComentario = idComentario;
    }

    public Respuesta(String nombreUsuario, String textoRespuesta, Date fecha, String fotoPerfilRestaurante, String idUsuarioRestaurante, String idComentario) {
        this.nombreUsuario = nombreUsuario;
        this.textoRespuesta = textoRespuesta;
        this.fecha = fecha;
        this.fotoPerfil = fotoPerfilRestaurante;
        this.idUsuarioRestaurante = idUsuarioRestaurante;
        this.idComentario = idComentario;
    }

    public Respuesta() {
    }

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
