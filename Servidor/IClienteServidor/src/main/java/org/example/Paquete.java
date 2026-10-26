package org.example;

public record Paquete(
        String tipo,
        String emisor,
        String receptor,
        long id,
        int parte,
        int total,
        String nombreArchivo,
        String hash,
        byte[] datos) {

    public Paquete {
        tipo = tipo == null ? "" : tipo;
        emisor = emisor == null ? "" : emisor;
        receptor = receptor == null ? "" : receptor;
        nombreArchivo = nombreArchivo == null ? "" : nombreArchivo;
        hash = hash == null ? "" : hash;
        datos = datos == null ? new byte[0] : datos;
    }
}
