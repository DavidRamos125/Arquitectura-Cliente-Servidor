package org.example;

public class FactoryAlmacenarInformacion {
    public static IAlmacenarInformacion getAlmacenarInformacion() {
        return new ImpAlmacenarInformacion(FactoryPersistencia.getPersistencia());
    }
}