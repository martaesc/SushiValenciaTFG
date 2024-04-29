package com.example.sushivalenciatfg.models;

/**
 * Esta es la clase Usuario, que representa un usuario en la aplicación.
 * Contiene información como el ID del usuario, el nombre de usuario, el correo electrónico, el tipo de usuario y la foto de perfil.
 */
public class Usuario {
    private String uid;
    private String nombreUsuario;
    private String correo;
    private String tipoUsuario; //String en vez de TipoUsuario porque Firebase no acepta tipos enum
    private String fotoPerfil;


    /**
     * Constructor vacío de la clase Usuario.
     */
    public Usuario() {
    }


    /**
     * Constructor para crear una nueva instancia de la clase Usuario.
     *
     * @param uid           El ID del usuario.
     * @param nombreUsuario El nombre del usuario.
     * @param correo        El correo electrónico del usuario.
     * @param tipoUsuario   El tipo de usuario.
     */
    public Usuario(String uid, String nombreUsuario, String correo, String tipoUsuario) {
        this.uid = uid;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.tipoUsuario = tipoUsuario;
        this.fotoPerfil = ""; //Por defecto la foto de perfil es un string vacío, ya que es opcional
    }

    // Métodos getter y setter
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

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }


    /**
     * Este método se utiliza para obtener una representación en cadena de la instancia de Usuario.
     *
     * @return Una cadena que representa la instancia de Usuario.
     */
    public String toString() {
        return "Usuario{" +
                "uid='" + getUid() + '\'' +
                ", nombreUsuario='" + getNombreUsuario() + '\'' +
                ", correo='" + getCorreo() + '\'' +
                ", tipoUsuario='" + getTipoUsuario() + '\'' +
                ", fotoPerfil='" + getFotoPerfil() + '\'' +
                '}';
    }
}
