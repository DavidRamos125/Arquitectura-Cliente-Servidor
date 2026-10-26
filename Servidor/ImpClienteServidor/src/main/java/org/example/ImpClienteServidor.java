package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

public class ImpClienteServidor implements IClienteServidor {

    private final ObjectMapper mapper;

    public ImpClienteServidor() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public byte[] construirPaquete(MensajeDTO mensajeDTO) {
        try {
            return mapper.writeValueAsBytes(mensajeDTO);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Error al convertir el mensaje a bytes", e
            );
        }
    }

    @Override
    public MensajeDTO obtenerMensaje(byte[] datos) {
        try {
            return mapper.readValue(datos, MensajeDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Error al convertir los bytes a MensajeDTO", e
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] construirPaquete(PaqueteMensajeDTO paquete) {
        try {
            return mapper.writeValueAsBytes(paquete);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Error al construir la cabecera", e
            );
        }
    }

    @Override
    public PaqueteMensajeDTO obtenerPaquete(byte[] datos) {
        try {
            return mapper.readValue(datos, PaqueteMensajeDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Error al interpretar la cabecera", e
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
