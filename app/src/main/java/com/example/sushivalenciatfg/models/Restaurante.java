package com.example.sushivalenciatfg.models;

import java.util.List;

/**
 * Esta es la clase Restaurante, que representa un restaurante en la aplicación.
 * Contiene información como el ID del restaurante, el nombre, la descripción, la dirección, el teléfono, el horario, el enlace al sitio web del restaurante, la puntuación, la imagen del restaurante, una lista de comentarios y el ID del usuario del restaurante.
 */
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
    private List<Comentario> comentarios;
    private String idUsuarioRestaurante;

    /**
     * Constructor vacío de la clase Restaurante.
     */
    public Restaurante() {
    }

    /**
     * Constructor para crear una nueva instancia de la clase Restaurante.
     *
     * @param nombreRestaurante El nombre del restaurante.
     * @param descripcionRestaurante La descripción del restaurante.
     * @param direccion La dirección del restaurante.
     * @param telefono El teléfono del restaurante.
     * @param horario El horario del restaurante.
     * @param linkRestaurante El enlace al sitio web del restaurante.
     * @param imagenRestaurante La imagen del restaurante.
     * @param idUsuario El ID del usuario del restaurante.
     */
    public Restaurante(String nombreRestaurante, String descripcionRestaurante, String direccion, String telefono, String horario, String linkRestaurante, String imagenRestaurante, String idUsuario) {
        this.nombre = nombreRestaurante;
        this.descripcion = descripcionRestaurante;
        this.direccion = direccion;
        this.telefono = telefono;
        this.horario = horario;
        this.linkRestaurante = linkRestaurante;
        this.imagenRestaurante = imagenRestaurante;
        this.idUsuarioRestaurante = idUsuario;
    }

    // Métodos getter y setter
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
    public List<Comentario> getComentarios() {
        return comentarios;
    }
    public void setComentarios(List<Comentario> comentarios) {
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


    /**
     * Este método se utiliza para obtener una representación en cadena de la instancia de Restaurante.
     *
     * @return Una cadena que representa la instancia de Restaurante.
     */
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
