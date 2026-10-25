package org.example;

import java.util.List;

public interface IPersistencia {
    void guardarMensaje(MensajeProcesadoDTO mensaje);
    void guardarLog(LogEventoDTO log);
    List<MensajeProcesadoDTO> listarMensajes();
    List<LogEventoDTO> listarLogs();
}