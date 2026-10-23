package org.example;

public class Factory {
    public static RepositorioUsuario getRepositorioUsuario(){
        return switch (Util.getProperty("persistencia.tipo")) {
            case "textoPlano" -> new RepositorioTextoPlano();
            case "baseDeDatos" -> new RepositorioBaseDeDatos();
            default -> null;
        };
    }
}
