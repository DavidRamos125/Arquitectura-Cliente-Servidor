package org.example.InicioServidor;

import org.example.UtileriasVarias.IVista;
import org.example.VistaEscritorio.VistaEscritorio;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        IVista vistaEscritorio = new VistaEscritorio();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            vistaEscritorio.iniciar();
        });
    }
}