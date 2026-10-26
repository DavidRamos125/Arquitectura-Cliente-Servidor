package org.example.ConexionesClientes;

import org.example.IConexion;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class ConexionCliente {

    private final UUID idConexion;
    private String codigoUsuario;
    private final IConexion conexion;
    private final LocalDateTime fechaConexion;
    private final Object bloqueoEnvio = new Object();
    private volatile boolean activa;

    public ConexionCliente(String codigoUsuario, IConexion conexion) {
        if (codigoUsuario == null || codigoUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del usuario no puede estar vacío"
            );
        }
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexión no puede ser null"
            );
        }
        this.idConexion = UUID.randomUUID();
        this.codigoUsuario = codigoUsuario;
        this.conexion = conexion;
        this.fechaConexion = LocalDateTime.now();
        this.activa = true;
    }

    public UUID getIdConexion() {
        return idConexion;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public LocalDateTime getFechaConexion() {
        return fechaConexion;
    }

    public boolean estaActiva() {
        return activa;
    }

    public synchronized void establecerUsuarioAutenticado(String codigoUsuario) {
        if (codigoUsuario == null || codigoUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del usuario autenticado no puede estar vacío"
            );
        }
        this.codigoUsuario = codigoUsuario;
    }

    public void enviarDatos(byte[] datos) throws IOException {
        synchronized (bloqueoEnvio) {
            if (!estaActiva()) {
                throw new IOException(
                        "La conexión del cliente está cerrada"
                );
            }

            conexion.enviar(datos);
        }
    }

    public byte[] recibirDatos() throws IOException {
        return conexion.recibir();
    }

    public void cerrar() throws IOException {
        synchronized (bloqueoEnvio) {
            if (!activa) {
                return;
            }

            activa = false;
            conexion.desconectar();
        }
    }
}
