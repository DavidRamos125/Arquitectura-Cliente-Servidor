package org.example.Mensajes;

import org.example.AlmacenarInformacion.IAlmacenarInformacion;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.UtileriasVarias.TipoContenido;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ImpMensajes implements IMensajes {

    private final IAlmacenarInformacion almacenar;
    private final ConcurrentHashMap<Long, MensajeProcesadoDTO> porId = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<MensajeProcesadoDTO> enOrden = new ConcurrentLinkedQueue<>();
    private final AtomicLong idsSinBaseDeDatos = new AtomicLong(0);

    public ImpMensajes(IAlmacenarInformacion almacenar) {
        this.almacenar = almacenar;
    }

    @Override
    public MensajeProcesadoDTO agregar(MensajeProcesadoDTO mensaje) {
        LocalDateTime fecha = mensaje.fecha() != null ? mensaje.fecha() : LocalDateTime.now();
        MensajeProcesadoDTO sinId = copiarConId(mensaje, 0, fecha);

        long id;
        try {
            id = almacenar.guardarMensaje(sinId);
        } catch (RuntimeException e) {
            id = -idsSinBaseDeDatos.incrementAndGet();
            System.err.println("No se pudo guardar el mensaje en la base de datos (id temporal "
                    + id + "): " + e.getMessage());
        }

        MensajeProcesadoDTO guardado = copiarConId(mensaje, id, fecha);
        porId.put(id, guardado);
        enOrden.add(guardado);

        try {
            guardarOriginal(guardado);
        } catch (RuntimeException e) {
            System.err.println("No se pudo guardar el archivo original del mensaje " + id
                    + ": " + e.getMessage());
        }
        return guardado;
    }

    @Override
    public List<MensajeProcesadoDTO> listarTodos() {
        return new ArrayList<>(enOrden);
    }

    @Override
    public List<MensajeProcesadoDTO> listarPorEmisor(String emisor) {
        return enOrden.stream()
                .filter(m -> m.emisor() != null && m.emisor().equals(emisor))
                .toList();
    }

    @Override
    public List<MensajeProcesadoDTO> listarPorReceptor(String receptor) {
        return enOrden.stream()
                .filter(m -> m.receptor() != null && m.receptor().equals(receptor))
                .toList();
    }

    @Override
    public Optional<MensajeProcesadoDTO> buscarPorId(long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public int cantidad() {
        return porId.size();
    }

    private static MensajeProcesadoDTO copiarConId(MensajeProcesadoDTO m, long id, LocalDateTime fecha) {
        return new MensajeProcesadoDTO(id, m.emisor(), m.receptor(), m.tipo(), m.texto(),
                m.archivo(), m.nombreArchivo(), m.hash(), m.caracteres(), m.palabras(), fecha);
    }

    private void guardarOriginal(MensajeProcesadoDTO mensaje) {
        if (mensaje.tipo() == TipoContenido.IMAGEN && mensaje.archivo() != null) {
            almacenar.guardarArchivoDeMensaje(mensaje.id(), mensaje.emisor(), "ORIGINAL",
                    mensaje.nombreArchivo(), mensaje.archivo());
        } else if (mensaje.tipo() == TipoContenido.TEXTO && mensaje.texto() != null) {
            almacenar.guardarArchivoDeMensaje(mensaje.id(), mensaje.emisor(), "ORIGINAL",
                    "mensaje.txt", mensaje.texto().getBytes(StandardCharsets.UTF_8));
        }
    }
}
