package org.example;

import java.net.Socket;

public class Factory {
    public static IProtocoloComunicacion getProtocoloComunicacion(){
        return ProtocoloComunicacionTCP.getInstance();
    }

    public static IConexion getConexion(Socket conexion){
        return new ConexionTCP(conexion);
    }
}
