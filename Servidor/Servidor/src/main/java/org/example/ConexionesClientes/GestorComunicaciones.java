package org.example.ConexionesClientes;
import org.example.*;
import org.example.AlmacenarInformacion.ImpAlmacenarInformacion;
import org.example.Fachada.Factory;
import org.example.LogDeEventos.ILogDeEventos;
import org.example.LogDeEventos.ImpLogDeEventos;
import org.example.PFabrica.Fabrica;
import org.example.Persistencia.PersistenciaMemoria;
import org.example.Procesamiento.ProcesarMensaje;
import org.example.Procesamiento.UtilMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GestorComunicaciones {

    private static final String LOGIN = "LOGIN";
    private static final String LOGOUT = "LOGOUT";
    private static final String TEXTO = "TEXTO";
    private static final String IMAGEN_INICIO = "IMG_INICIO";
    private static final String IMAGEN_PARTE = "IMG_PARTE";
    private static final String IMAGEN_FIN = "IMG_FIN";
    private static final String OK = "OK";
    private static final String ERROR = "ERROR";

    private final IProtocoloComunicacion protocoloComunicacion;
    private final GestorConexiones gestorConexiones;
    private final IClienteServidor clienteServidor;
    private final IUsuariosDisponibles usuariosDisponibles;
    private final ProcesarMensaje procesarMensaje;

    private ILogDeEventos logDeEventos;
    private Thread hiloEscucha;
    private volatile boolean ejecutando;

    public GestorComunicaciones() {
        this.protocoloComunicacion = ProtocoloFactory.getProtocoloComunicacion();
        this.gestorConexiones = Fabrica.getGestorConexiones();
        this.clienteServidor = Fabrica.getClienteServidor();
        this.usuariosDisponibles = Fabrica.getUsuariosDisponibles();
        this.procesarMensaje = new ProcesarMensaje();
    }

    public synchronized void iniciar() {
        if (ejecutando) {
            return;
        }

        protocoloComunicacion.iniciar();
        ejecutando = true;

        hiloEscucha = new Thread(
                this::esperarConexiones,
                "Hilo-Escucha"
        );

        hiloEscucha.start();

        System.out.println("Servidor iniciado.");
    }

    private void esperarConexiones() {
        while (ejecutando) {
            IConexion conexion = null;

            try {
                conexion = protocoloComunicacion.escuchar();

                if (conexion == null) {
                    continue;
                }

                ConexionCliente cliente = Fabrica.getConexionCliente("PENDIENTE", conexion);

                if (!gestorConexiones.registrar(cliente)) {
                    conexion.desconectar();
                    continue;
                }

                System.out.println("Nueva conexión: " + cliente.getIdConexion());

                Thread hiloCliente = new Thread(
                        () -> atenderCliente(cliente),
                        "Cliente-" + cliente.getIdConexion()
                );

                hiloCliente.start();

            } catch (IllegalStateException e) {
                if (ejecutando) {
                    System.err.println(
                            "Error aceptando conexiones: "
                                    + e.getMessage()
                    );
                }

                break;

            } catch (IOException e) {
                cerrarConexionNoRegistrada(conexion);

                if (ejecutando) {
                    System.err.println(
                            "Error al aceptar la conexión: "
                                    + e.getMessage()
                    );
                }
            }
        }
    }

    private void atenderCliente(ConexionCliente cliente) {
        try {
            if (!autenticar(cliente)) {
                return;
            }

            Map<Long, ImagenPendiente> imagenes = new HashMap<>();

            while (ejecutando && cliente.estaActiva()) {
                Paquete paquete = clienteServidor.obtener(cliente.recibirDatos());

                if (LOGOUT.equals(paquete.tipo())) {
                    responder(cliente, OK, "LOGOUT");
                    break;
                }

                String error = validar(paquete);

                if (error != null) {
                    responder(cliente, ERROR, error);
                    continue;
                }

                reenviar(cliente, paquete);
                procesarPaquete(cliente, paquete, imagenes);
            }

        } catch (IOException e) {
            System.out.println(
                    "Cliente desconectado: "
                            + cliente.getIdConexion()
            );

        } catch (IllegalArgumentException e) {
            System.err.println(
                    "Paquete inválido del cliente "
                            + cliente.getIdConexion()
                            + ": " + e.getMessage()
            );

        } catch (RuntimeException e) {
            System.err.println(
                    "Error atendiendo al cliente "
                            + cliente.getIdConexion()
                            + ": " + e.getMessage()
            );

        } finally {
            try {
                gestorConexiones.cerrarConexion(
                        cliente.getIdConexion()
                );
            } catch (RuntimeException e) {
                System.err.println(
                        "Error cerrando la conexión: "
                                + e.getMessage()
                );
            }

            registrarDesconexion(
                    cliente.getCodigoUsuario(),
                    "Conexión finalizada"
            );

            System.out.println(
                    "Conexión finalizada: "
                            + cliente.getIdConexion()
            );
        }
    }

    private boolean autenticar(ConexionCliente cliente) throws IOException {
        Paquete paquete = clienteServidor.obtener(cliente.recibirDatos());

        if (!LOGIN.equals(paquete.tipo())) {
            responder(cliente, ERROR, "LOGIN_REQUERIDO");
            return false;
        }

        String usuario = paquete.emisor();
        String contrasena = new String(paquete.datos(), StandardCharsets.UTF_8);

        UsuarioDTO autenticado = usuariosDisponibles.comprobarUsuario(usuario, contrasena);

        if (autenticado == null) {
            responder(cliente, ERROR, "CREDENCIALES_INVALIDAS");
            return false;
        }

        cliente.establecerUsuarioAutenticado(autenticado.codigo());
        responder(cliente, OK, "LOGIN");
        registrarConexion(autenticado.codigo(), "Autenticado");

        System.out.println("Usuario autenticado: " + autenticado.codigo());

        return true;
    }

    private ILogDeEventos obtenerLogDeEventos() {
        if (logDeEventos == null) {
            try {
                logDeEventos = Factory.getLogDeEventosImp();
            } catch (RuntimeException e) {
                System.err.println(
                        "Logs de eventos no disponibles, usando memoria: "
                                + e.getMessage()
                );
                logDeEventos = new ImpLogDeEventos(
                        new ImpAlmacenarInformacion(
                                PersistenciaMemoria.getInstance()
                        )
                );
            }
        }

        return logDeEventos;
    }

    private void registrarConexion(String usuario, String detalle) {
        ILogDeEventos logs = obtenerLogDeEventos();

        if (logs == null) {
            return;
        }

        try {
            logs.registrarConexion(usuario, detalle);
        } catch (RuntimeException e) {
            System.err.println("No se pudo registrar la conexión: " + e.getMessage());
        }
    }

    private void registrarDesconexion(String usuario, String detalle) {
        ILogDeEventos logs = obtenerLogDeEventos();

        if (logs == null) {
            return;
        }

        try {
            logs.registrarDesconexion(usuario, detalle);
        } catch (RuntimeException e) {
            System.err.println("No se pudo registrar la desconexión: " + e.getMessage());
        }
    }

    private void registrarMensajeEnviado(String emisor, String receptor, String detalle) {
        ILogDeEventos logs = obtenerLogDeEventos();

        if (logs == null) {
            return;
        }

        try {
            logs.registrarMensajeEnviado(emisor, receptor, detalle);
        } catch (RuntimeException e) {
            System.err.println("No se pudo registrar el mensaje: " + e.getMessage());
        }
    }

    private void procesarPaquete(
            ConexionCliente cliente,
            Paquete paquete,
            Map<Long, ImagenPendiente> imagenes) {

        try {
            if (TEXTO.equals(paquete.tipo())) {
                Paquete normalizado = new Paquete(
                        paquete.tipo(),
                        cliente.getCodigoUsuario(),
                        paquete.receptor(),
                        paquete.id(),
                        paquete.parte(),
                        paquete.total(),
                        paquete.nombreArchivo(),
                        paquete.hash(),
                        paquete.datos()
                );

                procesarMensaje.procesarMensaje(
                        UtilMapper.paqueteToMensajeProcesadoDTO(normalizado)
                );
                return;
            }

            if (IMAGEN_INICIO.equals(paquete.tipo())) {
                ImagenPendiente imagen = new ImagenPendiente(
                        paquete.nombreArchivo(),
                        paquete.total(),
                        paquete.hash()
                );

                if (paquete.datos().length > 0) {
                    imagen.agregar(paquete.parte(), paquete.datos());
                }

                imagenes.put(paquete.id(), imagen);
                return;
            }

            if (IMAGEN_PARTE.equals(paquete.tipo())) {
                ImagenPendiente imagen = imagenes.get(paquete.id());

                if (imagen == null) {
                    System.err.println(
                            "Fragmento sin inicio para la imagen "
                                    + paquete.id()
                    );
                    return;
                }

                imagen.agregar(paquete.parte(), paquete.datos());
                return;
            }

            if (IMAGEN_FIN.equals(paquete.tipo())) {
                ImagenPendiente imagen = imagenes.remove(paquete.id());

                if (imagen == null) {
                    System.err.println(
                            "Fin sin inicio para la imagen "
                                    + paquete.id()
                    );
                    return;
                }

                if (paquete.datos().length > 0) {
                    imagen.agregar(paquete.parte(), paquete.datos());
                }

                if (imagen.faltanPartes()) {
                    System.err.println(
                            "Imagen incompleta "
                                    + paquete.id()
                                    + ": se recibieron "
                                    + imagen.cantidadPartes()
                                    + " de "
                                    + imagen.total()
                    );
                    return;
                }

                Paquete completa = new Paquete(
                        IMAGEN_FIN,
                        cliente.getCodigoUsuario(),
                        paquete.receptor(),
                        paquete.id(),
                        1,
                        1,
                        imagen.nombreArchivo(),
                        imagen.hash(),
                        imagen.unir()
                );

                procesarMensaje.procesarMensaje(
                        UtilMapper.paqueteToMensajeProcesadoDTO(completa)
                );
            }

        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudo procesar el paquete "
                            + paquete.tipo()
                            + ": " + e.getMessage()
            );
        }
    }

    private String validar(Paquete paquete) {
        String tipo = paquete.tipo();

        if (TEXTO.equals(tipo)) {
            if (paquete.receptor().isBlank()) {
                return "RECEPTOR_VACIO";
            }
            if (paquete.datos().length == 0) {
                return "MENSAJE_VACIO";
            }
            return null;
        }

        if (IMAGEN_INICIO.equals(tipo)) {
            if (paquete.receptor().isBlank()) {
                return "RECEPTOR_VACIO";
            }
            if (paquete.id() == 0) {
                return "ID_INVALIDO";
            }
            if (paquete.total() <= 0) {
                return "TOTAL_INVALIDO";
            }
            if (paquete.nombreArchivo().isBlank()) {
                return "NOMBRE_INVALIDO";
            }
            if (!formatoImagenValido(paquete.nombreArchivo())) {
                return "FORMATO_INVALIDO";
            }
            return null;
        }

        if (IMAGEN_PARTE.equals(tipo)) {
            if (paquete.receptor().isBlank()) {
                return "RECEPTOR_VACIO";
            }
            if (paquete.id() == 0) {
                return "ID_INVALIDO";
            }
            if (paquete.total() <= 0) {
                return "TOTAL_INVALIDO";
            }
            if (paquete.parte() < 1 || paquete.parte() > paquete.total()) {
                return "PARTE_INVALIDA";
            }
            if (paquete.datos().length == 0) {
                return "FRAGMENTO_VACIO";
            }
            return null;
        }

        if (IMAGEN_FIN.equals(tipo)) {
            if (paquete.receptor().isBlank()) {
                return "RECEPTOR_VACIO";
            }
            if (paquete.id() == 0) {
                return "ID_INVALIDO";
            }
            return null;
        }

        return "TIPO_NO_VALIDO";
    }

    private boolean formatoImagenValido(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();

        return nombre.endsWith(".jpg")
                || nombre.endsWith(".jpeg")
                || nombre.endsWith(".png");
    }

    private void reenviar(ConexionCliente origen, Paquete paquete) {
        List<ConexionCliente> destinos = gestorConexiones.conexionesDe(paquete.receptor());

        if (destinos.isEmpty()) {
            responder(origen, ERROR, "USUARIO_NO_CONECTADO");
            return;
        }

        Paquete salida = new Paquete(
                paquete.tipo(),
                origen.getCodigoUsuario(),
                paquete.receptor(),
                paquete.id(),
                paquete.parte(),
                paquete.total(),
                paquete.nombreArchivo(),
                paquete.hash(),
                paquete.datos()
        );

        byte[] datos = clienteServidor.construir(salida);

        for (ConexionCliente destino : destinos) {
            try {
                destino.enviarDatos(datos);
            } catch (IOException e) {
                System.err.println(
                        "No se pudo entregar a "
                                + destino.getCodigoUsuario()
                                + ": " + e.getMessage()
                );
            }
        }

        if (TEXTO.equals(paquete.tipo()) || IMAGEN_FIN.equals(paquete.tipo())) {
            registrarMensajeEnviado(
                    origen.getCodigoUsuario(),
                    paquete.receptor(),
                    paquete.tipo()
            );
            responder(origen, OK, "ENVIADO");
        }
    }

    private void responder(
            ConexionCliente cliente,
            String tipo,
            String detalle) {

        Paquete respuesta = new Paquete(
                tipo,
                "SERVIDOR",
                cliente.getCodigoUsuario(),
                0,
                0,
                0,
                "",
                "",
                detalle.getBytes(StandardCharsets.UTF_8)
        );

        try {
            cliente.enviarDatos(clienteServidor.construir(respuesta));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(
                    "No se pudo enviar la respuesta al cliente "
                            + cliente.getIdConexion()
                            + ": " + e.getMessage()
            );
        }
    }

    private void cerrarConexionNoRegistrada(IConexion conexion) {
        if (conexion == null) {
            return;
        }

        try {
            conexion.desconectar();
        } catch (IOException e) {
            System.err.println(
                    "Error cerrando una conexión no registrada: "
                            + e.getMessage()
            );
        }
    }

    public boolean estaEjecutando() {
        return ejecutando;
    }

    public Set<String> usuariosConectados() {
        return gestorConexiones.listarUsuariosConectados();
    }

    public int contarConexiones() {
        return gestorConexiones.contarConexiones();
    }

    public void desconectarUsuario(String codigoUsuario) {
        gestorConexiones.cerrarConexionesUsuario(codigoUsuario);
    }

    public synchronized void detener() {
        if (!ejecutando) {
            return;
        }

        ejecutando = false;

        protocoloComunicacion.detener();

        gestorConexiones.cerrarTodas();

        System.out.println("Servidor detenido.");
    }

    private static final class ImagenPendiente {

        private final String nombreArchivo;
        private final int total;
        private final String hash;
        private final Map<Integer, byte[]> partes = new TreeMap<>();

        private ImagenPendiente(String nombreArchivo, int total, String hash) {
            this.nombreArchivo = nombreArchivo;
            this.total = total;
            this.hash = hash;
        }

        private void agregar(int parte, byte[] datos) {
            if (datos == null || datos.length == 0) {
                return;
            }

            partes.put(parte, datos);
        }

        private boolean faltanPartes() {
            return partes.size() < total;
        }

        private int cantidadPartes() {
            return partes.size();
        }

        private int total() {
            return total;
        }

        private String nombreArchivo() {
            return nombreArchivo;
        }

        private String hash() {
            return hash;
        }

        private byte[] unir() {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();

            for (byte[] parte : partes.values()) {
                salida.writeBytes(parte);
            }

            return salida.toByteArray();
        }
    }
}
