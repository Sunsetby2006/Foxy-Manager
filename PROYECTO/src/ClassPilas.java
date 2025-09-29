//Gestion de pilas y los tiempos de los pedidos
//Importaciones necesarias
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

// DIVISIÓN 5.1: GESTIÓN DE TIEMPOS Y ESTADO DE PEDIDOS
public class ClassPilas extends JPanel {
    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private JButton completarBtn, refrescarBtn, volverBtn, historialBtn;
    private File carpetaLogs = new File("PROYECTO/data/logs");
    private File archivoPedidos = new File(carpetaLogs, "pedidos.csv");
    private File archivoHistorial = new File(carpetaLogs, "historial.csv");
    private File archivoTiempos = new File(carpetaLogs, "tiempos_inicio.csv");
    private CardLayout layout;
    private JPanel panelPrincipal;
    private javax.swing.Timer cronometro;
    private Map<String, LocalDateTime> tiemposInicio;
    // Variable para el monitor de archivos
    private ClaseActualizar monitor;

    // Estructura de Pedido
    public static class Pedido {
        public String id, cliente, entrega, combo;
        public int prioridad, precio, tiempo;
        public Pedido(String id, String cliente, String entrega, int prioridad, 
                     int precio, String combo, int tiempo) {
            this.id = id; this.cliente = cliente; this.entrega = entrega;
            this.prioridad = prioridad; this.precio = precio; 
            this.combo = combo; this.tiempo = tiempo;
        }
    }

    public ClassPilas(JFrame ventana, CardLayout layout, JPanel panelPrincipal) {
        this.layout = layout;
        this.panelPrincipal = panelPrincipal;
        this.tiemposInicio = new HashMap<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 250));

        inicializarInterfaz();
        cargarTiemposInicio();
        cargarPedidos(); // Carga inicial
        iniciarCronometro();
        
        // ** INTEGRACIÓN DE CLASEACTUALIZAR **
        Runnable refrescarCallback = this::cargarPedidos;
        monitor = new ClaseActualizar("PROYECTO/data/logs", refrescarCallback);
        monitor.start();
    }
    
    // Detiene el cronómetro y el monitor cuando el panel se oculta
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (cronometro != null && cronometro.isRunning()) cronometro.stop();
        if (monitor != null && monitor.isAlive()) {
            monitor.interrupt();
        }
    }
    
    // DIVISIÓN 5.2: INTERFAZ DE USUARIO
    private void inicializarInterfaz() {
        JLabel titulo = new JLabel("Menú de Cocineros", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setForeground(new Color(60, 60, 100));
        add(titulo, BorderLayout.NORTH);

        String[] columnas = {"ID", "Cliente", "Entrega", "Prioridad", "Precio", "Combo", "T. Prep. (min)", "Estado", "T. Restante"};
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tablaPedidos = new JTable(modeloTabla);
        
        tablaPedidos.setDefaultRenderer(Object.class, new EstadoRenderer());
        add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        completarBtn = new JButton("Completar Pedido");
        refrescarBtn = new JButton("Refrescar Lista");
        volverBtn = new JButton("Volver al Menú");
        historialBtn = new JButton("Ver Historial");

        // Estilos
        completarBtn.setBackground(new Color(144, 238, 144));
        refrescarBtn.setBackground(new Color(200, 255, 200));
        volverBtn.setBackground(new Color(255, 180, 180));
        historialBtn.setBackground(new Color(180, 200, 255));

        // Listeners
        completarBtn.addActionListener(e -> completarPedido());
        refrescarBtn.addActionListener(e -> cargarPedidos());
        volverBtn.addActionListener(e -> layout.show(panelPrincipal, "Menu"));
        historialBtn.addActionListener(e -> mostrarHistorial());
        
        panelBotones.add(completarBtn); panelBotones.add(refrescarBtn);
        panelBotones.add(historialBtn); panelBotones.add(volverBtn);
        add(panelBotones, BorderLayout.SOUTH);
    }

    // DIVISIÓN 5.3: LÓGICA DE TIEMPOS Y ESTRUCTURAS
    
    private void iniciarCronometro() {
        if (cronometro == null) {
            cronometro = new javax.swing.Timer(1000, e -> cargarPedidos());
        }
        if (!cronometro.isRunning()) {
            cronometro.start();
        }
    }

    // FUNCIÓN DE RECARGA USADA POR EL MONITOR Y EL CRONÓMETRO
private void cargarPedidos() {
    // 1. CAPTURAR EL ID SELECCIONADO ANTES DE BORRAR LA TABLA
    String idSeleccionado = null; 
    int filaActual = tablaPedidos.getSelectedRow(); 
    if (filaActual != -1) {
        try {
            idSeleccionado = (String) modeloTabla.getValueAt(filaActual, 0);
        } catch (Exception ignored) {}
    }

    modeloTabla.setRowCount(0);
    cargarTiemposInicio(); 
    
    List<Pedido> pedidosPendientes = new ArrayList<>();
    // Cargar pedidos desde CSV
    try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos))) {
        String linea; br.readLine();
        while ((linea = br.readLine()) != null) {
            String[] datos = linea.split(",");
            if (datos.length >= 7) {
                try {
                    pedidosPendientes.add(new Pedido(datos[0].trim(), datos[1].trim(), datos[2].trim(), 
                                                     Integer.parseInt(datos[3].trim()), Integer.parseInt(datos[4].trim()), 
                                                     datos[5].trim(), Integer.parseInt(datos[6].trim())));
                } catch (NumberFormatException ignored) {}
            }
        }
    } catch (IOException e) {
        // No hacer nada si el archivo no existe o está vacío
    }
    
    pedidosPendientes.sort(Comparator.comparingInt((Pedido p) -> p.prioridad)
            .reversed()
            .thenComparing(p -> p.id));
    LocalDateTime ahora = LocalDateTime.now();
    
    // RASTREADOR DE SELECCIÓN
    int filaAReseleccionar = -1; 
    int filaContador = 0;

    for (Pedido p : pedidosPendientes) {
        // Asignar tiempo de inicio si no existe
        if (!tiemposInicio.containsKey(p.id)) {
            tiemposInicio.put(p.id, ahora);
            guardarTiemposInicio();
        }
        
        LocalDateTime inicio = tiemposInicio.get(p.id);
        int tiempoPreparacion = p.tiempo * 60; // tiempo en segundos (minutos a segundos)
        long segundosTranscurridos = ChronoUnit.SECONDS.between(inicio, ahora);
        long tiempoRestante = tiempoPreparacion - segundosTranscurridos;
        
        String estado = tiempoRestante > 0 ? "EN PREPARACIÓN" : "EXCEDIDO";
        String tiempoRestanteStr = tiempoRestante > 0 ? tiempoRestante + "s" : "+" + Math.abs(tiempoRestante) + "s";
        
        modeloTabla.addRow(new Object[]{
            p.id, p.cliente, p.entrega, p.prioridad, "$" + p.precio, p.combo, p.tiempo + " min", estado, tiempoRestanteStr
        });
        
        // 2. RASTREAR LA NUEVA POSICIÓN
        if (idSeleccionado != null && idSeleccionado.equals(p.id)) {
            filaAReseleccionar = filaContador;
        }
        filaContador++;
    }
    
    // 3. RESTAURAR LA SELECCIÓN AL FINAL DEL MÉTODO
    if (filaAReseleccionar != -1) {
        tablaPedidos.setRowSelectionInterval(filaAReseleccionar, filaAReseleccionar); // <--- AÑADIR ESTA LÍNEA
    }
}
    
    private void completarPedido() {
    // ** CORRECCIÓN: Detener el cronómetro antes de obtener la selección **
    if (cronometro != null && cronometro.isRunning()) {
        cronometro.stop();
    }
    
    // 1. Obtener la fila seleccionada
    int filaSeleccionada = tablaPedidos.getSelectedRow(); 
    
    if (filaSeleccionada == -1) {
        // Si la selección se perdió o no había nada seleccionado, mostramos el mensaje.
        JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla para completar.", "Error", JOptionPane.ERROR_MESSAGE);
        iniciarCronometro(); // Reiniciar cronómetro
        return;
    }

    // Código necesario para obtener el ID, que faltaba en tu fragmento:
    String idPedido = (String) modeloTabla.getValueAt(filaSeleccionada, 0); 

    int resp = JOptionPane.showConfirmDialog(this, 
        "¿Desea marcar el pedido " + idPedido + " como completado?", 
        "Confirmar", JOptionPane.YES_NO_OPTION);
    
    if (resp == JOptionPane.YES_OPTION) {
        // 1. Operaciones de archivo (eliminan el pedido y activan el monitor)
        moverPedidoAHistorial(idPedido);
        tiemposInicio.remove(idPedido);
        guardarTiemposInicio(); 
        // 2. Pequeña pausa para asegurar que el monitor detecte el cambio
        try {
            Thread.sleep(150); 
        } catch (InterruptedException ignored) {}
        
        // 3. Recarga manual final.
        cargarPedidos(); 
        JOptionPane.showMessageDialog(this, "Pedido " + idPedido + " completado y movido al historial.");
    }
    
    // 4. Reiniciar el cronómetro.
    iniciarCronometro();
}
    
    // ... (El resto de los métodos se mantiene igual)

    private void moverPedidoAHistorial(String idPedido) {
        File tempFile = new File(carpetaLogs, "temp_pedidos.csv");
        Pedido pedidoCompletado = null;
        
        try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos));
             PrintWriter pw = new PrintWriter(new FileWriter(tempFile))) {
            
            String linea;
            pw.println(br.readLine()); // Escribir cabecera
            
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith(idPedido + ",")) {
                    // Este es el pedido a mover
                    String[] datos = linea.split(",");
                    pedidoCompletado = new Pedido(datos[0].trim(), datos[1].trim(), datos[2].trim(), 
                                                Integer.parseInt(datos[3].trim()), Integer.parseInt(datos[4].trim()), 
                                                datos[5].trim(), Integer.parseInt(datos[6].trim()));
                } else {
                    // Mantener el resto de pedidos en el archivo temporal
                    pw.println(linea);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error de archivo al completar pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Renombrar temporal a original
        if (archivoPedidos.delete()) {
            tempFile.renameTo(archivoPedidos);
        }

        // Agregar al historial
        if (pedidoCompletado != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(archivoHistorial, true))) {
                pw.println(String.join(",", pedidoCompletado.id, pedidoCompletado.cliente, pedidoCompletado.entrega,
                        String.valueOf(pedidoCompletado.prioridad), String.valueOf(pedidoCompletado.precio),
                        pedidoCompletado.combo, String.valueOf(pedidoCompletado.tiempo)));
            } catch (IOException e) {
                // Notificar error de historial
            }
        }
    }

    private void cargarTiemposInicio() {
        tiemposInicio.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        if (!archivoTiempos.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(archivoTiempos))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",", 2);
                if (partes.length == 2) {
                    tiemposInicio.put(partes[0].trim(), LocalDateTime.parse(partes[1].trim(), formatter));
                }
            }
        } catch (IOException | java.time.format.DateTimeParseException e) {
            // Ignorar errores
        }
    }

    private void guardarTiemposInicio() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoTiempos))) {
            for (Map.Entry<String, LocalDateTime> entry : tiemposInicio.entrySet()) {
                pw.println(entry.getKey() + "," + entry.getValue().format(formatter));
            }
        } catch (IOException e) {
            // Notificar error de guardado de tiempos
        }
    }
    
    // DIVISIÓN 5.4: GESTIÓN DE HISTORIAL
    private void mostrarHistorial() {
        DefaultTableModel modeloHistorial = new DefaultTableModel(
            new String[]{"ID", "Cliente", "Entrega", "Prioridad", "Precio", "Combo", "Tiempo (min)"}, 0);
        
        cargarHistorial(modeloHistorial);

        JTable tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setEnabled(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        JPanel panelHistorial = new JPanel(new BorderLayout());
        panelHistorial.add(scrollPane, BorderLayout.CENTER);
        panelHistorial.add(crearPanelInfoHistorial(modeloHistorial), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panelHistorial, "Historial de Pedidos Completados", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void cargarHistorial(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        if (!archivoHistorial.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(archivoHistorial))) {
            String linea; br.readLine(); // Saltar cabecera
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 7) {
                    modelo.addRow(new Object[]{datos[0], datos[1], datos[2], datos[3], 
                        "$" + datos[4], datos[5], datos[6] + " min"});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel crearPanelInfoHistorial(DefaultTableModel modelo) {
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel infoLabel = new JLabel("<html><b>Total de pedidos completados:</b> " + 
            modelo.getRowCount() + "</html>");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfo.add(infoLabel);

        return panelInfo;
    }

    // DIVISIÓN 5.5: RENDERIZADOR DE ESTADO (Colores)
private static class EstadoRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
                                                 boolean isSelected, boolean hasFocus, 
                                                 int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String tiempoRestanteStr = (String) table.getValueAt(row, 8); // Columna 8 es "T. Restante"
        String estado = (String) table.getValueAt(row, 7); // Columna 7 es "Estado"
        long segundosRestantes = 0;

        try {
            if (tiempoRestanteStr.startsWith("+")) {
                // Excedido
                segundosRestantes = -1 * Long.parseLong(tiempoRestanteStr.substring(1).replace("s", "").trim());
            } else if (tiempoRestanteStr.endsWith("s")) {
                // En preparación
                segundosRestantes = Long.parseLong(tiempoRestanteStr.replace("s", "").trim());
            }
        } catch (NumberFormatException ignored) { }

        // Definición de umbrales
        // Más de 5 min (300 s) -> VERDE
        // 4:59 min a 2:31 min (299 s a 151 s) -> NARANJA
        // Menos de 2:30 min (150 s o menos, pero no excedido) -> ROJO
        // Excedido (segundosRestantes <= 0) -> GRIS (ya manejado con el estado "EXCEDIDO")

        if ("EXCEDIDO".equals(estado) || segundosRestantes <= 0) {
            c.setBackground(new Color(190, 190, 190)); // GRIS
        } else if (segundosRestantes > (5 * 60)) { // > 300 segundos (5 minutos)
            c.setBackground(new Color(200, 255, 200)); // VERDE claro
        } else if (segundosRestantes > (2 * 60) + 30) { // > 150 segundos (2:30 minutos)
            c.setBackground(new Color(255, 210, 180)); // NARANJA claro
        } else { // 150 segundos o menos
            c.setBackground(new Color(255, 180, 180)); // ROJO claro
        }

        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
        }
        return c;
    }
  }
}