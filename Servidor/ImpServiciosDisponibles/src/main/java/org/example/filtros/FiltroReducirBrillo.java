package org.example.filtros;

import org.example.interfaz.Filtro;

import java.awt.image.BufferedImage;
import java.io.File;

public class FiltroReducirBrillo implements Filtro {

    @Override
    public String getNombre() {
        return "brillo";
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

                rojo /= 2;
                verde /= 2;
                azul /= 2;

                int nuevoPixel = (alpha << 24)
                        | (rojo << 16)
                        | (verde << 8)
                        | azul;

                resultado.setRGB(x, y, nuevoPixel);
            }
        }

        return UtilidadImagen.guardar(
                resultado, (File) dato, "brillo50"
        );
    }
}