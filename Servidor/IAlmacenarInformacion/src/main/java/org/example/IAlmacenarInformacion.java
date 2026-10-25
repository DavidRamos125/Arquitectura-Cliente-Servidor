package org.example;

import java.util.List;

public interface IAlmacenarInformacion {
    String guardarArchivo(String usuario, String nombreArchivo, byte[] contenido);
    String guardarTexto(String usuario, String nombreArchivo, String texto);
    byte[] leerArchivo(String ruta);

    void guardarMensaje(MensajeProcesadoDTO mensaje);
    void guardarLog(LogEventoDTO log);
    List<MensajeProcesadoDTO> listarMensajes();
    List<LogEventoDTO> listarLogs();
}