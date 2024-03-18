package com.example.sushivalenciatfg.models;


public class Usuario {
    private String uid;
    private String nombreUsuario;
    private String correo;
    private String tipoUsuario; //String en vez de TipoUsuario porque Firebase no acepta tipos enum

    public Usuario(String uid, String nombreUsuario, String correo, String tipoUsuario){
        this.uid = uid;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.tipoUsuario = tipoUsuario;
    }

    public Usuario() {
    }

    public String getUid() {
        return uid;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String toString() {
        return "Usuario{" +
                "uid='" + getUid() + '\'' +
                ", nombreUsuario='" + getNombreUsuario() + '\'' +
                ", correo='" + getCorreo() + '\'' +
                ", tipoUsuario='" + getTipoUsuario() + '\'' +
                '}';
    }
}
