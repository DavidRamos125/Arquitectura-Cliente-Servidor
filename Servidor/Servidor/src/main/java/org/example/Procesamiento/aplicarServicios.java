package org.example.Procesamiento;

import org.example.PFabrica.Fabrica;
import org.example.IServiciosDisponibles;

import java.util.Map;


public class aplicarServicios {
    private final IServiciosDisponibles serviciosDisponibles;

    public aplicarServicios() {
        serviciosDisponibles = Fabrica.getServiciosDisponibles();
    }

    public Map<String, Object> procesar(Object datos) {
        return serviciosDisponibles.procesar(datos);
    }

}
