import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ClassPilas extends JPanel {

    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private JButton completarBtn, refrescarBtn, volverBtn;
    private File carpetaLogs = new File("data/logs");
    private File archivoPedidos = new File(carpetaLogs, "pedidos.csv");
    private File archivoHistorial = new File(carpetaLogs, "historial.csv");

    private CardLayout layout;
    private JPanel panelPrincipal;

    public ClassPilas(JFrame ventana, CardLayout layout, JPanel panelPrincipal) {
        this.layout = layout;
        this.panelPrincipal = panelPrincipal;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 250));

        JLabel titulo = new JLabel("Menú de Cocineros", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(50, 50, 80));
        add(titulo, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Cliente", "Tipo", "Prioridad", "Precio", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPedidos = new JTable(modeloTabla);
        tablaPedidos.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaPedidos.setRowHeight(25);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tablaPedidos);
        add(scroll, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        completarBtn = new JButton("Completar Pedido");
        refrescarBtn = new JButton("Refrescar");
        volverBtn = new JButton("Volver");

        for (JButton b : new JButton[]{completarBtn, refrescarBtn, volverBtn}) {
            b.setFont(new Font("Arial", Font.BOLD, 14));
            b.setBackground(new Color(180, 220, 255));
        }

        panelBotones.add(completarBtn);
        panelBotones.add(refrescarBtn);
        panelBotones.add(volverBtn);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        completarBtn.addActionListener(e -> completarPedido());
        refrescarBtn.addActionListener(e -> cargarPedidos());
        volverBtn.addActionListener(e -> layout.show(panelPrincipal, "Menu")); // Regresar al menú principal

        cargarPedidos();
    }

    private void cargarPedidos() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        if (!archivoPedidos.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos))) {
            String linea;
            br.readLine(); // saltar header
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split(",");
                if (p.length >= 6) modeloTabla.addRow(p);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completarPedido() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para completar.");
            return;
        }

        String[] datos = new String[6];
        for (int i = 0; i < 6; i++) datos[i] = modeloTabla.getValueAt(fila, i).toString();

        // Guardar en historial
        try {
            if (!archivoHistorial.exists()) {
                carpetaLogs.mkdirs();
                archivoHistorial.createNewFile();
                try (PrintWriter pw = new PrintWriter(new FileWriter(archivoHistorial))) {
                    pw.println("ID,Cliente,Tipo,Prioridad,Precio,Estado");
                }
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(archivoHistorial, true))) {
                pw.println(String.join(",", datos));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar en historial.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Eliminar del CSV de pedidos
        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoPedidos))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.startsWith(datos[0] + ",")) lineas.add(linea);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar pedidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoPedidos))) {
            for (String l : lineas) pw.println(l);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar pedidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        cargarPedidos();
        JOptionPane.showMessageDialog(this, "Pedido completado y movido al historial.");
    }
}
