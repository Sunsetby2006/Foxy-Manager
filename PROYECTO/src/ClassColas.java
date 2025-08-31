import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class ClassColas extends JPanel {

    private PriorityQueue<Pedido> colaPrioridad;
    private Stack<Pedido> pilaAutoservicio;
    private ArrayList<Pedido> listaHistorial;
    private JFrame ventana;
    private CardLayout layout;
    private JPanel panelPrincipal;

    private final File carpetaLogs = new File("data/logs");
    private final File archivoPedidos = new File(carpetaLogs, "pedidos.csv");
    private final File archivoHistorial = new File(carpetaLogs, "historial.csv");

    private int siguienteID = 1;

    private final Map<String, Integer> precios;
    private final Map<String, Integer> prioridadPorCombo;

    public ClassColas(JFrame ventanaPrincipal, CardLayout layout, JPanel panelPrincipal) {
        this.ventana = ventanaPrincipal;
        this.layout = layout;
        this.panelPrincipal = panelPrincipal;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 245, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Gestión de Pedidos", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 26));
        titulo.setForeground(new Color(60, 60, 100));
        add(titulo, BorderLayout.NORTH);

        // Inicializar estructuras
        colaPrioridad = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.prioridad, p1.prioridad));
        pilaAutoservicio = new Stack<>();
        listaHistorial = new ArrayList<>();

        // Definir precios y prioridades por combo
        precios = new HashMap<>();
        prioridadPorCombo = new HashMap<>();

        precios.put("Combo Fast", 80);
        precios.put("Combo Family", 200);
        precios.put("Combo Duo", 150);
        precios.put("Combo King", 180);
        precios.put("Combo Sunset", 220);
        precios.put("Combo Cheese", 120);
        precios.put("Combo Fit", 140);
        precios.put("Combo Triple", 250);

        prioridadPorCombo.put("Combo Fast", 3);
        prioridadPorCombo.put("Combo Family", 2);
        prioridadPorCombo.put("Combo Duo", 3);
        prioridadPorCombo.put("Combo King", 4);
        prioridadPorCombo.put("Combo Sunset", 1);
        prioridadPorCombo.put("Combo Cheese", 4);
        prioridadPorCombo.put("Combo Fit", 3);
        prioridadPorCombo.put("Combo Triple", 2);

        cargarPedidosCSV();
        cargarHistorial();

        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 15, 15));
        panelBotones.setBackground(getBackground());

        JButton agregarBtn = crearBoton("Agregar Pedido");
        JButton consultarBtn = crearBoton("Consultar Cocina");
        JButton historialBtn = crearBoton("Ver Historial");
        JButton volverBtn = crearBoton("Volver");
        volverBtn.setBackground(new Color(255, 180, 180));

        panelBotones.add(agregarBtn);
        panelBotones.add(consultarBtn);
        panelBotones.add(historialBtn);
        panelBotones.add(volverBtn);

        add(panelBotones, BorderLayout.CENTER);

        // Acciones
        agregarBtn.addActionListener(e -> agregarPedido());
        consultarBtn.addActionListener(e -> consultaCocina());
        historialBtn.addActionListener(e -> mostrarHistorial());
        volverBtn.addActionListener(e -> layout.show(panelPrincipal, "Menu"));
    }

    private JButton crearBoton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setBackground(new Color(180, 200, 255));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(120, 140, 200), 2));
        return b;
    }

    public static class Pedido {
        String id, cliente, estado;
        boolean esAutoservicio;
        int prioridad;
        int precio;

        public Pedido(String id, String cliente, boolean esAutoservicio, int prioridad, int precio) {
            //Iniciamos variables 
            this.id = id;
            this.cliente = cliente;
            this.esAutoservicio = esAutoservicio;
            this.prioridad = prioridad;
            this.precio = precio;
            this.estado = "NUEVO";
        }

        public String toCSV() {
            return id + "," + cliente + "," + (esAutoservicio ? "Auto" : "Caja") + "," + prioridad + "," + precio + "," + estado;
        }

        public static Pedido fromCSV(String linea) {
            try {
                String[] p = linea.split(",");
                int precio = p.length > 4 ? Integer.parseInt(p[4]) : 0;
                Pedido ped = new Pedido(p[0], p[1], p[2].equals("Auto"), Integer.parseInt(p[3]), precio);
                if (p.length > 5) ped.estado = p[5];
                return ped;
            } catch (Exception e) {
                return new Pedido("ERR", "ERR", true, 0, 0);
            }
        }

        @Override
        public String toString() {
            return id + " - " + cliente + " - " + (esAutoservicio ? "Auto" : "Caja") +
                    " - Prioridad:" + prioridad + " - Precio:" + precio + " - " + estado;
        }
    }

    private void guardarPedidosCSV() {
        try {
            carpetaLogs.mkdirs();
            if (!archivoPedidos.exists()) archivoPedidos.createNewFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(archivoPedidos))) {
                pw.println("ID,Cliente,Tipo,Prioridad,Precio,Estado");
                for (Pedido p : pilaAutoservicio) pw.println(p.toCSV());
                for (Pedido p : colaPrioridad) pw.println(p.toCSV());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar pedidos CSV.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarPedidosCSV() {
        if (!archivoPedidos.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos))) {
            String linea;
            br.readLine(); // saltar header
            while ((linea = br.readLine()) != null) {
                Pedido p = Pedido.fromCSV(linea);
                try {
                    int idInt = Integer.parseInt(p.id);
                    if (p.esAutoservicio) pilaAutoservicio.push(p);
                    else colaPrioridad.offer(p);
                    if (idInt >= siguienteID) siguienteID = idInt + 1;
                } catch (NumberFormatException ex) {
                    System.out.println("Se ignoró línea inválida en CSV: " + linea);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarHistorial() {
        if (!archivoHistorial.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivoHistorial))) {
            String linea;
            br.readLine();
            while ((linea = br.readLine()) != null) listaHistorial.add(Pedido.fromCSV(linea));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarPedido() {
        String[] combos = {"Combo Fast", "Combo Family", "Combo Duo", "Combo Burger King", "Combo Sunset", "Combo Chess", "Combo Smart", "Combo Triple"};
        String combo = (String) JOptionPane.showInputDialog(this, "Seleccione un combo:", "Combos", JOptionPane.PLAIN_MESSAGE, null, combos, combos[0]);
        if (combo == null) return;

        String cliente = JOptionPane.showInputDialog(this, "Ingrese nombre del cliente:");
        if (cliente == null || cliente.isEmpty()) cliente = "Cliente " + siguienteID;

        String[] opcionesTipo = {"Autoservicio", "Caja"};
        int tipoSeleccion = JOptionPane.showOptionDialog(this, "Tipo de pedido:", "Seleccionar tipo",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcionesTipo, opcionesTipo[0]);
        boolean esAuto = tipoSeleccion == 0;

        int prioridad = esAuto ? 5 : prioridadPorCombo.getOrDefault(combo, 3);
        int precio = precios.getOrDefault(combo, 0);

        Pedido p = new Pedido(String.valueOf(siguienteID++), cliente, esAuto, prioridad, precio);
        p.estado = combo;

        int resp = JOptionPane.showConfirmDialog(this, "Confirmar Pedido?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            if (esAuto) pilaAutoservicio.push(p);
            else colaPrioridad.offer(p);
            guardarPedidosCSV();
            JOptionPane.showMessageDialog(this, "Pedido guardado correctamente.");
        } else JOptionPane.showMessageDialog(this, "Pedido desechado.");
    }

    private void consultaCocina() {
    ArrayList<Pedido> pedidosPendientes = new ArrayList<>();

    if (!archivoPedidos.exists()) {
        JOptionPane.showMessageDialog(this, "No hay pedidos pendientes.");
        return;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos))) {
        String linea;
        br.readLine(); // saltar encabezado
        while ((linea = br.readLine()) != null) {
            Pedido p = Pedido.fromCSV(linea);
            if (!p.estado.equalsIgnoreCase("ENTREGADO")) {
                pedidosPendientes.add(p);
            }
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Error al leer pedidos.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    StringBuilder sb = new StringBuilder("Pedidos pendientes:\n");
    for (Pedido p : pedidosPendientes) sb.append(p).append("\n");

    JOptionPane.showMessageDialog(this, sb.length() > 0 ? sb.toString() : "No hay pedidos pendientes.");
}


    private void mostrarHistorial() {
        StringBuilder sb = new StringBuilder("Historial de pedidos:\n");
        for (Pedido p : listaHistorial) sb.append(p).append("\n");
        JOptionPane.showMessageDialog(this, sb.length() > 0 ? sb.toString() : "No hay historial.");
    }
}
