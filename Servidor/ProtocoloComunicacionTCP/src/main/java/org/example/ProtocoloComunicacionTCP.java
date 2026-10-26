package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProtocoloComunicacionTCP implements IProtocoloComunicacion {
    private static ProtocoloComunicacionTCP instance;
    private ServerSocket listener;
    private final int puerto;

    private ProtocoloComunicacionTCP() {
        this.puerto = Integer.parseInt(ProtocoloConfig.getProperty("tcp.puerto"));
    }

    public static synchronized ProtocoloComunicacionTCP getInstance() {
        if(instance == null){
            instance = new ProtocoloComunicacionTCP();
        }
        return instance;
    }

    @Override
    public synchronized void iniciar() {
        if (listener != null && !listener.isClosed()) {
            return; // El servidor ya está iniciado
        }

        try {
            listener = new ServerSocket(puerto);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo iniciar el servidor en el puerto " + puerto, e
            );
        }
    }

    @Override
    public IConexion escuchar() {
        ServerSocket servidor = listener;

        if (servidor == null || servidor.isClosed()) {
            throw new IllegalStateException(
                    "El servidor no está iniciado o ya fue detenido"
            );
        }

        try {
            Socket socketCliente = servidor.accept();
            return new ConexionTCP(socketCliente);
        } catch (IOException e) {
            if (servidor.isClosed()) {
                return null;
            }

            throw new IllegalStateException(
                    "Error al aceptar una conexión TCP", e
            );
        }
    }

    @Override
    public synchronized void detener() {
        if (listener != null && !listener.isClosed()) {
            try {
                listener.close();
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Error al detener el servidor TCP", e
                );
            }
        }
    }

    @Override
    public boolean estaActivo() {
        ServerSocket servidor = listener;
        return servidor != null && !servidor.isClosed();
    }
}
