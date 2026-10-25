package org.example;

public class FactoryPersistencia {
    public static IPersistencia getPersistencia() {
        return new ImpPersistencia();
    }
}