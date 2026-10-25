package org.example.AlmacenarInformacion;

import org.example.Persistencia.FactoryPersistencia;

public class FactoryAlmacenarInformacion {
    public static IAlmacenarInformacion getAlmacenarInformacion() {
        return new ImpAlmacenarInformacion(FactoryPersistencia.getPersistencia());
    }
}