package org.example;

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
    public void guardarMensaje(MensajeProcesadoDTO mensaje) {
        String sql = "INSERT INTO mensajes (emisor, receptor, tipo, texto, nombre_archivo, hash, "
                + "caracteres, palabras, fecha) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
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
}