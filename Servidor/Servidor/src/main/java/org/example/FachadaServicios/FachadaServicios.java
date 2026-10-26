package org.example.FachadaServicios;

import org.example.AlmacenarInformacion.IAlmacenarInformacion;
import org.example.AlmacenarInformacion.ImpAlmacenarInformacion;
import org.example.ConexionesClientes.GestorComunicaciones;
import org.example.DTORecords.LogEventoDTO;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.Fachada.Factory;
import org.example.LogDeEventos.ILogDeEventos;
import org.example.LogDeEventos.ImpLogDeEventos;
import org.example.Mensajes.IMensajes;
import org.example.Mensajes.ImpMensajes;
import org.example.Persistencia.IPersistencia;
import org.example.Persistencia.PersistenciaMemoria;
import org.example.RepositorioTextoPlano;
import org.example.UsuarioDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FachadaServicios {

    private final IAlmacenarInformacion almacenar;
    private final IMensajes mensajes;
    private final ILogDeEventos logDeEventos;

    private GestorComunicaciones gestorComunicaciones;

    public FachadaServicios() {
        this.almacenar = new ImpAlmacenarInformacion(obtenerPersistencia());
        this.mensajes = new ImpMensajes(almacenar);
        this.logDeEventos = new ImpLogDeEventos(almacenar);
    }

    private IPersistencia obtenerPersistencia() {
        try {
            return Factory.getPersistenciaImp();
        } catch (RuntimeException e) {
            System.err.println(
                    "Persistencia no disponible, usando memoria: "
                            + e.getMessage()
            );
            return PersistenciaMemoria.getInstance();
        }
    }

    public List<MensajeProcesadoDTO> getMensajesTodos() {
        return mensajes.listarTodos();
    }

    public List<MensajeProcesadoDTO> getMensajesPersistidos() {
        try {
            return almacenar.listarMensajes();
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudieron leer los mensajes persistidos: "
                            + e.getMessage()
            );
            return mensajes.listarTodos();
        }
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

    public MensajeProcesadoDTO sendMensaje(MensajeProcesadoDTO mensaje) {
        return mensajes.agregar(mensaje);
    }

    public int getMensajesCantidad() {
        return mensajes.cantidad();
    }

    public List<LogEventoDTO> getLogDeEventos() {
        return logDeEventos.listar();
    }

    public List<LogEventoDTO> getLogsPersistidos() {
        try {
            return almacenar.listarLogs();
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudieron leer los logs persistidos: "
                            + e.getMessage()
            );
            return List.of();
        }
    }

    public void registrarLog(String evento, String emisor, String receptor, String detalle) {
        logDeEventos.registrar(evento, emisor, receptor, detalle);
    }

    public void registrarConexionLog(String usuario, String detalle) {
        logDeEventos.registrarConexion(usuario, detalle);
    }

    public void registrarDesconexionLog(String usuario, String detalle) {
        logDeEventos.registrarDesconexion(usuario, detalle);
    }

    public void registrarMensajeEnviadoLog(String emisor, String receptor, String detalle) {
        logDeEventos.registrarMensajeEnviado(emisor, receptor, detalle);
    }

    public List<UsuarioDTO> getUsuarios() {
        try {
            return new RepositorioTextoPlano().getUsuariosDisponibles();
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudieron cargar los usuarios: "
                            + e.getMessage()
            );
            return List.of();
        }
    }

    public synchronized void iniciarServidor() {
        if (gestorComunicaciones == null) {
            gestorComunicaciones = new GestorComunicaciones();
        }

        gestorComunicaciones.iniciar();
    }

    public synchronized void detenerServidor() {
        if (gestorComunicaciones != null) {
            gestorComunicaciones.detener();
        }
    }

    public boolean servidorActivo() {
        return gestorComunicaciones != null
                && gestorComunicaciones.estaEjecutando();
    }

    public Set<String> getUsuariosConectados() {
        return gestorComunicaciones == null
                ? Set.of()
                : gestorComunicaciones.usuariosConectados();
    }

    public int getConexionesCantidad() {
        return gestorComunicaciones == null
                ? 0
                : gestorComunicaciones.contarConexiones();
    }

    public void desconectarUsuario(String usuario) {
        if (gestorComunicaciones != null) {
            gestorComunicaciones.desconectarUsuario(usuario);
        }
    }
}
