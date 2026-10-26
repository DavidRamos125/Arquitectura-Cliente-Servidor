package org.example;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ImpClienteServidor implements IClienteServidor {

    private static final byte FIN_CABECERA = '\n';
    private static final String SEPARADOR = "\\|";

    @Override
    public byte[] construir(Paquete paquete) {
        String cabecera = String.join("|",
                paquete.tipo(),
                paquete.emisor(),
                paquete.receptor(),
                Long.toString(paquete.id()),
                Integer.toString(paquete.parte()),
                Integer.toString(paquete.total()),
                paquete.nombreArchivo(),
                paquete.hash());

        validarCabecera(cabecera);

        byte[] bytesCabecera = cabecera.getBytes(StandardCharsets.UTF_8);
        byte[] datos = paquete.datos();

        ByteArrayOutputStream salida = new ByteArrayOutputStream(
                bytesCabecera.length + 1 + datos.length);

        salida.writeBytes(bytesCabecera);
        salida.write(FIN_CABECERA);
        salida.writeBytes(datos);

        return salida.toByteArray();
    }

    @Override
    public Paquete obtener(byte[] datos) {
        if (datos == null) {
            throw new IllegalArgumentException("El paquete no puede ser null");
        }

        int finCabecera = -1;
        for (int i = 0; i < datos.length; i++) {
            if (datos[i] == FIN_CABECERA) {
                finCabecera = i;
                break;
            }
        }

        if (finCabecera < 0) {
            throw new IllegalArgumentException(
                    "El paquete no contiene una cabecera válida");
        }

        String[] campos = new String(
                datos, 0, finCabecera, StandardCharsets.UTF_8)
                .split(SEPARADOR, -1);

        if (campos.length != 8) {
            throw new IllegalArgumentException(
                    "La cabecera debe tener 8 campos y tiene " + campos.length);
        }

        try {
            return new Paquete(
                    campos[0],
                    campos[1],
                    campos[2],
                    Long.parseLong(campos[3]),
                    Integer.parseInt(campos[4]),
                    Integer.parseInt(campos[5]),
                    campos[6],
                    campos[7],
                    Arrays.copyOfRange(datos, finCabecera + 1, datos.length));

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "La cabecera contiene números inválidos", e);
        }
    }

    private static void validarCabecera(String cabecera) {
        if (cabecera.indexOf('\n') >= 0 || cabecera.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(
                    "La cabecera no puede contener saltos de línea");
        }

        long separadores = cabecera.chars()
                .filter(caracter -> caracter == '|')
                .count();

        if (separadores != 7) {
            throw new IllegalArgumentException(
                    "Los campos de la cabecera no pueden contener '|'");
        }
    }
}
