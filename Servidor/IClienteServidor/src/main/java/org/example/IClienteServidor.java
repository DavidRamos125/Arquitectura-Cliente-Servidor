package org.example;

public interface IClienteServidor {

    byte[] construir(Paquete paquete);
    Paquete obtener(byte[] datos);
}
