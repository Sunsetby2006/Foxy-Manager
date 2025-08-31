import java.awt.*;
import javax.swing.*;

public class main {
    // Referencias globales
    public static CardLayout layout;
    public static JPanel panelPrincipal;

    public static void main(String[] args) {
        mostrarPantallaDeCarga();
        mostrarMenuInicio();
    }

    public static void mostrarPantallaDeCarga() {
        JWindow loadingWindow = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();

        JLabel label = new JLabel("Cargando aplicación...", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        content.add(label, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);

        loadingWindow.getContentPane().add(content);
        loadingWindow.setSize(300, 100);
        loadingWindow.setLocationRelativeTo(null);
        loadingWindow.setVisible(true);

        for (int i = 0; i <= 100; i++) {
            progressBar.setValue(i);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        loadingWindow.setVisible(false);
    }

    public static void mostrarMenuInicio() {
        JFrame ventana = new JFrame("Menú Principal");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(600, 500);
        ventana.setLocationRelativeTo(null);

        // CardLayout para manejar pantallas
        layout = new CardLayout();
        panelPrincipal = new JPanel(layout);

        // Panel del menú principal
        JPanel menuPanel = new JPanel(new BorderLayout(10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel tituloPanel = new JPanel(new GridLayout(2, 1));
        JLabel titulo = new JLabel("Proyecto", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel subtitulo = new JLabel("Estructura de Datos", JLabel.CENTER);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        tituloPanel.add(titulo);
        tituloPanel.add(subtitulo);

        JPanel botonesPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton botonListas = new JButton("Clientes en Espera");
        JButton botonColas = new JButton("Gestion de Pedidos");
        JButton botonPilas = new JButton("Pedidos en Cocina");
        JButton botonSalir = new JButton("Salir");

        botonesPanel.add(botonListas);
        botonesPanel.add(botonColas);
        botonesPanel.add(botonPilas);
        botonesPanel.add(botonSalir);

        menuPanel.add(tituloPanel, BorderLayout.NORTH);
        menuPanel.add(botonesPanel, BorderLayout.CENTER);

        // Paneles de contenido
        JPanel listasPanel = new ClassListas(ventana, layout, panelPrincipal); 
        JPanel colasPanel = new ClassColas(ventana, layout, panelPrincipal);  
        JPanel pilasPanel = new ClassPilas(ventana, layout, panelPrincipal);  

        // Agregar todos los paneles al principal
        panelPrincipal.add(menuPanel, "Menu");
        panelPrincipal.add(listasPanel, "Listas");
        panelPrincipal.add(colasPanel, "Colas");
        panelPrincipal.add(pilasPanel, "Pilas");

        // Acciones de botones
        botonListas.addActionListener(e -> layout.show(panelPrincipal, "Listas"));
        botonColas.addActionListener(e -> layout.show(panelPrincipal, "Colas"));
        botonPilas.addActionListener(e -> layout.show(panelPrincipal, "Pilas"));
        botonSalir.addActionListener(e -> System.exit(0));

        ventana.add(panelPrincipal);
        ventana.setVisible(true);
    }
}
