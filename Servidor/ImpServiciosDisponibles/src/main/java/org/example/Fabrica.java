package org.example;

import org.example.filtros.*;
import org.example.interfaz.Filtro;

import java.util.List;

public class Fabrica {
    private static final Filtro filtroSha256 = new FiltroSHA256();
    private static final Filtro filtroEscalaGrises = new FiltroEscalaGrises();
    private static final Filtro filtroReducirBrillo = new FiltroReducirBrillo();
    private static final Filtro filtroReducirTamano = new FiltroReducirTamano();
    private static final Filtro filtroSepia = new FiltroSepia();
    private static final Filtro filtroRotar180 = new FiltroRotar180();
    private static final Filtro filtroContPalabras = new FiltroContarPalabras();
    private static final Filtro filtroContarCaracteres = new FiltroContarCaracteres();


    public static List<Filtro> getFiltroImagen(){
        return List.of(filtroSha256,
                filtroEscalaGrises,
                filtroReducirBrillo,
                filtroReducirTamano,
                filtroSepia,
                filtroRotar180);
    }

    public static List<Filtro> getFiltroTexto(){
        return List.of(filtroSha256,
                filtroContPalabras,
                filtroContarCaracteres);
    }
}
