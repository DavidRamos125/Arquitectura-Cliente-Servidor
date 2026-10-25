package org.example.filtros;

import org.example.interfaz.Filtro;
import java.awt.image.BufferedImage;
import java.io.File;

public class FiltroEscalaGrises implements Filtro {

    @Override
    public String getNombre() {
        return "escalaGrises";
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

                int gris = (int) Math.round(
                        0.299 * rojo
                                + 0.587 * verde
                                + 0.114 * azul
                );

                int nuevoPixel = (alpha << 24)
                        | (gris << 16)
                        | (gris << 8)
                        | gris;

                resultado.setRGB(x, y, nuevoPixel);
            }
        }

        return UtilidadImagen.guardar(
                resultado, (File) dato, "grises"
        );
    }
}
