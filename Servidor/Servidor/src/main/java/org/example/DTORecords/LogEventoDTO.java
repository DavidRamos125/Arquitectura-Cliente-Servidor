package org.example.DTORecords;

import java.time.LocalDateTime;

public record LogEventoDTO(
        long id,
        LocalDateTime fecha,
        String evento,
        String emisor,
        String receptor,
        String detalle) {
}