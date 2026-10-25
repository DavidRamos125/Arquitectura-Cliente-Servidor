package org.example.filtros;

import org.example.interfaz.Filtro;

public class FiltroContarCaracteres implements Filtro {

    @Override
    public String getNombre() {
        return "caracteres";
    }

    @Override
    public Object procesar(Object dato) {
        if (!(dato instanceof String texto)) {
            throw new IllegalArgumentException(
                    "El dato debe ser un String"
            );
        }

        return texto.codePointCount(0, texto.length());
    }
}
