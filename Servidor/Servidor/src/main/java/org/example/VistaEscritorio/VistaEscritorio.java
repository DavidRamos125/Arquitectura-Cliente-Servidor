package org.example.VistaEscritorio;

import org.example.DTORecords.LogEventoDTO;
import org.example.DTORecords.MensajeProcesadoDTO;
import org.example.FachadaServicios.FachadaServicios;
import org.example.UsuarioDTO;
import org.example.UtileriasVarias.IVista;
import org.example.UtileriasVarias.TipoContenido;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class VistaEscritorio extends JFrame implements IVista {

    private final FachadaServicios fachadaServicios = new FachadaServicios();

    private final String ADMIN = "SERVIDOR";
    private final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-M-d h:mm a", Locale.ENGLISH);
    private final DateTimeFormatter FECHA_LOG = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private long siguienteId = 1;
    private long siguienteLogId = 1;
    private final Color AZUL = new Color(0x1E88E5);
    private final Color VERDE = new Color(0x00C853);
    private final Color SELECCION = new Color(0xCCCCFF);

    private final List<String[]> conexiones = new ArrayList<>();   // {usuario, estado}
    private final List<MensajeProcesadoDTO> mensajes = new ArrayList<>();
    private final List<LogEventoDTO> logs = new ArrayList<>();
    private final List<String[]> usuarios = new ArrayList<>();     // {codigo, nombre}

    private JPanel panelConexiones, panelListaMensajes;
    private JScrollPane scrollMensajes;
    private JToggleButton btnMensajes, btnLogs;
    private JLabel lblSha, lblTipo, lblLetras, lblPalabras;
    private JTextField txtMensaje;
    private final ButtonGroup grupoUsuarios = new ButtonGroup();
    private String destinatario = "Todos";
    private int mensajeSeleccionado = -1;
    private final String PLACEHOLDER = "Ingrese el mensaje...";
    private Timer timerConexiones;

    @Override
    public void iniciar() {
        try {
            if (!fachadaServicios.servidorActivo()) {
                fachadaServicios.iniciarServidor();
                log("INICIO", ADMIN, null, "Servidor iniciado");
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo iniciar el servidor: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        iniciarActualizacionConexiones();
        refrescarConexiones();
        setVisible(true);
    }

    public VistaEscritorio() {
        super("Administración del Servidor Mensajería");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(660, 470);
        setMinimumSize(new Dimension(620, 440));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion();
            }
        });

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(12, 12, 8, 12));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 8, 8);

        // Izquierda arriba: Conexiones
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.31;
        c.weighty = 0.52;
        root.add(crearPanelConexiones(), c);

        // Derecha arriba: Mensajes / Logs
        c.gridx = 1;
        c.weightx = 0.69;
        c.insets = new Insets(0, 0, 8, 0);
        root.add(crearPanelMensajes(), c);

        // Izquierda abajo: Usuarios
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.31;
        c.weighty = 0.48;
        c.insets = new Insets(0, 0, 8, 8);
        root.add(crearPanelUsuarios(), c);

        // Derecha abajo: Detalle
        c.gridx = 1;
        c.weightx = 0.69;
        c.insets = new Insets(0, 0, 8, 0);
        root.add(crearPanelDetalle(), c);

        // Barra inferior
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weighty = 0;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        root.add(crearBarraEnvio(), c);

        cargarMensajes();
        cargarLogs();
        refrescarConexiones();
        refrescarLista();
    }

    // ---------- Paneles ----------
    private JPanel panelBase(String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(Color.BLACK, 1));
        if (titulo != null) {
            JLabel l = new JLabel(titulo);
            l.setBorder(new EmptyBorder(10, 22, 6, 0));
            p.add(l, BorderLayout.NORTH);
        }
        return p;
    }

    private JPanel crearPanelConexiones() {
        JPanel p = panelBase("Conexiones");
        panelConexiones = new JPanel();
        panelConexiones.setLayout(new BoxLayout(panelConexiones, BoxLayout.Y_AXIS));
        panelConexiones.setBackground(Color.WHITE);
        panelConexiones.setBorder(new EmptyBorder(2, 6, 6, 6));
        p.add(panelConexiones, BorderLayout.CENTER);
        return p;
    }

    private void refrescarConexiones() {
        if (panelConexiones == null) {
            return;
        }

        Set<String> conectados;

        try {
            conectados = fachadaServicios.getUsuariosConectados();
        } catch (RuntimeException e) {
            conectados = Set.of();
        }

        conexiones.clear();

        for (String usuario : conectados.stream().sorted().toList()) {
            conexiones.add(new String[]{usuario, "Conectado"});
        }

        panelConexiones.removeAll();

        if (conexiones.isEmpty()) {
            JLabel vacio = new JLabel("Sin conexiones activas");
            vacio.setFont(vacio.getFont().deriveFont(Font.PLAIN, 11f));
            vacio.setForeground(Color.GRAY);
            vacio.setBorder(new EmptyBorder(4, 2, 0, 0));
            panelConexiones.add(vacio);
        }

        for (String[] con : new ArrayList<>(conexiones)) {
            JPanel fila = new JPanel(new BorderLayout(6, 0));
            fila.setBackground(Color.WHITE);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            fila.setBorder(new EmptyBorder(2, 0, 2, 0));
            fila.add(new JLabel(new CirculoIcon(VERDE, 12, true)), BorderLayout.WEST);

            JPanel caja = new JPanel(new BorderLayout());
            caja.setBackground(Color.WHITE);
            caja.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1, true), new EmptyBorder(1, 4, 1, 4)));
            JLabel txt = new JLabel("<html>" + con[0] + " &nbsp;&nbsp;&nbsp; <span style='font-size:9px'>" + con[1] + "</span></html>");
            txt.setFont(txt.getFont().deriveFont(Font.PLAIN, 11f));
            JButton x = new JButton("x");
            x.setMargin(new Insets(0, 4, 0, 2));
            x.setBorderPainted(false);
            x.setContentAreaFilled(false);
            x.setFocusPainted(false);
            x.setFont(x.getFont().deriveFont(11f));
            x.addActionListener(e -> {
                fachadaServicios.desconectarUsuario(con[0]);
                log("CONEXION_CERRADA", ADMIN, con[0], "Sesión finalizada");
                refrescarConexiones();
            });
            caja.add(txt, BorderLayout.CENTER);
            caja.add(x, BorderLayout.EAST);
            fila.add(caja, BorderLayout.CENTER);
            panelConexiones.add(fila);
        }
        panelConexiones.revalidate();
        panelConexiones.repaint();
    }

    private JPanel crearPanelUsuarios() {
        cargarUsuarios();

        JPanel p = panelBase("Usuarios");
        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Color.WHITE);
        lista.setBorder(new EmptyBorder(2, 10, 6, 40));

        for (String[] usuario : usuarios) {
            String codigo = usuario[0];
            String nombreUsuario = usuario[1];

            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setBackground(Color.WHITE);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            fila.setBorder(new EmptyBorder(3, 0, 3, 0));

            JRadioButton rb = new JRadioButton();
            rb.setOpaque(false);
            rb.setFocusPainted(false);
            rb.setIcon(new CirculoIcon(Color.WHITE, 14, false));
            rb.setSelectedIcon(new CirculoIcon(AZUL, 14, true));
            rb.setBorder(null);
            grupoUsuarios.add(rb);
            rb.addActionListener(e -> destinatario = codigo);
            if ("Todos".equals(codigo)) rb.setSelected(true);

            JLabel nombre = new JLabel(nombreUsuario);
            nombre.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1, true), new EmptyBorder(2, 4, 2, 4)));
            nombre.setFont(nombre.getFont().deriveFont(Font.PLAIN, 12f));
            nombre.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    rb.doClick();
                }
            });
            fila.add(rb, BorderLayout.WEST);
            fila.add(nombre, BorderLayout.CENTER);
            lista.add(fila);
        }
        p.add(lista, BorderLayout.CENTER);
        return p;
    }

    private void cargarUsuarios() {
        usuarios.clear();

        for (UsuarioDTO usuario : fachadaServicios.getUsuarios()) {
            if (usuario == null) {
                continue;
            }

            usuarios.add(new String[]{
                    usuario.codigo(),
                    usuario.nombre() + " " + usuario.apellidos()
            });
        }

        if (usuarios.isEmpty()) {
            usuarios.add(new String[]{"Alex", "Alex"});
            usuarios.add(new String[]{"Juan", "Juan"});
            usuarios.add(new String[]{"Fabian", "Fabian"});
            usuarios.add(new String[]{"Pepito Perez", "Pepito Perez"});
        }

        usuarios.add(new String[]{"Todos", "Todos"});
    }

    private JPanel crearPanelMensajes() {
        JPanel p = panelBase(null);

        // Encabezado tipo pestañas
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
        JPanel tabs = new JPanel(new GridLayout(1, 2));
        tabs.setPreferredSize(new Dimension(200, 30));
        btnMensajes = crearTab("Mensajes");
        btnLogs = crearTab("Logs");
        ButtonGroup g = new ButtonGroup();
        g.add(btnMensajes);
        g.add(btnLogs);
        btnMensajes.setSelected(true);
        btnMensajes.addActionListener(e -> {
            cargarMensajes();
            refrescarLista();
        });
        btnLogs.addActionListener(e -> {
            cargarLogs();
            refrescarLista();
        });
        tabs.add(btnMensajes);
        tabs.add(btnLogs);

        JButton refresh = new JButton("\u21BB");
        refresh.setForeground(AZUL);
        refresh.setFont(refresh.getFont().deriveFont(Font.BOLD, 18f));
        refresh.setBorderPainted(false);
        refresh.setContentAreaFilled(false);
        refresh.setFocusPainted(false);
        refresh.addActionListener(e -> {
            cargarMensajes();
            cargarLogs();
            refrescarLista();
        });

        header.add(tabs, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        panelListaMensajes = new JPanel();
        panelListaMensajes.setLayout(new BoxLayout(panelListaMensajes, BoxLayout.Y_AXIS));
        panelListaMensajes.setBackground(Color.WHITE);
        panelListaMensajes.setBorder(new EmptyBorder(6, 8, 6, 8));
        scrollMensajes = new JScrollPane(panelListaMensajes);
        scrollMensajes.setBorder(null);
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(12);
        p.add(scrollMensajes, BorderLayout.CENTER);
        return p;
    }

    private JToggleButton crearTab(String texto) {
        JToggleButton b = new JToggleButton(texto);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(new MatteBorder(0, 0, 0, 1, Color.BLACK));
        b.addChangeListener(e -> b.setBackground(b.isSelected() ? Color.WHITE : new Color(0xF2F2F2)));
        return b;
    }

    private void refrescarLista() {
        panelListaMensajes.removeAll();
        if (btnMensajes.isSelected()) {
            for (int i = 0; i < mensajes.size(); i++) {
                final int idx = i;
                panelListaMensajes.add(crearFilaLista(resumen(mensajes.get(i)), i == mensajeSeleccionado, ev -> {
                    mensajeSeleccionado = idx;
                    MensajeProcesadoDTO sel = mensajes.get(idx);
                    mostrarDetalle(sel);
                    refrescarLista();
                    if (ev.getClickCount() == 2 && sel.tipo() == TipoContenido.IMAGEN) verImagen(sel);
                }));
            }
        } else {
            for (LogEventoDTO l : logs) panelListaMensajes.add(crearFilaLista(resumenLog(l), false, null));
        }
        panelListaMensajes.revalidate();
        panelListaMensajes.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = scrollMensajes.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }

    private void cargarMensajes() {
        try {
            mensajes.clear();
            mensajes.addAll(fachadaServicios.getMensajesPersistidos());

            if (mensajeSeleccionado >= mensajes.size()) {
                mensajeSeleccionado = -1;
            }
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudieron cargar los mensajes: "
                            + e.getMessage()
            );
        }
    }

    private void cargarLogs() {
        try {
            logs.clear();
            logs.addAll(fachadaServicios.getLogsPersistidos());
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudieron cargar los logs: "
                            + e.getMessage()
            );
        }
    }

    private void iniciarActualizacionConexiones() {
        if (timerConexiones != null) {
            return;
        }

        timerConexiones = new Timer(1500, e -> refrescarConexiones());
        timerConexiones.start();
    }

    private void cerrarAplicacion() {
        if (timerConexiones != null) {
            timerConexiones.stop();
        }

        try {
            fachadaServicios.detenerServidor();
        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudo detener el servidor: "
                            + e.getMessage()
            );
        }

        dispose();
        System.exit(0);
    }

    private JPanel crearFilaLista(String texto, boolean seleccionada, Consumer<MouseEvent> onClick) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(seleccionada ? SELECCION : Color.WHITE);
        fila.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1, true), new EmptyBorder(3, 6, 3, 6)));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        fila.add(l, BorderLayout.CENTER);
        if (onClick != null) {
            fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fila.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.accept(e);
                }
            });
        }
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 4, 0));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        wrap.add(fila);
        return wrap;
    }

    private JPanel crearPanelDetalle() {
        JPanel p = panelBase(null);
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
        JLabel tab = new JLabel("Detalle", SwingConstants.CENTER);
        tab.setPreferredSize(new Dimension(100, 30));
        tab.setBorder(new MatteBorder(0, 0, 0, 1, Color.BLACK));
        header.add(tab);
        p.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 8));
        grid.setBackground(Color.WHITE);
        grid.setBorder(new EmptyBorder(10, 10, 40, 10));
        lblSha = cajaDetalle("Sha256");
        lblTipo = cajaDetalle("Tipo: ");
        lblLetras = cajaDetalle("Cantidad de letras: ");
        lblPalabras = cajaDetalle("Cantidad de palabras: ");
        grid.add(lblSha);
        grid.add(lblLetras);
        grid.add(lblTipo);
        grid.add(lblPalabras);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.add(grid, BorderLayout.NORTH);
        p.add(wrap, BorderLayout.CENTER);
        return p;
    }

    private JLabel cajaDetalle(String t) {
        JLabel l = new JLabel(t);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        l.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1, true), new EmptyBorder(3, 6, 3, 6)));
        return l;
    }

    private JPanel crearBarraEnvio() {
        JPanel barra = new JPanel(new BorderLayout(6, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(2, 4, 2, 6)));

        txtMensaje = new JTextField(PLACEHOLDER);
        txtMensaje.setForeground(Color.GRAY);
        txtMensaje.setBorder(new EmptyBorder(4, 2, 4, 2));
        txtMensaje.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtMensaje.getText().equals(PLACEHOLDER)) {
                    txtMensaje.setText("");
                    txtMensaje.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtMensaje.getText().isEmpty()) {
                    txtMensaje.setText(PLACEHOLDER);
                    txtMensaje.setForeground(Color.GRAY);
                }
            }
        });
        txtMensaje.addActionListener(e -> enviar());

        JButton agregar = botonIcono(new IconoMas(AZUL));
        agregar.setToolTipText("Adjuntar imagen");
        agregar.addActionListener(e -> adjuntarImagen());
        JButton enviar = botonIcono(new IconoEnviar());
        enviar.setToolTipText("Enviar");
        enviar.addActionListener(e -> enviar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(agregar);
        botones.add(enviar);

        barra.add(txtMensaje, BorderLayout.CENTER);
        barra.add(botones, BorderLayout.EAST);
        return barra;
    }

    private JButton botonIcono(Icon icon) {
        JButton b = new JButton(icon);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ---------- Lógica ----------
    private void enviar() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty() || texto.equals(PLACEHOLDER)) return;
        MensajeProcesadoDTO m = crearTexto(ADMIN, destinatario, texto, LocalDateTime.now());
        MensajeProcesadoDTO guardado = fachadaServicios.sendMensaje(m);
        mensajes.add(guardado);
        log("MENSAJE_ENVIADO: " + texto, ADMIN, destinatario, guardado.caracteres() + " letras, " + guardado.palabras() + " palabras");
        txtMensaje.setText(PLACEHOLDER);
        txtMensaje.setForeground(Color.GRAY);
        mensajeSeleccionado = mensajes.size() - 1;
        mostrarDetalle(guardado);
        btnMensajes.setSelected(true);
        refrescarLista();
    }

    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB

    /**
     * Abre el selector de archivos, valida que sea imagen y la envía.
     */
    private void adjuntarImagen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar imagen");
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileNameExtensionFilter("Imágenes (png, jpg, jpeg, gif, bmp)",
                "png", "jpg", "jpeg", "gif", "bmp"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        try {
            if (f.length() > MAX_BYTES) {
                JOptionPane.showMessageDialog(this, "La imagen supera el máximo de 10 MB.", "Imagen muy grande", JOptionPane.WARNING_MESSAGE);
                return;
            }
            byte[] datos = Files.readAllBytes(f.toPath());
            if (ImageIO.read(new ByteArrayInputStream(datos)) == null) {
                JOptionPane.showMessageDialog(this, "El archivo no es una imagen válida.", "Archivo inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
            MensajeProcesadoDTO m = new MensajeProcesadoDTO(siguienteId++, ADMIN, destinatario,
                    TipoContenido.IMAGEN, "", datos, f.getName(), sha256(datos), 0, 0, LocalDateTime.now());
            MensajeProcesadoDTO guardado = fachadaServicios.sendMensaje(m);
            mensajes.add(guardado);
            log("IMAGEN_ENVIADA", ADMIN, destinatario, f.getName());
            mensajeSeleccionado = mensajes.size() - 1;
            mostrarDetalle(guardado);
            btnMensajes.setSelected(true);
            refrescarLista();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Vista previa de la imagen (doble clic sobre el mensaje).
     */
    private void verImagen(MensajeProcesadoDTO m) {
        if (m.archivo() == null || m.archivo().length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "La imagen no está disponible para este mensaje.",
                    "Imagen no disponible",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(m.archivo()));
            if (img == null) return;
            double esc = Math.min(1.0, Math.min(640.0 / img.getWidth(), 460.0 / img.getHeight()));
            Image vista = img.getScaledInstance((int) (img.getWidth() * esc), (int) (img.getHeight() * esc), Image.SCALE_SMOOTH);
            JDialog d = new JDialog(this, m.nombreArchivo(), true);
            d.add(new JLabel(new ImageIcon(vista)));
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo mostrar la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Construye el DTO calculando hash, caracteres (letras) y palabras.
     */
    private MensajeProcesadoDTO crearTexto(String emisor, String receptor, String texto, LocalDateTime fecha) {
        String t = texto.trim();
        int letras = 0;
        for (char ch : t.toCharArray()) if (Character.isLetter(ch)) letras++;
        int palabras = t.isEmpty() ? 0 : t.split("\\s+").length;
        return new MensajeProcesadoDTO(siguienteId++, emisor, receptor, TipoContenido.TEXTO,
                t, null, null, sha256(t), letras, palabras, fecha);
    }

    private String resumen(MensajeProcesadoDTO m) {
        LocalDateTime fechaHora = m.fecha() != null ? m.fecha() : LocalDateTime.now();
        String fecha = FECHA.format(fechaHora).replace("AM", "am").replace("PM", "pm");
        String contenido = m.tipo() == TipoContenido.IMAGEN
                ? "[Imagen] " + m.nombreArchivo()
                : (m.texto() != null ? m.texto() : "");
        return m.emisor() + ": " + contenido + "     : para " + m.receptor() + "  " + fecha;
    }

    private void mostrarDetalle(MensajeProcesadoDTO m) {
        String hash = m.hash() != null ? m.hash() : "";
        String hashCorto = hash.isEmpty()
                ? "No disponible"
                : hash.substring(0, Math.min(20, hash.length())) + "...";

        lblSha.setText("Sha256: " + hashCorto);
        lblSha.setToolTipText(hash.isEmpty() ? null : hash);
        lblTipo.setText("Tipo: " + m.tipo());

        if (m.tipo() == TipoContenido.IMAGEN) {
            if (m.archivo() == null || m.archivo().length == 0) {
                lblLetras.setText("Tamaño: no disponible");
            } else {
                int kb = Math.max(1, m.archivo().length / 1024);
                lblLetras.setText("Tamaño: " + kb + " KB");
            }

            lblPalabras.setText("Archivo: " + m.nombreArchivo());
            lblPalabras.setToolTipText(m.nombreArchivo());
        } else {
            lblLetras.setText("Cantidad de letras: " + m.caracteres());
            lblPalabras.setText("Cantidad de palabras: " + m.palabras());
            lblPalabras.setToolTipText(null);
        }
    }

    private String sha256(String s) {
        return sha256(s.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256(byte[] datos) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(datos);
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * Registra un evento en la pestaña Logs usando LogEventoDTO.
     */
    private void log(String evento, String emisor, String receptor, String detalle) {
        logs.add(new LogEventoDTO(siguienteLogId++, LocalDateTime.now(), evento, emisor, receptor, detalle));
        fachadaServicios.registrarLog(evento != null ? evento : "", emisor != null ? emisor : "", receptor != null ? receptor : "", detalle != null ?  detalle : "");
        if (panelListaMensajes != null && btnLogs != null && btnLogs.isSelected()) refrescarLista();
    }

    private String resumenLog(LogEventoDTO l) {
        String fecha = l.fecha() != null ? FECHA_LOG.format(l.fecha()) : "";
        StringBuilder sb = new StringBuilder(fecha).append("  ").append(l.evento());
        if (l.emisor() != null || l.receptor() != null) {
            sb.append("  ").append(l.emisor() != null ? l.emisor() : "-")
                    .append(" > ").append(l.receptor() != null ? l.receptor() : "-");
        }
        if (l.detalle() != null && !l.detalle().isEmpty()) sb.append("  (").append(l.detalle()).append(")");
        return sb.toString();
    }

    // ---------- Iconos dibujados ----------
    static class CirculoIcon implements Icon {
        private final Color relleno;
        private final int d;
        private final boolean lleno;

        CirculoIcon(Color relleno, int d, boolean lleno) {
            this.relleno = relleno;
            this.d = d;
            this.lleno = lleno;
        }

        public int getIconWidth() {
            return d + 2;
        }

        public int getIconHeight() {
            return d + 2;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(relleno);
            g2.fillOval(x + 1, y + 1, d, d);
            g2.setColor(Color.BLACK);
            g2.drawOval(x + 1, y + 1, d, d);
            g2.dispose();
        }
    }

    static class IconoMas implements Icon {
        private final Color color;

        IconoMas(Color c) {
            color = c;
        }

        public int getIconWidth() {
            return 18;
        }

        public int getIconHeight() {
            return 18;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x + 2, y + 2, 13, 13);
            g2.drawLine(x + 8, y + 5, x + 8, y + 12);
            g2.drawLine(x + 5, y + 8, x + 12, y + 8);
            g2.dispose();
        }
    }

    static class IconoEnviar implements Icon {
        public int getIconWidth() {
            return 22;
        }

        public int getIconHeight() {
            return 20;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            Polygon p = new Polygon(new int[]{x + 2, x + 20, x + 2, x + 6}, new int[]{y + 2, y + 10, y + 18, y + 10}, 4);
            g2.drawPolygon(p);
            g2.drawLine(x + 6, y + 10, x + 20, y + 10);
            g2.dispose();
        }
    }
}
