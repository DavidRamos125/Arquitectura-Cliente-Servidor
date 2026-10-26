package org.example.Persistencia;

import org.example.DTORecords.ArchivoDTO;
import org.example.DTORecords.LogEventoDTO;
import org.example.DTORecords.MensajeProcesadoDTO;

import java.util.List;

public interface IPersistencia {
    long guardarMensaje(MensajeProcesadoDTO mensaje);
    void guardarLog(LogEventoDTO log);
    List<MensajeProcesadoDTO> listarMensajes();
    List<LogEventoDTO> listarLogs();

    long registrarArchivo(ArchivoDTO archivo);
    List<ArchivoDTO> listarArchivos();
    List<ArchivoDTO> listarArchivosPorMensaje(long mensajeId);
}
