package org.example.Mensajes;

import org.example.AlmacenarInformacion.FactoryAlmacenarInformacion;

public class FactoryMensajes {

    private static IMensajes instancia;

    public static synchronized IMensajes getMensajes() {
        if (instancia == null) {
            instancia = new ImpMensajes(FactoryAlmacenarInformacion.getAlmacenarInformacion());
        }
        return instancia;
    }
}