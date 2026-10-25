package org.example.filtros;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class UtilidadImagen {

    private UtilidadImagen() {
    }

    public static BufferedImage cargar(Object dato) {
        if (!(dato instanceof File archivo)) {
            throw new IllegalArgumentException(
                    "El dato debe ser un File"
            );
        }

        String nombre = archivo.getName().toLowerCase();

        if (!nombre.endsWith(".jpg")
                && !nombre.endsWith(".jpeg")
                && !nombre.endsWith(".png")) {
            throw new IllegalArgumentException(
                    "El archivo debe ser JPG o PNG"
            );
        }

        try {
            BufferedImage imagen = ImageIO.read(archivo);

            if (imagen == null) {
                throw new IllegalArgumentException(
                        "No se pudo leer la imagen"
                );
            }

            return imagen;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al leer la imagen", e
            );
        }
    }

    public static File guardar(
            BufferedImage imagen,
            File original,
            String sufijo) {

        String nombre = original.getName();
        int punto = nombre.lastIndexOf('.');

        String base = punto >= 0
                ? nombre.substring(0, punto)
                : nombre;

        File salida = new File(
                original.getAbsoluteFile().getParentFile(),
                base + "_" + sufijo + ".png"
        );

        try {
            if (!ImageIO.write(imagen, "png", salida)) {
                throw new IOException(
                        "No se encontró un escritor PNG"
                );
            }

            return salida;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al guardar la imagen", e
            );
        }
    }
}
