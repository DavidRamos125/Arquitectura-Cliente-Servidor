package org.example;

import java.util.List;
import java.util.Optional;

public interface IMensajes {
    MensajeProcesadoDTO agregar(MensajeProcesadoDTO mensaje);
    List<MensajeProcesadoDTO> listarTodos();
    List<MensajeProcesadoDTO> listarPorEmisor(String emisor);
    List<MensajeProcesadoDTO> listarPorReceptor(String receptor);
    Optional<MensajeProcesadoDTO> buscarPorId(long id);
    int cantidad();
}