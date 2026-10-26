package org.example;

import com.fasterxml.jackson.databind.JsonNode;

public record SolicitudDTO(
        TipoSolicitud tipo,
        JsonNode datos
) {}