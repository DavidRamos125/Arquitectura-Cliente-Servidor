package org.example.AlmacenarInformacion;

import org.example.DTORecords.ArchivoDTO;
import org.example.DTORecords.LogEventoDTO;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.Persistencia.IPersistencia;
import org.example.UtileriasVarias.UtilAlmacenamiento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ImpAlmacenarInformacion implements IAlmacenarInformacion {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final IPersistencia persistencia;
    private final Path directorioBase;

    public ImpAlmacenarInformacion(IPersistencia persistencia) {
        this.persistencia = persistencia;
        this.directorioBase = Path.of(UtilAlmacenamiento.getProperty("almacen.ruta"))
                .toAbsolutePath().normalize();
    }

    @Override
    public String guardarArchivo(String usuario, String nombreArchivo, byte[] contenido) {
        try {
            Path carpetaUsuario = directorioBase.resolve(limpiar(usuario));
            Files.createDirectories(carpetaUsuario);
            String nombre = LocalDateTime.now().format(FORMATO_FECHA) + "_" + limpiar(nombreArchivo);
            Path destino = carpetaUsuario.resolve(nombre);
            Files.write(destino, contenido);
            return destino.toString();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public String guardarTexto(String usuario, String nombreArchivo, String texto) {
        return guardarArchivo(usuario, nombreArchivo, texto.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] leerArchivo(String ruta) {
        try {
            Path archivo = Path.of(ruta).toAbsolutePath().normalize();
            if (!archivo.startsWith(directorioBase)) {
                throw new IllegalArgumentException("La ruta está fuera del directorio de almacenamiento");
            }
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public ArchivoDTO guardarArchivoDeMensaje(long mensajeId, String usuario, String variante,
                                              String nombreArchivo, byte[] contenido) {
        String ruta = guardarArchivo(usuario, nombreArchivo, contenido);
        String nombreGuardado = Path.of(ruta).getFileName().toString();
        LocalDateTime fecha = LocalDateTime.now();
        ArchivoDTO sinId = new ArchivoDTO(0, mensajeId, variante, nombreGuardado, ruta, fecha);
        long id = persistencia.registrarArchivo(sinId);
        return new ArchivoDTO(id, mensajeId, variante, nombreGuardado, ruta, fecha);
    }

    @Override
    public List<ArchivoDTO> listarArchivosDeMensaje(long mensajeId) {
        return persistencia.listarArchivosPorMensaje(mensajeId);
    }

    @Override
    public List<ArchivoDTO> listarArchivos() {
        return persistencia.listarArchivos();
    }

    @Override
    public long guardarMensaje(MensajeProcesadoDTO mensaje) {
        return persistencia.guardarMensaje(mensaje);
    }

    @Override
    public void guardarLog(LogEventoDTO log) {
        persistencia.guardarLog(log);
    }

    @Override
    public List<MensajeProcesadoDTO> listarMensajes() {
        return persistencia.listarMensajes();
    }

    @Override
    public List<LogEventoDTO> listarLogs() {
        return persistencia.listarLogs();
    }

    private static String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "sin_nombre";
        }
        String soloNombre = valor.substring(Math.max(valor.lastIndexOf('/'), valor.lastIndexOf('\\')) + 1);
        String limpio = soloNombre.replaceAll("[^A-Za-z0-9._-]", "_");
        if (limpio.length() > 150) {
            limpio = limpio.substring(limpio.length() - 150);
        }
        if (limpio.isEmpty() || limpio.matches("\\.+")) {
            return "sin_nombre";
        }
        return limpio;
    }
}
