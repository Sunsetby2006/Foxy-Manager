// Gestion de clientes en espera
//Importaciones necesarias
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

// DIVISIÓN 3.1: GESTIÓN DE LISTA DE PEDIDOS EN ESPERA
public class ClassListas extends JPanel {
    private ArrayList<Pedido> listaEspera;
    private JTable tablaEspera;
    private DefaultTableModel modeloTabla;
    private JFrame ventana;
    private CardLayout layout;
    private JPanel panelPrincipal;
    // Nueva variable para el monitor
    private ClaseActualizar monitor;

    // DIVISIÓN 3.1.1: ESTRUCTURA DE DATOS DE PEDIDO
    //Se inician las variables
    public static class Pedido {
        public String id, cliente, entrega, combo;
        public int prioridad, precio, tiempo;
        public Pedido(String id, String cliente, String entrega, int prioridad, 
                     int precio, String combo, int tiempo) {
            this.id = id; this.cliente = cliente; this.entrega = entrega;
            this.prioridad = prioridad; this.precio = precio; 
            this.combo = combo; this.tiempo = tiempo;
        }
        
        // Función para leer un pedido desde una línea CSV
        public static Pedido fromCSV(String linea) {
            try {
                String[] p = linea.split(",");
                return new Pedido(p[0].trim(), p[1].trim(), p[2].trim(), 
                    Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim()), 
                    p[5].trim(), Integer.parseInt(p[6].trim()));
            } catch (Exception e) {
                return null;
            }
        }
    }

    //GUI
    public ClassListas(JFrame ventanaPrincipal, CardLayout layout, JPanel panelPrincipal) {
        this.ventana = ventanaPrincipal;
        this.layout = layout;
        this.panelPrincipal = panelPrincipal;
        this.listaEspera = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 250));

        inicializarInterfaz();
        refrescarDatos(); // Carga inicial
        
        // INTEGRACIÓN DE CLASEACTUALIZAR **
        // El callback llama al método que recarga y actualiza la tabla
        Runnable refrescarCallback = this::refrescarDatos;
        monitor = new ClaseActualizar("PROYECTO/data/logs", refrescarCallback);
        monitor.start();
    }
    
    // ** CRUCIAL **: Detiene el monitor cuando el panel se oculta para liberar recursos
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (monitor != null && monitor.isAlive()) {
            monitor.interrupt();
        }
    }

    // DIVISIÓN 3.2: INTERFAZ DE USUARIO
    private void inicializarInterfaz() {
        JLabel titulo = new JLabel("Clientes en Espera", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setForeground(new Color(60, 60, 100));
        add(titulo, BorderLayout.NORTH);

        String[] columnas = {"ID", "Cliente", "Entrega", "Prioridad", "Precio", "Combo", "Tiempo (min)"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaEspera = new JTable(modeloTabla);
        tablaEspera.setEnabled(false);
        add(new JScrollPane(tablaEspera), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton volverBtn = new JButton("Volver al Menú");
        volverBtn.setBackground(new Color(255, 200, 200));
        volverBtn.setFont(new Font("Arial", Font.BOLD, 14));
        volverBtn.addActionListener(e -> layout.show(panelPrincipal, "Menu"));
        
        panelBotones.add(volverBtn);
        add(panelBotones, BorderLayout.SOUTH);
    }

    // DIVISIÓN 3.3: CARGA Y ACTUALIZACIÓN DE DATOS
    private void cargarPedidosDesdeCSV() { 
        listaEspera.clear(); 
        String ruta = "PROYECTO/data/logs/pedidos.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            br.readLine();
            while ((linea = br.readLine()) != null) {
                Pedido p = Pedido.fromCSV(linea);
                if (p != null) {
                    listaEspera.add(p);
                }
            }
        } catch (IOException e) { 
            // Archivo vacío o no existe
        }
    }

    // Método centralizado para recargar datos y actualizar la tabla
    private void refrescarDatos() {
        cargarPedidosDesdeCSV();
        actualizarTabla();
    }
    
    // DIVISIÓN 3.4: ORDENAMIENTO Y VISUALIZACIÓN
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        // Ordenamiento por Prioridad (descendente) y luego por ID
        listaEspera.sort(Comparator.comparingInt((Pedido p) -> p.prioridad)
                .reversed()
                .thenComparingInt(p -> {
                    try { return Integer.parseInt(p.id); } 
                    catch (NumberFormatException e) { return 0; }
                }));

        for (Pedido p : listaEspera) {
            modeloTabla.addRow(new Object[]{
                p.id, p.cliente, p.entrega, p.prioridad, "$" + p.precio, p.combo, p.tiempo + " min"
            });
        }
    }
}