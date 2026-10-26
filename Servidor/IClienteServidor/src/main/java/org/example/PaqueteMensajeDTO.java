package org.example;

public record PaqueteMensajeDTO(
        String tipoPaquete,
        MensajeDTO mensaje,
        long tamanoArchivo
) {}
