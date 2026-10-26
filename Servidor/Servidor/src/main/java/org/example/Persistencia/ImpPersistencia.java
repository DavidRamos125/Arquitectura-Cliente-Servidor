package org.example.Persistencia;

import org.example.DTORecords.ArchivoDTO;
import org.example.DTORecords.LogEventoDTO;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.UtileriasVarias.TipoContenido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ImpPersistencia implements IPersistencia {

    private final ConexionMySQL conexion;

    public ImpPersistencia() {
        this.conexion = new ConexionMySQL();
    }

    @Override
    public long guardarMensaje(MensajeProcesadoDTO mensaje) {
        String sql = "INSERT INTO mensajes (emisor, receptor, tipo, texto, nombre_archivo, hash, "
                + "caracteres, palabras, fecha) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mensaje.emisor());
            ps.setString(2, mensaje.receptor());
            ps.setString(3, mensaje.tipo().name());
            ps.setString(4, mensaje.texto());
            ps.setString(5, mensaje.nombreArchivo());
            ps.setString(6, mensaje.hash());
            ps.setInt(7, mensaje.caracteres());
            ps.setInt(8, mensaje.palabras());
            ps.setTimestamp(9, Timestamp.valueOf(
                    mensaje.fecha() != null ? mensaje.fecha() : LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getLong(1);
                }
            }
            throw new SQLException("MySQL no devolvió el id del mensaje");
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el mensaje: " + e.getMessage(), e);
        }
    }

    @Override
    public void guardarLog(LogEventoDTO log) {
        String sql = "INSERT INTO logs (fecha, evento, emisor, receptor, detalle) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(
                    log.fecha() != null ? log.fecha() : LocalDateTime.now()));
            ps.setString(2, log.evento());
            ps.setString(3, log.emisor());
            ps.setString(4, log.receptor());
            ps.setString(5, log.detalle());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el log: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MensajeProcesadoDTO> listarMensajes() {
        List<MensajeProcesadoDTO> mensajes = new ArrayList<>();
        String sql = "SELECT id, emisor, receptor, tipo, texto, nombre_archivo, hash, "
                + "caracteres, palabras, fecha FROM mensajes ORDER BY id";
        try (Connection c = conexion.obtener();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                mensajes.add(new MensajeProcesadoDTO(
                        rs.getLong("id"),
                        rs.getString("emisor"),
                        rs.getString("receptor"),
                        TipoContenido.valueOf(rs.getString("tipo")),
                        rs.getString("texto"),
                        null,
                        rs.getString("nombre_archivo"),
                        rs.getString("hash"),
                        rs.getInt("caracteres"),
                        rs.getInt("palabras"),
                        rs.getTimestamp("fecha").toLocalDateTime()));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron listar los mensajes: " + e.getMessage(), e);
        }
        return mensajes;
    }

    @Override
    public List<LogEventoDTO> listarLogs() {
        List<LogEventoDTO> logs = new ArrayList<>();
        String sql = "SELECT id, fecha, evento, emisor, receptor, detalle FROM logs ORDER BY id";
        try (Connection c = conexion.obtener();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new LogEventoDTO(
                        rs.getLong("id"),
                        rs.getTimestamp("fecha").toLocalDateTime(),
                        rs.getString("evento"),
                        rs.getString("emisor"),
                        rs.getString("receptor"),
                        rs.getString("detalle")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron listar los logs: " + e.getMessage(), e);
        }
        return logs;
    }

    @Override
    public long registrarArchivo(ArchivoDTO archivo) {
        String sql = "INSERT INTO archivos (mensaje_id, variante, nombre_archivo, ruta, fecha) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, archivo.mensajeId());
            ps.setString(2, archivo.variante());
            ps.setString(3, archivo.nombreArchivo());
            ps.setString(4, archivo.ruta());
            ps.setTimestamp(5, Timestamp.valueOf(
                    archivo.fecha() != null ? archivo.fecha() : LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getLong(1);
                }
            }
            throw new SQLException("MySQL no devolvió el id del archivo");
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo registrar el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ArchivoDTO> listarArchivos() {
        return consultarArchivos("SELECT id, mensaje_id, variante, nombre_archivo, ruta, fecha "
                + "FROM archivos ORDER BY id", null);
    }

    @Override
    public List<ArchivoDTO> listarArchivosPorMensaje(long mensajeId) {
        return consultarArchivos("SELECT id, mensaje_id, variante, nombre_archivo, ruta, fecha "
                + "FROM archivos WHERE mensaje_id = ? ORDER BY id", mensajeId);
    }

    private List<ArchivoDTO> consultarArchivos(String sql, Long mensajeId) {
        List<ArchivoDTO> archivos = new ArrayList<>();
        try (Connection c = conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (mensajeId != null) {
                ps.setLong(1, mensajeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    archivos.add(new ArchivoDTO(
                            rs.getLong("id"),
                            rs.getLong("mensaje_id"),
                            rs.getString("variante"),
                            rs.getString("nombre_archivo"),
                            rs.getString("ruta"),
                            rs.getTimestamp("fecha").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron listar los archivos: " + e.getMessage(), e);
        }
        return archivos;
    }
}
