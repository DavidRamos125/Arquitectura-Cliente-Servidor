package org.example.FachadaServicios;

import org.example.AlmacenarInformacion.ImpAlmacenarInformacion;
import org.example.DTORecords.LogEventoDTO;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.Fachada.Factory;
import org.example.IClienteServidor;
import org.example.IProtocoloComunicacion;
import org.example.IServiciosDisponibles;
import org.example.IUsuariosDisponibles;
import org.example.LogDeEventos.ILogDeEventos;
import org.example.LogDeEventos.ImpLogDeEventos;
import org.example.Mensajes.IMensajes;
import org.example.Mensajes.ImpMensajes;
import org.example.Persistencia.ImpPersistencia;

import java.util.List;
import java.util.Optional;

public class FachadaServicios {

    private IClienteServidor clienteServidor;
    private IProtocoloComunicacion protocoloComunicacion;
    private IServiciosDisponibles serviciosDisponibles;
    private IUsuariosDisponibles usuariosDisponibles;
    private IMensajes mensajes;
    private ILogDeEventos logDeEventos;
    //private ConexionesClientes conexionesClientes;

    public FachadaServicios(){
        this.mensajes = Factory.getMensajesImp();
        this.logDeEventos = Factory.getLogDeEventosImp();
    }

    public List<MensajeProcesadoDTO> getMensajesTodos() {
        return mensajes.listarTodos();
    }

    public List<MensajeProcesadoDTO> getMensajesPorEmisor(String emisor) {
        return mensajes.listarPorEmisor(emisor);
    }

    public List<MensajeProcesadoDTO> getMensajesPorReceptor(String receptor) {
        return mensajes.listarPorReceptor(receptor);
    }

    public Optional<MensajeProcesadoDTO> getMensajePorId(long id) {
        return mensajes.buscarPorId(id);
    }

    public void sendMensaje(MensajeProcesadoDTO mensaje) {
        mensajes.agregar(mensaje);
    }

    public int getMensajesCantidad() {
        return mensajes.cantidad();
    }

    public List<LogEventoDTO> getLogDeEventos() {
        return logDeEventos.listar();
    }

    public void registrarLog(String evento, String emisor, String receptor, String detalle) {
        logDeEventos.registrar(evento,emisor,receptor,detalle);
    }

    public void registrarConexionLog(String usuario, String detalle) {
        logDeEventos.registrarConexion(usuario,detalle);
    }

    public void registrarDesconexionLog(String usuario, String detalle) {
        logDeEventos.registrarDesconexion(usuario,detalle);
    }

    public void registrarMensajeEnviadoLog(String emisor, String receptor, String detalle) {
        logDeEventos.registrarMensajeEnviado(emisor,receptor,detalle);
    }
}
