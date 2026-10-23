package org.example;

import java.util.List;

public class RepositorioBaseDeDatos implements RepositorioUsuario {

    @Override
    public List<UsuarioDTO> getUsuariosDisponibles() {
        return List.of();
    }
}
