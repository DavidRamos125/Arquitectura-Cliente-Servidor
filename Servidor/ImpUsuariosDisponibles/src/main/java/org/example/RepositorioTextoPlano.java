package org.example;

import java.util.ArrayList;
import java.util.List;

public class RepositorioTextoPlano implements RepositorioUsuario {
    private final String ruta;

    public RepositorioTextoPlano(){
        this.ruta = UsuariosUtil.getProperty("textoPlano.ruta");
    }

    @Override
    public List<UsuarioDTO> getUsuariosDisponibles() {
        List<String> usuariosPlano = UsuariosUtil.getLines(ruta);
        List<UsuarioDTO> usuarios = new ArrayList<>();

        for(String usuario: usuariosPlano){
            usuarios.add(textoAUsuario(usuario));
        }

        return usuarios;
    }

    private UsuarioDTO textoAUsuario(String texto){
        String[] campos = texto.split(",", -1);

        if (campos.length != 5) {
            return null;
        }

        return new UsuarioDTO(
                campos[0].trim(),
                campos[1].trim(),
                campos[2].trim(),
                campos[3].trim(),
                campos[4].trim()
        );
    }
}
