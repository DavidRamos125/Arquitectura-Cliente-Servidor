package org.example.DTORecords;

import java.time.LocalDateTime;

public record ArchivoDTO(
        long id,
        long mensajeId,
        String variante,
        String nombreArchivo,
        String ruta,
        LocalDateTime fecha) {
}
