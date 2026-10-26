package org.example;

public interface IClienteServidor {

    byte[] construirPaquete(MensajeDTO mensajeDTO);

    MensajeDTO obtenerMensaje(byte[] datos);

    byte[] construirPaquete(PaqueteMensajeDTO paquete);

    PaqueteMensajeDTO obtenerPaquete(byte[] datos);
}
