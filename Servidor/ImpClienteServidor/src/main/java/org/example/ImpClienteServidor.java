package org.example;

public class ImpClienteServidor implements IClienteServidor {

    @Override
    public String construirPaquete(MensajeDTO mensajeDTO) {
        return "";
    }

    @Override
    public MensajeDTO obtenerMensaje(String s) {
        return null;
    }
}
