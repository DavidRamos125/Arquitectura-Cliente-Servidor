package org.example.filtros;

import org.example.interfaz.Filtro;

public class FiltroContarPalabras implements Filtro {

    @Override
    public String getNombre() {
        return "palabras";
    }

    @Override
    public Object procesar(Object dato) {
        if (!(dato instanceof String texto)) {
            throw new IllegalArgumentException(
                    "El dato debe ser un String"
            );
        }

        texto = texto.strip();

        if (texto.isEmpty()) {
            return 0;
        }

        return String.valueOf(texto.split("\\s+").length);
    }
}
