package org.example.filtros;

import org.example.interfaz.Filtro;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FiltroSHA256 implements Filtro {

    @Override
    public String getNombre() {
        return "sha256";
    }

    @Override
    public String procesar(Object dato) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            if (dato instanceof String texto) {
                byte[] hash = digest.digest(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(hash);
            }

            if (dato instanceof File archivo) {
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    byte[] buffer = new byte[8192];
                    int bytesLeidos;

                    while ((bytesLeidos = fis.read(buffer)) != -1) {
                        digest.update(buffer, 0, bytesLeidos);
                    }
                }

                return HexFormat.of().formatHex(digest.digest());
            }

            throw new IllegalArgumentException(
                    "El dato debe ser de tipo String o File"
            );

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Error al calcular SHA-256", e);
        }
    }

}