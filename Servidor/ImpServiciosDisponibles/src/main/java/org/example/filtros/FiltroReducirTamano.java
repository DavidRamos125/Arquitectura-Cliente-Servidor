package org.example.filtros;

import org.example.interfaz.Filtro;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

public class FiltroReducirTamano implements Filtro {

    @Override
    public String getNombre() {
        return "tamaño50";
    }

    @Override
    public Object procesar(Object dato) {
        BufferedImage original = UtilidadImagen.cargar(dato);

        int anchoOriginal = original.getWidth();
        int altoOriginal = original.getHeight();

        int nuevoAncho = Math.max(1, anchoOriginal / 2);
        int nuevoAlto = Math.max(1, altoOriginal / 2);

        BufferedImage resultado = new BufferedImage(
                nuevoAncho,
                nuevoAlto,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = resultado.createGraphics();

        try {
            g2d.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g2d.drawImage(
                    original,
                    0, 0,
                    nuevoAncho, nuevoAlto,
                    null
            );
        } finally {
            g2d.dispose();
        }

        return UtilidadImagen.guardar(
                resultado, (File) dato, "tamano50"
        );
    }
}