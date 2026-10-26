package org.example.ConexionesClientes;
import org.example.*;
import org.example.Fabrica.Fabrica;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    private Thread hiloEscucha;
    private volatile boolean ejecutando;

    public GestorComunicaciones() {
        this.protocoloComunicacion = ProtocoloFactory.getProtocoloComunicacion();
        this.gestorConexiones = Fabrica.getGestorConexiones();
        this.clienteServidor = Fabrica.getClienteServidor();
        this.usuariosDisponibles = Fabrica.getUsuariosDisponibles();
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

        System.out.println("Usuario autenticado: " + autenticado.codigo());

        return true;
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

    public synchronized void detener() {
        if (!ejecutando) {
            return;
        }

        ejecutando = false;

        protocoloComunicacion.detener();

        gestorConexiones.cerrarTodas();

        System.out.println("Servidor detenido.");
    }
}
