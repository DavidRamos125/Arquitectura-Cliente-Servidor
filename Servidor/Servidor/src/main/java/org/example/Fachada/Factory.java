package org.example.Fachada;

import org.example.AlmacenarInformacion.IAlmacenarInformacion;
import org.example.AlmacenarInformacion.ImpAlmacenarInformacion;
import org.example.LogDeEventos.ILogDeEventos;
import org.example.LogDeEventos.ImpLogDeEventos;
import org.example.Mensajes.IMensajes;
import org.example.Mensajes.ImpMensajes;
import org.example.Persistencia.IPersistencia;
import org.example.Persistencia.ImpPersistencia;

public class Factory {
    public static IPersistencia getPersistenciaImp(){
        return new ImpPersistencia();
    }

    public static IAlmacenarInformacion getAlmacenarInformacionImp(){
        return new ImpAlmacenarInformacion(getPersistenciaImp());
    }

    public static IMensajes getMensajesImp(){
        return new ImpMensajes(getAlmacenarInformacionImp());
    }

    public static ILogDeEventos getLogDeEventosImp(){
        return new ImpLogDeEventos(getAlmacenarInformacionImp());
    }

}
