// Gestion de prioridades / busqueda binaria
//Importaciones necesarias
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

// DIVISIÓN 4.1: GESTIÓN DE COLAS DE PRIORIDAD Y ÁRBOL BINARIO
public class ClassColas extends JPanel {
    private PriorityQueue<Pedido> colaPrioridad;
    private Stack<Pedido> pilaAutoservicio;
    private ArrayList<Pedido> listaHistorial;
    private JFrame ventana;
    private CardLayout layout;
    private JPanel panelPrincipal;
    private final File carpetaLogs = new File("PROYECTO/data/logs");
    private final File archivoPedidos = new File(carpetaLogs, "pedidos.csv");
    private final File archivoHistorial = new File(carpetaLogs, "historial.csv");
    private int siguienteID = 1;
    private final Map<String, Integer> precios, prioridadPorCombo, tiemposCombo;
    // Nueva variable para el monitor
    private ClaseActualizar monitor;

    // DIVISIÓN 4.1.1: ESTRUCTURA DE ÁRBOL BINARIO PARA BÚSQUEDA
    private static class NodoArbol {
        Pedido pedido;
        NodoArbol izquierdo, derecho;
        public NodoArbol(Pedido pedido) {
            this.pedido = pedido;
            this.izquierdo = this.derecho = null;
        }
    }

    private static class ArbolBusquedaBinaria {
        // ... (métodos insertar, buscar, obtenerTodosEnOrden)
        private NodoArbol raiz;
        public ArbolBusquedaBinaria() { raiz = null; }

        public void insertar(Pedido pedido) { raiz = insertarRec(raiz, pedido); }
        
        private NodoArbol insertarRec(NodoArbol nodo, Pedido pedido) {
            if (nodo == null) return new NodoArbol(pedido);
            if (pedido.cliente.compareToIgnoreCase(nodo.pedido.cliente) < 0) {
                nodo.izquierdo = insertarRec(nodo.izquierdo, pedido);
            } else if (pedido.cliente.compareToIgnoreCase(nodo.pedido.cliente) > 0) {
                nodo.derecho = insertarRec(nodo.derecho, pedido);
            }
            return nodo;
        }

        public ArrayList<Pedido> buscar(String cliente) {
            ArrayList<Pedido> resultados = new ArrayList<>();
            buscarRec(raiz, cliente.toLowerCase(), resultados);
            return resultados;
        }

        private void buscarRec(NodoArbol nodo, String cliente, ArrayList<Pedido> resultados) {
            if (nodo == null) return;
            if (nodo.pedido.cliente.toLowerCase().contains(cliente)) resultados.add(nodo.pedido);
            buscarRec(nodo.izquierdo, cliente, resultados);
            buscarRec(nodo.derecho, cliente, resultados);
        }

        public ArrayList<Pedido> obtenerTodosEnOrden() {
            ArrayList<Pedido> resultado = new ArrayList<>();
            inOrderRec(raiz, resultado);
            return resultado;
        }

        private void inOrderRec(NodoArbol nodo, ArrayList<Pedido> resultado) {
            if (nodo != null) {
                inOrderRec(nodo.izquierdo, resultado);
                resultado.add(nodo.pedido);
                inOrderRec(nodo.derecho, resultado);
            }
        }
    }

    // DIVISIÓN 4.1.2: ESTRUCTURA DE PEDIDO
    public static class Pedido {
        String id, cliente, entrega, combo;
        int prioridad, precio, tiempo;
        public Pedido(String id, String cliente, String entrega, int prioridad, int precio, String combo, int tiempo) {
            this.id = id; this.cliente = cliente; this.entrega = entrega;
            this.prioridad = prioridad; this.precio = precio; this.combo = combo; this.tiempo = tiempo;
        }

        public String toCSV() {
            return String.join(",", id, cliente, entrega,
                String.valueOf(prioridad), String.valueOf(precio), combo, String.valueOf(tiempo)); 
        }

        public static Pedido fromCSV(String linea) {
            try {
                String[] p = linea.split(",");
                return new Pedido(p[0].trim(), p[1].trim(), p[2].trim(), 
                    Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim()), 
                    p[5].trim(), Integer.parseInt(p[6].trim()));
            } catch (Exception e) {
                return new Pedido("ERR", "ERR", "ERR", 0, 0, "ERR", 0);
            }
        }

        @Override
        public String toString() {
            return String.format("ID: %s | Cliente: %s | %s | Prioridad: %d | $%d | %s | %d min",
                id, cliente, entrega, prioridad, precio, combo, tiempo);
        }
    }

    public ClassColas(JFrame ventanaPrincipal, CardLayout layout, JPanel panelPrincipal) {
        this.ventana = ventanaPrincipal;
        this.layout = layout;
        this.panelPrincipal = panelPrincipal;
        this.listaHistorial = new ArrayList<>();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 245, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Inicializar estructuras
        colaPrioridad = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.prioridad, p1.prioridad));
        pilaAutoservicio = new Stack<>();
        
        // DIVISIÓN 4.2: HASHMAPS PARA CONFIGURACIÓN
        precios = new HashMap<>();
        prioridadPorCombo = new HashMap<>();
        tiemposCombo = new HashMap<>();
        configurarCombos();

        // Carga inicial al iniciar el programa
        cargarEstructurasDesdeCSV();
        cargarHistorial();
        inicializarInterfaz();
        
        // ** INTEGRACIÓN DE CLASEACTUALIZAR **
        Runnable refrescarCallback = this::cargarEstructurasDesdeCSV;
        monitor = new ClaseActualizar("PROYECTO/data/logs", refrescarCallback);
        monitor.start();
    }
    
    // Detiene el monitor cuando el panel se oculta
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (monitor != null && monitor.isAlive()) {
            monitor.interrupt();
        }
    }

    // DIVISIÓN 4.3: CONFIGURACIÓN DE COMBOS CON HASHMAPS
    private void configurarCombos() {
        // ... (se mantiene)
        precios.put("Combo Fast", 80); precios.put("Combo Familiar", 200);
        precios.put("Combo Duo", 150); precios.put("Combo King", 180);
        precios.put("Combo Sunset", 220); precios.put("Combo Cheese", 120);
        precios.put("Combo Fit", 140); precios.put("Combo Triple", 250);

        prioridadPorCombo.put("Combo Fast", 3); prioridadPorCombo.put("Combo Familiar", 2);
        prioridadPorCombo.put("Combo Duo", 3); prioridadPorCombo.put("Combo King", 4);
        prioridadPorCombo.put("Combo Sunset", 1); prioridadPorCombo.put("Combo Cheese", 4);
        prioridadPorCombo.put("Combo Fit", 3); prioridadPorCombo.put("Combo Triple", 2);

        tiemposCombo.put("Combo Fast", 10); tiemposCombo.put("Combo Familiar", 20);
        tiemposCombo.put("Combo Duo", 10); tiemposCombo.put("Combo King", 12);
        tiemposCombo.put("Combo Sunset", 18); tiemposCombo.put("Combo Cheese", 8);
        tiemposCombo.put("Combo Fit", 7); tiemposCombo.put("Combo Triple", 20);
    }

    // DIVISIÓN 4.4: INTERFAZ DE USUARIO
    // ... (inicializarInterfaz y crearBoton)
    private void inicializarInterfaz() {
        JLabel titulo = new JLabel("Gestión de Pedidos", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 26));
        titulo.setForeground(new Color(60, 60, 100));
        add(titulo, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 15, 15));
        panelBotones.setBackground(getBackground());

        JButton agregarBtn = crearBoton("Agregar Pedido");
        JButton consultarBtn = crearBoton("Consultar Cocina");
        JButton historialBtn = crearBoton("Ver Historial");
        JButton volverBtn = crearBoton("Volver");
        volverBtn.setBackground(new Color(255, 180, 180));

        panelBotones.add(agregarBtn); panelBotones.add(consultarBtn);
        panelBotones.add(historialBtn); panelBotones.add(volverBtn);
        add(panelBotones, BorderLayout.CENTER);

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

    // DIVISIÓN 4.5: GESTIÓN DE ARCHIVOS CSV
    private void guardarPedidosCSV() {
        try {
            carpetaLogs.mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(archivoPedidos))) {
                pw.println("ID,Cliente,Entrega,Prioridad,Precio,Combo,Tiempo");
                // Aseguramos que la pila se guarde en orden
                Stack<Pedido> tempStack = (Stack<Pedido>) pilaAutoservicio.clone();
                List<Pedido> pedidosPila = new ArrayList<>();
                while (!tempStack.isEmpty()) pedidosPila.add(tempStack.pop());
                
                for (Pedido p : pedidosPila) pw.println(p.toCSV());
                for (Pedido p : colaPrioridad) pw.println(p.toCSV());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar pedidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ** FUNCIÓN DE RECARGA USADA POR EL MONITOR **
    private void cargarEstructurasDesdeCSV() { 
        colaPrioridad.clear(); // Limpiar la cola
        pilaAutoservicio.clear(); // Limpiar la pila
        siguienteID = 1; // Reiniciar el contador

        if (!archivoPedidos.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos))) {
            String linea; br.readLine();
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    Pedido p = Pedido.fromCSV(linea);
                    if (p.id.equals("ERR")) continue;
                    
                    try {
                        int idInt = Integer.parseInt(p.id);
                        if (p.entrega.equals("Autoservicio")) pilaAutoservicio.push(p);
                        else colaPrioridad.offer(p);
                        if (idInt >= siguienteID) siguienteID = idInt + 1;
                    } catch (NumberFormatException ex) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar pedidos en ClassColas: " + e.getMessage());
        }
    }

    private void cargarHistorial() {
        listaHistorial.clear();
        if (!archivoHistorial.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivoHistorial))) {
            String linea; br.readLine();
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    Pedido pedido = Pedido.fromCSV(linea);
                    if (!pedido.id.equals("ERR")) listaHistorial.add(pedido);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // DIVISIÓN 4.6: OPERACIONES DE PEDIDOS
    private void agregarPedido() {
        //  (Lógica de selección de combos y entrada de datos)
        String[] combos = {"Combo Fast", "Combo Familiar", "Combo Duo", "Combo King", 
                          "Combo Sunset", "Combo Cheese", "Combo Fit", "Combo Triple"};
        
        String combo = (String) JOptionPane.showInputDialog(this, 
            "Seleccione un combo:", "Combos", JOptionPane.PLAIN_MESSAGE, null, combos, combos[0]);
        if (combo == null) return;

        String cliente = JOptionPane.showInputDialog(this, "Ingrese nombre del cliente:");
        if (cliente == null || cliente.isEmpty()) cliente = "Cliente " + siguienteID;

        String[] opcionesEntrega = {"Autoservicio", "Caja"};
        String entrega = (String) JOptionPane.showInputDialog(this,
            "Tipo de entrega:", "Entrega", JOptionPane.PLAIN_MESSAGE, null, opcionesEntrega, opcionesEntrega[0]);
        if (entrega == null) return;

        int prioridad = entrega.equals("Autoservicio") ? 5 : prioridadPorCombo.getOrDefault(combo, 3); 
        int precio = precios.getOrDefault(combo, 0);
        int tiempo = tiemposCombo.getOrDefault(combo, 10);

        Pedido p = new Pedido(String.valueOf(siguienteID++), cliente, entrega, prioridad, precio, combo, tiempo);

        int resp = JOptionPane.showConfirmDialog(this, 
            "¿Confirmar pedido?\n" + p.toString(), "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (resp == JOptionPane.YES_OPTION) {
            // Recargar ANTES de añadir el nuevo para no perder pedidos ya existentes
            cargarEstructurasDesdeCSV();
            
            if (entrega.equals("Autoservicio")) pilaAutoservicio.push(p);
            else colaPrioridad.offer(p);
            
            guardarPedidosCSV();
            JOptionPane.showMessageDialog(this, "Pedido guardado correctamente.");
        }
    }

    // DIVISIÓN 4.6: OPERACIONES DE PEDIDOS
private void consultaCocina() {
    ArrayList<Pedido> pedidosPendientes = new ArrayList<>();
    if (!archivoPedidos.exists()) {
        JOptionPane.showMessageDialog(this, "No hay pedidos pendientes."); return;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos))) {
        String linea; br.readLine();
        while ((linea = br.readLine()) != null) {
            if (!linea.trim().isEmpty()) pedidosPendientes.add(Pedido.fromCSV(linea));
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Error al leer pedidos.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (pedidosPendientes.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No hay pedidos pendientes."); return;
    }

    // AÑADIR: Ordenar la lista por Prioridad (descendente) y luego por ID (alfabético para IDs)
    pedidosPendientes.sort(Comparator.comparingInt((Pedido p) -> p.prioridad).reversed()
            .thenComparing(p -> p.id)); // El ID se ordena alfabéticamente (por defecto de String)

    StringBuilder sb = new StringBuilder("PEDIDOS PENDIENTES:\n\n");
    for (Pedido p : pedidosPendientes) sb.append(p.toString()).append("\n\n");

    mostrarEnScrollPane(sb.toString(), "Pedidos en Cocina");
}

    // DIVISIÓN 4.7: GESTIÓN DE HISTORIAL CON ÁRBOL BINARIO
    // (mostrarHistorial y demás métodos)
    private void mostrarHistorial() {
        cargarHistorial(); 
        
        if (!LoginAppSwing.UserSession.currentRole.equalsIgnoreCase("gerente")) {
            JOptionPane.showMessageDialog(this, 
                "Acceso denegado\nSolo los gerentes pueden acceder al historial.", 
                "Permiso insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listaHistorial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay historial de pedidos."); return;
        }

        ArbolBusquedaBinaria arbolHistorial = new ArbolBusquedaBinaria();
        for (Pedido pedido : listaHistorial) arbolHistorial.insertar(pedido);

        String[] opciones = {"Buscar cliente en historial", "Mostrar historial completo", "Cancelar"};
        int opcion = JOptionPane.showOptionDialog(this,
            "Seleccione una opción para el historial:", "Historial de Pedidos - Gerente",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (opcion == 0) buscarEnHistorial(arbolHistorial);
        else if (opcion == 1) mostrarHistorialCompleto(arbolHistorial);
    }

    private void buscarEnHistorial(ArbolBusquedaBinaria arbol) {
        String clienteABuscar = JOptionPane.showInputDialog(this,
            "Ingrese el nombre del cliente a buscar:", "Búsqueda en Historial", JOptionPane.QUESTION_MESSAGE);

        if (clienteABuscar == null || clienteABuscar.trim().isEmpty()) return;

        ArrayList<Pedido> resultados = arbol.buscar(clienteABuscar.trim());
        if (!resultados.isEmpty()) {
            StringBuilder sb = new StringBuilder("Resultados de búsqueda para: " + clienteABuscar + "\n\n");
            for (Pedido p : resultados) sb.append(p.toString()).append("\n\n");
            mostrarEnScrollPane(sb.toString(), "Resultados de Búsqueda");
        } else {
            JOptionPane.showMessageDialog(this,
                "No se encontró ningún pedido para: " + clienteABuscar,
                "Búsqueda sin resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarHistorialCompleto(ArbolBusquedaBinaria arbol) {
        ArrayList<Pedido> historialOrdenado = arbol.obtenerTodosEnOrden();
        StringBuilder sb = new StringBuilder("HISTORIAL COMPLETO DE PEDIDOS\n");
        sb.append("(Ordenado alfabéticamente por cliente)\n\n");
        sb.append("Total de pedidos: ").append(historialOrdenado.size()).append("\n\n");
        for (Pedido p : historialOrdenado) sb.append(p.toString()).append("\n\n");
        mostrarEnScrollPane(sb.toString(), "Historial Completo - Gerente");
    }

    private void mostrarEnScrollPane(String contenido, String titulo) {
        JTextArea textArea = new JTextArea(contenido);
        textArea.setEditable(false); textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        JOptionPane.showMessageDialog(this, scrollPane, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}