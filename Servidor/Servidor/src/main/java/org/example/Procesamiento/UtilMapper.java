package org.example.Procesamiento;

import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.Paquete;
import org.example.UtileriasVarias.TipoContenido;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class UtilMapper {

    private UtilMapper() {
    }

    public static MensajeProcesadoDTO paqueteToMensajeProcesadoDTO(Paquete paquete) {
        if (paquete == null) {
            throw new IllegalArgumentException("El paquete no puede ser nulo");
        }

        boolean esImagen = esImagen(paquete.tipo());

        String texto = esImagen
                ? null
                : new String(paquete.datos(), StandardCharsets.UTF_8);

        byte[] archivo = esImagen ? paquete.datos() : null;
        String nombreArchivo = esImagen ? paquete.nombreArchivo() : "";

        return new MensajeProcesadoDTO(
                0L,
                paquete.emisor(),
                paquete.receptor(),
                esImagen ? TipoContenido.IMAGEN : TipoContenido.TEXTO,
                texto,
                archivo,
                nombreArchivo,
                paquete.hash(),
                0,
                0,
                LocalDateTime.now()
        );
    }

    private static boolean esImagen(String tipo) {
        return "IMG_INICIO".equals(tipo)
                || "IMG_PARTE".equals(tipo)
                || "IMG_FIN".equals(tipo);
    }
}
