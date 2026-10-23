
package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Util {

    private static final String CONFIG_PATH = "./resources/config/tcpConfig.properties";

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
}
