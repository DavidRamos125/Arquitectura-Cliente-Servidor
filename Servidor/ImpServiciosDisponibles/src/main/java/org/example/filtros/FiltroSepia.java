package org.example.filtros;

import org.example.interfaz.Filtro;

import java.awt.image.BufferedImage;
import java.io.File;

public class FiltroSepia implements Filtro {

    private int limitar(int valor) {
        return Math.clamp(valor, 0, 255);
    }

    @Override
    public String getNombre() {
        return "sepia";
    }

    @Override
    public Object procesar(Object dato) {
        BufferedImage original = UtilidadImagen.cargar(dato);

        int ancho = original.getWidth();
        int alto = original.getHeight();

        BufferedImage resultado = new BufferedImage(
                ancho, alto, BufferedImage.TYPE_INT_ARGB
        );

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int pixel = original.getRGB(x, y);

                int alpha = (pixel >>> 24) & 0xFF;
                int rojo = (pixel >>> 16) & 0xFF;
                int verde = (pixel >>> 8) & 0xFF;
                int azul = pixel & 0xFF;

                int nuevoRojo = limitar((int) (
                        0.393 * rojo
                                + 0.769 * verde
                                + 0.189 * azul
                ));

                int nuevoVerde = limitar((int) (
                        0.349 * rojo
                                + 0.686 * verde
                                + 0.168 * azul
                ));

                int nuevoAzul = limitar((int) (
                        0.272 * rojo
                                + 0.534 * verde
                                + 0.131 * azul
                ));

                int nuevoPixel = (alpha << 24)
                        | (nuevoRojo << 16)
                        | (nuevoVerde << 8)
                        | nuevoAzul;

                resultado.setRGB(x, y, nuevoPixel);
            }
        }

        return UtilidadImagen.guardar(
                resultado, (File) dato, "sepia"
        );
    }
}