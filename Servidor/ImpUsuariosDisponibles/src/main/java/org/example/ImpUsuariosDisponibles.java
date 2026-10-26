package org.example;

import java.util.List;

public class ImpUsuariosDisponibles implements IUsuariosDisponibles {
    List<UsuarioDTO> usuariosDisponibles;
    RepositorioUsuario repositorioUsuario;

    public ImpUsuariosDisponibles(){
        repositorioUsuario = UsuariosFactory.getRepositorioUsuario();
        getUsuarios();
    }

    private void getUsuarios(){
        usuariosDisponibles = repositorioUsuario.getUsuariosDisponibles();
    }


    @Override
    public UsuarioDTO comprobarUsuario(String codigo, String contrasena) {
        if (usuariosDisponibles != null){
            for (UsuarioDTO usuarioDTO : usuariosDisponibles){
                if(usuarioDTO != null
                        && usuarioDTO.codigo().equals(codigo)
                        && usuarioDTO.contrasena().equals(contrasena)){
                    return usuarioDTO.getUsuarioLimpio();
                }
            }
        }
        return null;
    }
}
