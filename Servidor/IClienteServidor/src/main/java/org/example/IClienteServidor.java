package org.example;

public interface IClienteServidor {
    String construirPaquete(MensajeDTO mensaje);
    MensajeDTO obtenerMensaje(String mensaje);
}
