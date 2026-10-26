package org.example.AlmacenarInformacion;

import org.example.DTORecords.ArchivoDTO;
import org.example.DTORecords.LogEventoDTO;
import org.example.DTORecords.MensajeProcesadoDTO;

import java.util.List;

public interface IAlmacenarInformacion {
    String guardarArchivo(String usuario, String nombreArchivo, byte[] contenido);
    String guardarTexto(String usuario, String nombreArchivo, String texto);
    byte[] leerArchivo(String ruta);

    ArchivoDTO guardarArchivoDeMensaje(long mensajeId, String usuario, String variante,
                                       String nombreArchivo, byte[] contenido);
    List<ArchivoDTO> listarArchivosDeMensaje(long mensajeId);
    List<ArchivoDTO> listarArchivos();

    long guardarMensaje(MensajeProcesadoDTO mensaje);
    void guardarLog(LogEventoDTO log);
    List<MensajeProcesadoDTO> listarMensajes();
    List<LogEventoDTO> listarLogs();
}
