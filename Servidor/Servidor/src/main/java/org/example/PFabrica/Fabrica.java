package org.example.PFabrica;


import org.example.*;
import org.example.ConexionesClientes.ConexionCliente;
import org.example.ConexionesClientes.GestorConexiones;
import org.example.Procesamiento.aplicarServicios;

public class Fabrica {


    public static GestorConexiones getGestorConexiones() {
        return new GestorConexiones();
    }

    public static IClienteServidor getClienteServidor() {
        return new ImpClienteServidor();
    }

    public static ConexionCliente getConexionCliente(String pendiente, IConexion conexion) {
        return new ConexionCliente(pendiente, conexion);
    }

    public static IUsuariosDisponibles getUsuariosDisponibles() {
        return new ImpUsuariosDisponibles();
    }

    public static IServiciosDisponibles getServiciosDisponibles() {
        return new ImpServiciosDisponibles();
    }

    public static aplicarServicios getAplicarServicios() {
        return new aplicarServicios();
    }
}
