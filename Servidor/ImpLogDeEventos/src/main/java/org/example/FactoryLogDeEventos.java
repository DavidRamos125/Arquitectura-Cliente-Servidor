package org.example;

public class FactoryLogDeEventos {
    public static ILogDeEventos getLogDeEventos() {
        return new ImpLogDeEventos(FactoryAlmacenarInformacion.getAlmacenarInformacion());
    }
}