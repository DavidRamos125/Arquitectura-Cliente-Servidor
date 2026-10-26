package org.example.ConexionesClientes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GestorConexiones {

    private final Map<UUID, ConexionCliente> conexiones;

    public GestorConexiones() {
        this.conexiones = new ConcurrentHashMap<>();
    }


    public boolean registrar(ConexionCliente conexion) {
        if (conexion == null) {
            return false;
        }

        if (!conexion.estaActiva()) {
            return false;
        }

        return conexiones.putIfAbsent(
                conexion.getIdConexion(),
                conexion
        ) == null;
    }

    public void cerrarConexion(UUID id) {
        ConexionCliente conexion = conexiones.remove(id);

        if (conexion != null) {
            cerrarInternamente(conexion);
        }
    }

    public void cerrarConexionesUsuario(String codigo) {
        if (codigo == null) {
            return;
        }

        conexiones.forEach((id, conexion) -> {
            if (codigo.equals(conexion.getCodigoUsuario())) {
                if (conexiones.remove(id, conexion)) {
                    cerrarInternamente(conexion);
                }
            }
        });
    }

    public List<ConexionCliente> conexionesDe(String codigoUsuario) {
        if (codigoUsuario == null) {
            return List.of();
        }

        return conexiones.values()
                .stream()
                .filter(ConexionCliente::estaActiva)
                .filter(conexion -> codigoUsuario.equals(conexion.getCodigoUsuario()))
                .toList();
    }

    public Set<String> listarUsuariosConectados() {
        return conexiones.values()
                .stream()
                .filter(ConexionCliente::estaActiva)
                .map(ConexionCliente::getCodigoUsuario)
                .collect(Collectors.toSet());
    }


    public int contarConexionesUsuario(String codigo) {
        if (codigo == null) {
            return 0;
        }

        return (int) conexiones.values()
                .stream()
                .filter(ConexionCliente::estaActiva)
                .filter(c -> codigo.equals(c.getCodigoUsuario()))
                .count();
    }

    public int contarConexiones() {
        return (int) conexiones.values()
                .stream()
                .filter(ConexionCliente::estaActiva)
                .count();
    }

    public void cerrarTodas() {
        conexiones.forEach((id, conexion) -> {
            if (conexiones.remove(id, conexion)) {
                cerrarInternamente(conexion);
            }
        });
    }

    private void cerrarInternamente(ConexionCliente conexion) {
        try {
            conexion.cerrar();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error al cerrar la conexión "
                            + conexion.getIdConexion(),
                    e
            );
        }
    }
}
