package org.example.DTORecords;

import org.example.UtileriasVarias.TipoContenido;

import java.time.LocalDateTime;

public record MensajeProcesadoDTO(
        long id,
        String emisor,
        String receptor,
        TipoContenido tipo,
        String texto,
        byte[] archivo,
        String nombreArchivo,
        String hash,
        int caracteres,
        int palabras,
        LocalDateTime fecha) {
}