package com.example.sushivalenciatfg.models;

import java.util.List;

public class Restaurante {
    private String idRestaurante;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String horario;

    private String linkRestaurante;
    private double puntuacion;
    private String imagenRestaurante;
    private List<String> comentarios;
    private String idUsuarioRestaurante;

    public Restaurante(String idRestaurante, String nombre, String descripcion, String direccion, String telefono, String horario, String linkRestaurante, double puntuacion, String imagenRestaurante, List<String> comentarios, String idUsuarioRestaurante) {
        this.idRestaurante = idRestaurante;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.telefono = telefono;
        this.horario = horario;
        this.linkRestaurante = linkRestaurante;
        this.puntuacion = puntuacion;
        this.imagenRestaurante = imagenRestaurante;
        this.comentarios = comentarios;
        this.idUsuarioRestaurante = idUsuarioRestaurante;
    }



    public Restaurante() {
    }

    //Constructor para crear un nuevo restaurante(aún no tiene comentarios ni puntuaicones)
    public Restaurante(String nombreRestaurante, String descripcionRestaurante, String direccion, String telefono, String horario, String linkRestaurante, String imagenBase64, String idUsuario) {
        this.nombre = nombreRestaurante;
        this.descripcion = descripcionRestaurante;
        this.direccion = direccion;
        this.telefono = telefono;
        this.horario = horario;
        this.linkRestaurante = linkRestaurante;
        this.imagenRestaurante = imagenBase64;
        this.idUsuarioRestaurante = idUsuario;
    }


    public String getIdRestaurante() {
        return idRestaurante;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setIdRestaurante(String idRestaurante) {
        this.idRestaurante = idRestaurante;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public double getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(double puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getImagenRestaurante() {
        return imagenRestaurante;
    }

    public void setImagenRestaurante(String imagenRestaurante) {
        this.imagenRestaurante = imagenRestaurante;
    }

    public List<String> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<String> comentarios) {
        this.comentarios = comentarios;
    }

    public String getIdUsuarioRestaurante() {
        return idUsuarioRestaurante;
    }

    public void setIdUsuarioRestaurante(String idUsuarioRestaurante) {
        this.idUsuarioRestaurante = idUsuarioRestaurante;
    }

    public String getLinkRestaurante() {
        return linkRestaurante;
    }

    public void setLinkRestaurante(String linkRestaurante) {
        this.linkRestaurante = linkRestaurante;
    }

    public String toString() {
        return "Restaurante{" +
                "idRestaurante='" + getIdRestaurante() + '\'' +
                ", nombre='" + getNombre() + '\'' +
                ", descripcion='" + getDescripcion() + '\'' +
                ", direccion='" + getDireccion() + '\'' +
                ", telefono='" + getTelefono() + '\'' +
                ", horario='" + getHorario() + '\'' +
                ", link web='" + getLinkRestaurante() + '\'' +
                ", puntuacion='" + getPuntuacion() + '\'' +
                ", imagenRestaurante='" + getImagenRestaurante() + '\'' +
                ", comentarios=" + getComentarios() +
                ", idUsuarioRestaurante='" + getIdUsuarioRestaurante() + '\'' +
                '}';
    }
}
