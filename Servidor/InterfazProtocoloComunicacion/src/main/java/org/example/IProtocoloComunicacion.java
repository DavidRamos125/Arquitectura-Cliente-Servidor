package org.example;

public interface IProtocoloComunicacion {
    IConexion escuchar();
    void iniciar();
    void detener();
}
