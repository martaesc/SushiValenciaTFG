package com.example.sushivalenciatfg.models;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class Comentario {
    private String idComentario;
    private String nombreUsuario;
    private int puntuacion;
    private Date fecha;
    private String textoComentario;
    private List<Respuesta> respuestasRestaurante;

    private String imagenUsuario;
    private String idRestaurante;
    private String idUsuario;

    public Comentario(String idComentario, String nombreUsuario, int puntuacion, Date fecha, String textoComentario, List<Respuesta> respuestasRestaurante, String imagenUsuario, String idRestaurante, String idUsuario) {
        this.idComentario = idComentario;
        this.nombreUsuario = nombreUsuario;
        this.puntuacion = puntuacion;
        this.fecha = fecha;
        this.textoComentario = textoComentario;
        this.respuestasRestaurante = respuestasRestaurante;
        this.imagenUsuario = imagenUsuario;
        this.idRestaurante = idRestaurante;
        this.idUsuario = idUsuario;
    }

    public Comentario(String nombreUsuario, int puntuacion, Date fecha, String textoComentario, String imagenUsuario, String idRestaurante, String idUsuario) {
        this.nombreUsuario = nombreUsuario;
        this.puntuacion = puntuacion;
        this.fecha = fecha;
        this.textoComentario = textoComentario;
        this.imagenUsuario = imagenUsuario;
        this.idRestaurante = idRestaurante;
        this.idUsuario = idUsuario;
    }




    public Comentario() {
    }

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

    public String getImagenUsuario() {
        return imagenUsuario;
    }

    public void setImagenUsuario(String imagenUsuario) {
        this.imagenUsuario = imagenUsuario;
    }

    @Override
    public String toString() {
        return "Comentario{" +
                "idComentario='" + getIdComentario() + '\'' +
                ", nombreUsuario='" + getNombreUsuario() + '\'' +
                ", puntuacion=" + getPuntuacion() +
                ", fecha=" + getFecha() +
                ", textoComentario='" + getTextoComentario() + '\'' +
                ", respuestasRestaurante=" + getRespuestasRestaurante() +
                ", imagenUsuario='" + getImagenUsuario() + '\'' +
                ", idRestaurante='" + getIdRestaurante() + '\'' +
                ", idUsuario='" + getIdUsuario() + '\'' +
                '}';
    }
}
