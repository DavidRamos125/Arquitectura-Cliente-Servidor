package org.example;

import java.io.IOException;

public interface IConexion {

    void enviar(byte[] datos) throws IOException;

    byte[] recibir() throws IOException;

    void desconectar() throws IOException;
}
