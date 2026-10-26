package org.example.Procesamiento;

import org.example.AlmacenarInformacion.IAlmacenarInformacion;
import org.example.AlmacenarInformacion.ImpAlmacenarInformacion;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.Fachada.Factory;
import org.example.PFabrica.Fabrica;
import org.example.Persistencia.IPersistencia;
import org.example.UtileriasVarias.TipoContenido;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

public class ProcesarMensaje {

    private final aplicarServicios serviciosDisponibles;

    private IAlmacenarInformacion almacenar;
    private IPersistencia persistencia;
    private boolean inicializado;

    public ProcesarMensaje() {
        serviciosDisponibles = Fabrica.getAplicarServicios();
    }

    public void procesarMensaje(MensajeProcesadoDTO mensaje) {
        if (mensaje == null) {
            return;
        }

        inicializarPersistencia();

        try {
            guardarOriginal(mensaje);

            Object entrada = construirEntrada(mensaje);

            if (entrada == null) {
                return;
            }

            Map<String, Object> datos = serviciosDisponibles.procesar(entrada);

            if (datos == null) {
                System.err.println("No se pudieron procesar los datos del mensaje");
                return;
            }

            MensajeProcesadoDTO procesado = enriquecer(mensaje, datos);
            guardarResultado(procesado, datos);

        } catch (RuntimeException e) {
            System.err.println("No se pudo procesar el mensaje: " + e.getMessage());
        }
    }

    private void inicializarPersistencia() {
        if (inicializado) {
            return;
        }

        synchronized (this) {
            if (inicializado) {
                return;
            }

            try {
                persistencia = Factory.getPersistenciaImp();
            } catch (RuntimeException e) {
                System.err.println("Persistencia no disponible: " + e.getMessage());
            }

            almacenar = new ImpAlmacenarInformacion(persistencia);
            inicializado = true;
        }
    }

    private void guardarOriginal(MensajeProcesadoDTO mensaje) {
        if (mensaje.tipo() == TipoContenido.TEXTO && mensaje.texto() != null) {
            almacenar.guardarTexto(mensaje.emisor(), "mensaje.txt", mensaje.texto());
        }
    }

    private Object construirEntrada(MensajeProcesadoDTO mensaje) {
        if (mensaje.tipo() == TipoContenido.TEXTO) {
            if (mensaje.texto() != null) {
                return mensaje.texto();
            }

            if (mensaje.archivo() != null) {
                return new String(mensaje.archivo(), StandardCharsets.UTF_8);
            }

            return null;
        }

        if (mensaje.tipo() == TipoContenido.IMAGEN) {
            return construirArchivo(mensaje);
        }

        return null;
    }

    private File construirArchivo(MensajeProcesadoDTO mensaje) {
        if (mensaje.archivo() == null || mensaje.archivo().length == 0) {
            throw new IllegalArgumentException("La imagen no contiene datos");
        }

        try {
            String ruta = almacenar.guardarArchivo(
                    mensaje.emisor(),
                    nombreSeguro(mensaje.nombreArchivo()),
                    mensaje.archivo()
            );

            return new File(ruta);

        } catch (RuntimeException e) {
            System.err.println("No se pudo guardar la imagen original: " + e.getMessage());
            return archivoTemporal(mensaje);
        }
    }

    private File archivoTemporal(MensajeProcesadoDTO mensaje) {
        try {
            String nombre = nombreSeguro(mensaje.nombreArchivo());
            String sufijo = nombre.substring(nombre.lastIndexOf('.'));

            Path temporal = Files.createTempFile("mensaje_", sufijo);
            Files.write(temporal, mensaje.archivo());
            temporal.toFile().deleteOnExit();

            return temporal.toFile();

        } catch (IOException e) {
            throw new RuntimeException("No se pudo preparar la imagen: " + e.getMessage(), e);
        }
    }

    private String nombreSeguro(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return "imagen.png";
        }

        String nombre = nombreArchivo.toLowerCase();

        if (nombre.endsWith(".jpg")
                || nombre.endsWith(".jpeg")
                || nombre.endsWith(".png")) {
            return nombreArchivo;
        }

        return nombreArchivo + ".png";
    }

    private MensajeProcesadoDTO enriquecer(
            MensajeProcesadoDTO mensaje,
            Map<String, Object> datos) {

        String hash = valorTexto(datos.get("sha256"), mensaje.hash());
        int caracteres = valorEntero(datos.get("caracteres"), mensaje.caracteres());
        int palabras = valorEntero(datos.get("palabras"), mensaje.palabras());

        return new MensajeProcesadoDTO(
                mensaje.id(),
                mensaje.emisor(),
                mensaje.receptor(),
                mensaje.tipo(),
                mensaje.texto(),
                mensaje.archivo(),
                mensaje.nombreArchivo(),
                hash,
                caracteres,
                palabras,
                mensaje.fecha() != null ? mensaje.fecha() : LocalDateTime.now()
        );
    }

    private void guardarResultado(
            MensajeProcesadoDTO mensaje,
            Map<String, Object> datos) {

        try {
            almacenar.guardarTexto(
                    mensaje.emisor(),
                    "resultado.txt",
                    formatear(datos)
            );
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudo guardar el resultado del procesamiento: "
                            + e.getMessage()
            );
        }

        if (persistencia == null) {
            return;
        }

        try {
            persistencia.guardarMensaje(mensaje);
        } catch (RuntimeException e) {
            System.err.println("No se pudo persistir el mensaje: " + e.getMessage());
        }
    }

    private String formatear(Map<String, Object> datos) {
        StringBuilder resultado = new StringBuilder();

        for (Map.Entry<String, Object> entrada : datos.entrySet()) {
            Object valor = entrada.getValue();

            String texto = valor instanceof File archivo
                    ? archivo.getAbsolutePath()
                    : String.valueOf(valor);

            resultado.append(entrada.getKey())
                    .append(": ")
                    .append(texto)
                    .append(System.lineSeparator());
        }

        return resultado.toString();
    }

    private String valorTexto(Object valor, String porDefecto) {
        if (valor == null) {
            return porDefecto;
        }

        return String.valueOf(valor);
    }

    private int valorEntero(Object valor, int porDefecto) {
        if (valor instanceof Number numero) {
            return numero.intValue();
        }

        if (valor != null) {
            try {
                return Integer.parseInt(String.valueOf(valor));
            } catch (NumberFormatException e) {
                System.err.println("Valor numérico inválido: " + valor);
            }
        }

        return porDefecto;
    }
}
