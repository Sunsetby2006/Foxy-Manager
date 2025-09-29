//Menu principal - navegacion del sistema
//Importaciones necesarias
import java.awt.*;
import javax.swing.*;

// DIVISIÓN 2.1: GESTIÓN DE VENTANA PRINCIPAL
public class Main {
    public static CardLayout layout;
    public static JPanel panelPrincipal;
    public static JFrame ventanaPrincipal;

    public static void main(String[] args) { //Este es el que se ejecuta 
        SwingUtilities.invokeLater(() -> new LoginAppSwing());
    }

    // DIVISIÓN 2.2: INICIALIZACIÓN DEL MENÚ PRINCIPAL
    //Creamos la interfaz grafica 
    public static void mostrarMenuInicio() {
        ventanaPrincipal = new JFrame("FOXY MANAGER - Menú Principal");
        ventanaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventanaPrincipal.setSize(1000, 900);
        ventanaPrincipal.setLocationRelativeTo(null);

        layout = new CardLayout();
        panelPrincipal = new JPanel(layout);

        JPanel menuPanel = crearPanelMenu();
        JPanel listasPanel = new ClassListas(ventanaPrincipal, layout, panelPrincipal); 
        JPanel colasPanel = new ClassColas(ventanaPrincipal, layout, panelPrincipal);  
        JPanel pilasPanel = new ClassPilas(ventanaPrincipal, layout, panelPrincipal);  

        panelPrincipal.add(menuPanel, "Menu");
        panelPrincipal.add(listasPanel, "Listas");
        panelPrincipal.add(colasPanel, "Colas");
        panelPrincipal.add(pilasPanel, "Pilas");

        ventanaPrincipal.add(panelPrincipal);
        ventanaPrincipal.setVisible(true);
    }

    // DIVISIÓN 2.3: DISEÑO DEL PANEL DEL MENÚ
    private static JPanel crearPanelMenu() {
        JPanel menuPanel = new JPanel(new BorderLayout(10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuPanel.setBackground(new Color(245, 245, 250));

        JPanel tituloPanel = new JPanel(new GridLayout(2, 1));
        tituloPanel.setBackground(new Color(245, 245, 250));
        JLabel titulo = new JLabel("FOXY MANAGER", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(60, 60, 100));
        JLabel subtitulo = new JLabel("Sistema de Gestión de Pedidos", JLabel.CENTER);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitulo.setForeground(new Color(100, 100, 140));
        tituloPanel.add(titulo); tituloPanel.add(subtitulo);

        JPanel botonesPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        botonesPanel.setBackground(new Color(245, 245, 250));
        
        JButton botonListas = crearBotonMenu("Clientes en Espera");
        JButton botonColas = crearBotonMenu("Gestión de Pedidos");
        JButton botonPilas = crearBotonMenu("Pedidos en Cocina");
        JButton botonSalir = crearBotonMenu("Salir");
        botonSalir.setBackground(new Color(255, 150, 150));

        botonesPanel.add(botonListas); botonesPanel.add(botonColas);
        botonesPanel.add(botonPilas); botonesPanel.add(botonSalir);

        menuPanel.add(tituloPanel, BorderLayout.NORTH);
        menuPanel.add(botonesPanel, BorderLayout.CENTER);

        configurarAccionesBotones(botonListas, botonColas, botonPilas, botonSalir);
        return menuPanel;
    }

    // DIVISIÓN 2.4: CONFIGURACIÓN DE BOTONES
    private static void configurarAccionesBotones(JButton listas, JButton colas, JButton pilas, JButton salir) {
        listas.addActionListener(e -> layout.show(panelPrincipal, "Listas"));
        colas.addActionListener(e -> layout.show(panelPrincipal, "Colas"));
        pilas.addActionListener(e -> layout.show(panelPrincipal, "Pilas"));
        salir.addActionListener(e -> System.exit(0));
    }

    private static JButton crearBotonMenu(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Arial", Font.BOLD, 18));
        b.setBackground(new Color(180, 200, 255));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 140, 200), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return b;
    }
}