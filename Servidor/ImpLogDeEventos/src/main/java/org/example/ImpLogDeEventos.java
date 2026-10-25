package org.example;

import java.time.LocalDateTime;
import java.util.List;

public class ImpLogDeEventos implements ILogDeEventos {

    private final IAlmacenarInformacion almacenar;

    public ImpLogDeEventos(IAlmacenarInformacion almacenar) {
        this.almacenar = almacenar;
    }

    @Override
    public void registrar(String evento, String emisor, String receptor, String detalle) {
        LogEventoDTO log = new LogEventoDTO(0, LocalDateTime.now(), evento, emisor, receptor, detalle);
        try {
            almacenar.guardarLog(log);
        } catch (RuntimeException e) {
            System.err.println("No se pudo registrar el log (" + evento + "): " + e.getMessage());
        }
    }

    @Override
    public void registrarMensajeEnviado(String emisor, String receptor, String detalle) {
        registrar(MENSAJE_ENVIADO, emisor, receptor, detalle);
    }

    @Override
    public void registrarConexion(String usuario, String detalle) {
        registrar(CONEXION, usuario, null, detalle);
    }

    @Override
    public void registrarDesconexion(String usuario, String detalle) {
        registrar(DESCONEXION, usuario, null, detalle);
    }

    @Override
    public List<LogEventoDTO> listar() {
        return almacenar.listarLogs();
    }
}