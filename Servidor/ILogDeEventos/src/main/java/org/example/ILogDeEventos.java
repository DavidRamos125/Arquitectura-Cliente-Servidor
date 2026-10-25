package org.example;

import java.util.List;

public interface ILogDeEventos {

    String MENSAJE_ENVIADO = "MENSAJE_ENVIADO";
    String CONEXION = "CONEXION";
    String DESCONEXION = "DESCONEXION";

    void registrar(String evento, String emisor, String receptor, String detalle);
    void registrarMensajeEnviado(String emisor, String receptor, String detalle);
    void registrarConexion(String usuario, String detalle);
    void registrarDesconexion(String usuario, String detalle);

    List<LogEventoDTO> listar();
}