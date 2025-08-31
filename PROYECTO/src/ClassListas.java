import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class ClassListas extends JPanel {

    private ArrayList<Pedido> listaEspera;
    private JTable tablaEspera;
    private DefaultTableModel modeloTabla;

    private JFrame ventana;
    private CardLayout layout;
    private JPanel panelPrincipal;

    // Clase interna para representar un pedido
    public static class Pedido {
        public String id;
        public String cliente;
        public String estado;
        public boolean esAutoservicio;

        public Pedido(String id, String cliente, String estado, boolean esAutoservicio) {
            this.id = id;
            this.cliente = cliente;
            this.estado = estado;
            this.esAutoservicio = esAutoservicio;
        }
    }

    // Constructor completo
    public ClassListas(JFrame ventanaPrincipal, CardLayout layout, JPanel panelPrincipal) {
        this.ventana = ventanaPrincipal;
        this.layout = layout;
        this.panelPrincipal = panelPrincipal;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 250));

        JLabel titulo = new JLabel("Clientes en Espera", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        listaEspera = new ArrayList<>();

        // Crear tabla
        String[] columnas = {"ID", "Cliente", "Orden", "Tipo"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaEspera = new JTable(modeloTabla);
        tablaEspera.setEnabled(false);
        add(new JScrollPane(tablaEspera), BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Botón volver
        JButton volverBtn = new JButton("Volver");
        volverBtn.setBackground(new Color(255, 200, 200));
        volverBtn.setFont(new Font("Arial", Font.BOLD, 14));
        volverBtn.addActionListener(e -> layout.show(panelPrincipal, "Menu"));
        panelBotones.add(volverBtn);

        // Botón refrescar
        JButton refrescarBtn = new JButton("Refrescar");
        refrescarBtn.setBackground(new Color(200, 255, 200));
        refrescarBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refrescarBtn.addActionListener(e -> refrescar());
        panelBotones.add(refrescarBtn);

        add(panelBotones, BorderLayout.SOUTH);

        // Cargar pedidos desde CSV
        cargarPedidosDesdeCSV();

        // Mostrar tabla
        actualizarTabla();
    }

    // Método público para refrescar la lista desde CSV
    public void refrescar() {
        listaEspera.clear();
        cargarPedidosDesdeCSV();
        actualizarTabla();
    }

    // Leer CSV y llenar listaEspera
    private void cargarPedidosDesdeCSV() {
        String ruta = "data/logs/pedidos.csv"; // ruta relativa al proyecto
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            br.readLine(); // saltar encabezado
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 6) { // asegurar que exista estado
                    String id = datos[0].trim();
                    String cliente = datos[1].trim();
                    String estado = datos[5].trim();
                    boolean esAutoservicio = Boolean.parseBoolean(datos[3].trim());

                    Pedido pedido = new Pedido(id, cliente, estado, esAutoservicio);
                    listaEspera.add(pedido);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Actualiza la tabla
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);

        // Ordenar por ID numérico
        listaEspera.sort(Comparator.comparingInt(p -> {
            try {
                return Integer.parseInt(p.id);
            } catch (NumberFormatException e) {
                return 0;
            }
        }));

        for (Pedido p : listaEspera) {
            modeloTabla.addRow(new Object[]{
                    p.id,
                    p.cliente,
                    p.estado,
                    p.esAutoservicio ? "Auto" : "Caja"
            });
        }
    }
}
