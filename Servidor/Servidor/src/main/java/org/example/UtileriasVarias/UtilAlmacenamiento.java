package org.example.UtileriasVarias;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class UtilAlmacenamiento {

    private static final String RUTA_EXTERNA = "./resources/config/almacenamiento.properties";
    private static final String RUTA_CLASSPATH = "/config/almacenamiento.properties";
    private static final Properties properties = new Properties();

    static {
        try {
            Path externa = Path.of(RUTA_EXTERNA);
            if (Files.exists(externa)) {
                try (InputStream in = Files.newInputStream(externa)) {
                    properties.load(in);
                }
            } else {
                try (InputStream in = UtilAlmacenamiento.class.getResourceAsStream(RUTA_CLASSPATH)) {
                    if (in == null) {
                        throw new IOException("No se encontró " + RUTA_CLASSPATH);
                    }
                    properties.load(in);
                }
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "No se pudo cargar la configuración de almacenamiento: " + e.getMessage());
        }
    }

    public static String getProperty(String name) {
        return properties.getProperty(name, "").trim();
    }
}