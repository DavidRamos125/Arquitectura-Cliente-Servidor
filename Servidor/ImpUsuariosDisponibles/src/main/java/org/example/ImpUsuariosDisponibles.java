package org.example;

import java.util.List;

public class ImpUsuariosDisponibles implements IUsuariosDisponibles {
    List<UsuarioDTO> usuariosDisponibles;
    RepositorioUsuario repositorioUsuario;

    public ImpUsuariosDisponibles(){
        repositorioUsuario = Factory.getRepositorioUsuario();
        getUsuarios();
    }

    private void getUsuarios(){
        usuariosDisponibles = repositorioUsuario.getUsuariosDisponibles();
    }


    @Override
    public UsuarioDTO comprobarUsuario(String codigo, String contrasena) {
        if (usuariosDisponibles != null){
            for (UsuarioDTO usuarioDTO : usuariosDisponibles){
                if(usuarioDTO.codigo().equals(codigo) && usuarioDTO.contrasena().equals(contrasena)){
                    System.out.println("Usuario disponible encontrado");
                    return usuarioDTO.getUsuarioLimpio();
                }
            }
        }
        return null;
    }
}
