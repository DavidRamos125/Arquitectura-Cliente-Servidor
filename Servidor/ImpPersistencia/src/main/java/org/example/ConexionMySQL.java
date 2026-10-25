package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionMySQL {

    private static final String CREAR_TABLA_MENSAJES = """
            CREATE TABLE IF NOT EXISTS mensajes (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                emisor VARCHAR(50) NOT NULL,
                receptor VARCHAR(50) NOT NULL,
                tipo VARCHAR(10) NOT NULL,
                texto TEXT NULL,
                nombre_archivo VARCHAR(255) NULL,
                hash VARCHAR(64) NULL,
                caracteres INT NOT NULL DEFAULT 0,
                palabras INT NOT NULL DEFAULT 0,
                fecha DATETIME NOT NULL
            )
            """;

    private static final String CREAR_TABLA_LOGS = """
            CREATE TABLE IF NOT EXISTS logs (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                fecha DATETIME NOT NULL,
                evento VARCHAR(50) NOT NULL,
                emisor VARCHAR(50) NULL,
                receptor VARCHAR(50) NULL,
                detalle TEXT NULL
            )
            """;

    private final String url;
    private final String usuario;
    private final String contrasena;

    public ConexionMySQL() {
        String host = UtilPersistencia.getProperty("db.host");
        String puerto = UtilPersistencia.getProperty("db.puerto");
        String nombre = UtilPersistencia.getProperty("db.nombre");

        this.url = "jdbc:mysql://" + host + ":" + puerto + "/" + nombre
                + "?createDatabaseIfNotExist=true&useSSL=false"
                + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        this.usuario = UtilPersistencia.getProperty("db.usuario");
        this.contrasena = UtilPersistencia.getProperty("db.contrasena");

        inicializarTablas();
    }

    public Connection obtener() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    private void inicializarTablas() {
        try (Connection conexion = obtener();
             Statement statement = conexion.createStatement()) {
            statement.execute(CREAR_TABLA_MENSAJES);
            statement.execute(CREAR_TABLA_LOGS);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo inicializar la base de datos: " + e.getMessage(), e);
        }
    }
}