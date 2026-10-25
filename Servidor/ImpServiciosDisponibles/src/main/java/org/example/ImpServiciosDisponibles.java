package org.example;

import org.example.interfaz.Filtro;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImpServiciosDisponibles implements IServiciosDisponibles {
    private final List<Filtro> filtroImagen;
    private final List<Filtro> filtroTexto;

    public ImpServiciosDisponibles(){
        filtroImagen = Fabrica.getFiltroImagen();
        filtroTexto = Fabrica.getFiltroTexto();
    }

    @Override
    public Map<String, Object> procesar(Object o) {
        Map<String, Object> map = new HashMap<>();
        if(o instanceof File){
            for(Filtro filtro : filtroImagen){
                map.putIfAbsent(filtro.getNombre(), filtro.procesar(o));
            }
            return map;
        }else if(o instanceof String){
            for(Filtro filtro : filtroTexto){
                map.putIfAbsent(filtro.getNombre(), filtro.procesar(o));
            }
            return map;
        }
        System.err.println("tipo de datos invalido (requiere String o File)");
        return null;
    }
}
