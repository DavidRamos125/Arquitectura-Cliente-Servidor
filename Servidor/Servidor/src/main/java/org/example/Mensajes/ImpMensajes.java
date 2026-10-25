package org.example.Mensajes;

import org.example.AlmacenarInformacion.IAlmacenarInformacion;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.UtileriasVarias.TipoContenido;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class ImpMensajes implements IMensajes {

    private final IAlmacenarInformacion almacenar;
    private final ConcurrentSkipListMap<Long, MensajeProcesadoDTO> mensajes = new ConcurrentSkipListMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public ImpMensajes(IAlmacenarInformacion almacenar) {
        this.almacenar = almacenar;
    }

    @Override
    public MensajeProcesadoDTO agregar(MensajeProcesadoDTO mensaje) {
        MensajeProcesadoDTO guardado = new MensajeProcesadoDTO(
                secuencia.incrementAndGet(),
                mensaje.emisor(),
                mensaje.receptor(),
                mensaje.tipo(),
                mensaje.texto(),
                mensaje.archivo(),
                mensaje.nombreArchivo(),
                mensaje.hash(),
                mensaje.caracteres(),
                mensaje.palabras(),
                mensaje.fecha() != null ? mensaje.fecha() : LocalDateTime.now());

        mensajes.put(guardado.id(), guardado);

        try {
            guardarOriginal(guardado);
            almacenar.guardarMensaje(guardado);
        } catch (RuntimeException e) {
            System.err.println("No se pudo almacenar el mensaje " + guardado.id() + ": " + e.getMessage());
        }
        return guardado;
    }

    @Override
    public List<MensajeProcesadoDTO> listarTodos() {
        return new ArrayList<>(mensajes.values());
    }

    @Override
    public List<MensajeProcesadoDTO> listarPorEmisor(String emisor) {
        return mensajes.values().stream()
                .filter(m -> m.emisor() != null && m.emisor().equals(emisor))
                .toList();
    }

    @Override
    public List<MensajeProcesadoDTO> listarPorReceptor(String receptor) {
        return mensajes.values().stream()
                .filter(m -> m.receptor() != null && m.receptor().equals(receptor))
                .toList();
    }

    @Override
    public Optional<MensajeProcesadoDTO> buscarPorId(long id) {
        return Optional.ofNullable(mensajes.get(id));
    }

    @Override
    public int cantidad() {
        return mensajes.size();
    }

    private void guardarOriginal(MensajeProcesadoDTO mensaje) {
        if (mensaje.tipo() == TipoContenido.IMAGEN && mensaje.archivo() != null) {
            almacenar.guardarArchivo(mensaje.emisor(), mensaje.nombreArchivo(), mensaje.archivo());
        } else if (mensaje.tipo() == TipoContenido.TEXTO && mensaje.texto() != null) {
            almacenar.guardarTexto(mensaje.emisor(), "mensaje.txt", mensaje.texto());
        }
    }
}