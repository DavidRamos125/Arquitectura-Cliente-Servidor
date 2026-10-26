package org.example;

import java.io.IOException;
import java.net.Socket;

public class Factory {
    public static IProtocoloComunicacion getProtocoloComunicacion(){
        return ProtocoloComunicacionTCP.getInstance();
    }

    public static IConexion getConexion(Socket conexion){
        try {
            return new ConexionTCP(conexion);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
