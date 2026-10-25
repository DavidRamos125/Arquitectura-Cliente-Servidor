package org.example.filtros;

import org.example.interfaz.Filtro;

import java.awt.image.BufferedImage;
import java.io.File;

public class FiltroRotar180 implements Filtro {

    @Override
    public String getNombre() {
        return "rotar180";
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
                resultado.setRGB(
                        ancho - 1 - x,
                        alto - 1 - y,
                        original.getRGB(x, y)
                );
            }
        }

        return UtilidadImagen.guardar(
                resultado, (File) dato, "rotada180"
        );
    }
}
