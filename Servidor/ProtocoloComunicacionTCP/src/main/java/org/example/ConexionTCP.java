
package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConexionTCP implements IConexion {

    private Socket conexion;
    private BufferedReader lector;
    private PrintWriter escritor;

    public ConexionTCP() {
    }

    public ConexionTCP(Socket conexion) {
        conectar(conexion);
    }

    private void conectar(Socket socket) {
        try {
            this.conexion = socket;

            this.lector = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            this.escritor = new PrintWriter(
                    socket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Se conserva la excepción original
            }

            throw new IllegalStateException(
                    "No se pudo inicializar la conexión TCP", e
            );
        }
    }

    @Override
    public String recibir() {
        comprobarConexion();

        try {
            // Lee hasta encontrar un salto de línea.
            return lector.readLine();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error al recibir un mensaje TCP", e
            );
        }
    }

    @Override
    public void enviar(String mensaje) {
        comprobarConexion();

        escritor.println(mensaje);

        if (escritor.checkError()) {
            throw new IllegalStateException(
                    "Error al enviar el mensaje TCP"
            );
        }
    }

    @Override
    public void desconectar() {
        if (conexion == null || conexion.isClosed()) {
            return;
        }

        try {
            conexion.close();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error al cerrar la conexión TCP", e
            );
        }
    }

    private void comprobarConexion() {
        if (conexion == null || conexion.isClosed()
                || !conexion.isConnected()) {
            throw new IllegalStateException(
                    "La conexión TCP no está disponible"
            );
        }
    }
}
