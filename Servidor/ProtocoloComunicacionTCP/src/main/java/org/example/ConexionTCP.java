package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConexionTCP implements IConexion {

    private static final int TAMANO_MAXIMO_MENSAJE =
            16 * 1024 * 1024; // 16 MiB

    private final Socket conexion;
    private final DataInputStream entrada;
    private final DataOutputStream salida;

    public ConexionTCP(Socket conexion) throws IOException {
        this.conexion = conexion;

        this.entrada = new DataInputStream(
                conexion.getInputStream()
        );

        this.salida = new DataOutputStream(
                conexion.getOutputStream()
        );
    }

    @Override
    public synchronized void enviar(byte[] datos)
            throws IOException {

        comprobarConexion();

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos no pueden ser null"
            );
        }

        if (datos.length > TAMANO_MAXIMO_MENSAJE) {
            throw new IOException(
                    "El mensaje supera el tamaño máximo permitido"
            );
        }

        salida.writeInt(datos.length);
        salida.write(datos);
        salida.flush();
    }

    @Override
    public synchronized byte[] recibir() throws IOException {
        comprobarConexion();

        int longitud = entrada.readInt();

        if (longitud < 0 ||
                longitud > TAMANO_MAXIMO_MENSAJE) {

            throw new IOException(
                    "Longitud de mensaje no válida: " + longitud
            );
        }

        byte[] datos = new byte[longitud];

        entrada.readFully(datos);

        return datos;
    }

    @Override
    public synchronized void desconectar() throws IOException {
        conexion.close();
    }

    private void comprobarConexion() throws IOException {
        if (conexion.isClosed() || !conexion.isConnected()) {
            throw new IOException(
                    "La conexión TCP no está disponible"
            );
        }
    }
}