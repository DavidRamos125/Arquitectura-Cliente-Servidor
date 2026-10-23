package org.example;

public record UsuarioDTO (String codigo,
                          String nombre,
                          String apellidos,
                          String contrasena,
                          String programa) {

    public UsuarioDTO getUsuarioLimpio(){
        return new UsuarioDTO (this.codigo, this.nombre, this.apellidos, "none", this.programa);
    }

}
