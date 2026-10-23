
package org.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Util {

    private static final String CONFIG_PATH = "./resources/config/config.properties";
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream input = new FileInputStream(CONFIG_PATH)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "No se pudo cargar la configuración: " + CONFIG_PATH
            );
        }
    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }

    public static List<String> getLines(String ruta) {
        List<String> usuarios = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(
                Path.of(ruta), StandardCharsets.UTF_8)) {

            String linea;
            boolean primeraLinea = true;

            while ((linea = reader.readLine()) != null) {

                // Ignorar la cabecera
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                if (linea.isBlank()) {
                    continue;
                }
                usuarios.add(linea);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return usuarios;
    }
}
