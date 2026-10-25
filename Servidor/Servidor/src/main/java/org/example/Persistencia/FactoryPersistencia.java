package org.example.Persistencia;

public class FactoryPersistencia {
    public static IPersistencia getPersistencia() {
        return new ImpPersistencia();
    }
}