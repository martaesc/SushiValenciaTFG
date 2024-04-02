package com.example.sushivalenciatfg.models;

import java.time.LocalDate;
import java.util.Date;

public class Respuesta {
    private String idRespuesta;
    private String textoRespuesta;
    private Date fecha;
    private String imagenUsuarioRestaurante;
    private String idUsuarioRestaurante;
    private String idComentario;

    public Respuesta(String idRespuesta, String textoRespuesta, Date fecha, String imagenUsuarioRestaurante, String idUsuarioRestaurante, String idComentario) {
        this.idRespuesta = idRespuesta;
        this.textoRespuesta = textoRespuesta;
        this.fecha = fecha;
        this.imagenUsuarioRestaurante = imagenUsuarioRestaurante;
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

    public String getImagenUsuarioRestaurante() {
        return imagenUsuarioRestaurante;
    }

    public void setImagenUsuarioRestaurante(String imagenUsuarioRestaurante) {
        this.imagenUsuarioRestaurante = imagenUsuarioRestaurante;
    }

    @Override
    public String toString() {
        return "Respuesta{" +
                "idRespuesta='" + getIdRespuesta() + '\'' +
                ", textoRespuesta='" + getTextoRespuesta() + '\'' +
                ", fecha=" + getFecha() +
                ", imagenUsuarioRestaurante='" + getImagenUsuarioRestaurante() + '\'' +
                ", idUsuarioRestaurante='" + getIdUsuarioRestaurante() + '\'' +
                ", idComentario='" + getIdComentario() + '\'' +
                '}';
    }
}
