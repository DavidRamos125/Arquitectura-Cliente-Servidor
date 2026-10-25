package org.example.LogDeEventos;

import org.example.AlmacenarInformacion.FactoryAlmacenarInformacion;

public class FactoryLogDeEventos {
    public static ILogDeEventos getLogDeEventos() {
        return new ImpLogDeEventos(FactoryAlmacenarInformacion.getAlmacenarInformacion());
    }
}