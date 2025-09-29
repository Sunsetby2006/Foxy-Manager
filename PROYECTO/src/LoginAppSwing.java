//Login y verificacion de credenciales
//Importaciones necesarias
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

// DIVISIÓN 1.1: GESTIÓN DE USUARIOS Y SESIONES
public class LoginAppSwing {
    private StringBuilder userInput = new StringBuilder();
    private StringBuilder passwordInput = new StringBuilder();
    private boolean enteringUser = true;
    private JTextField userField;
    private JPasswordField passField;
    private JRadioButton userToggle, passToggle;
    private Map<String, User> usersMap = new HashMap<>();
    private final String USERS_FILE = "PROYECTO/data/logs/users.csv";

    // DIVISIÓN 1.1.1: ESTRUCTURA DE DATOS DE USUARIO
    //Creamos la clase usuario y con eso iniciamos las variables para el login
    public static class User {
        String usuario, rol, contraseña, nombre;
        public User(String usuario, String rol, String contraseña, String nombre) {
            this.usuario = usuario;
            this.rol = rol;
            this.contraseña = contraseña;
            this.nombre = nombre;
        }
    }

    // DIVISIÓN 1.1.2: GESTIÓN DE SESIÓN ACTIVA
    public static class UserSession {
        public static String currentUser, currentRole, currentName;
    }

    //Llamamos los metodos para iniciar la ventana y abrir el archivo con los users
    public LoginAppSwing() {
        cargarUsuariosDesdeCSV();
        inicializarInterfaz();
    }

    // DIVISIÓN 1.2: CARGA DE DATOS DESDE CSV
    private void cargarUsuariosDesdeCSV() { //Se cargan todos los datos de todas las lineas del .csv, cada coma es una variable que se guarda
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (primeraLinea) { primeraLinea = false; continue; }
                String[] datos = linea.split(",");
                if (datos.length >= 4) {
                    String usuario = datos[0].trim();
                    String rol = datos[1].trim();
                    String contraseña = datos[2].trim();
                    String nombre = datos[3].trim();
                    usersMap.put(usuario, new User(usuario, rol, contraseña, nombre));
                }
            }
        } catch (Exception e) { //Si se borra o no se descarga la carpeta y/o archivo csv, manda este error
            JOptionPane.showMessageDialog(null, 
                "Error al cargar usuarios.\nVerifique que el archivo PROYECTO/data/logs/users.csv exista.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // DIVISIÓN 1.3: INTERFAZ GRÁFICA DEL LOGIN
    private void inicializarInterfaz() {
        JFrame frame = new JFrame("FOXY MANAGER");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 500);
        frame.setLayout(new BorderLayout());

        // Panel superior
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(4, 1, 5, 5));
        JLabel userLabel = new JLabel("INGRESE USUARIO:");
        userField = new JTextField();
        userField.setEditable(false);
        JLabel passLabel = new JLabel("CONTRASEÑA:");
        passField = new JPasswordField();
        passField.setEditable(false);
        topPanel.add(userLabel); topPanel.add(userField);
        topPanel.add(passLabel); topPanel.add(passField);

        // Panel de cambio de input (usuario-contraseña)
        JPanel togglePanel = new JPanel();
        userToggle = new JRadioButton("Usuario", true);
        passToggle = new JRadioButton("Contraseña");
        ButtonGroup toggleGroup = new ButtonGroup();
        toggleGroup.add(userToggle); toggleGroup.add(passToggle);
        togglePanel.add(userToggle); togglePanel.add(passToggle);
        userToggle.addActionListener(e -> enteringUser = true);
        passToggle.addActionListener(e -> enteringUser = false);

        // Teclado numérico
        JPanel keypad = new JPanel();
        keypad.setLayout(new GridLayout(4, 3, 5, 5));
        for (int i = 1; i <= 9; i++) {
            int digit = i;
            JButton btn = new JButton(String.valueOf(digit));
            btn.setPreferredSize(new Dimension(60, 60));
            btn.addActionListener(e -> appendDigit(digit));
            keypad.add(btn);
        }
        JButton delBtn = new JButton("DEL"); delBtn.addActionListener(e -> deleteLast()); keypad.add(delBtn);
        JButton zeroBtn = new JButton("0"); zeroBtn.addActionListener(e -> appendDigit(0)); keypad.add(zeroBtn);
        JButton okBtn = new JButton("OK"); okBtn.addActionListener(e -> validateLogin()); keypad.add(okBtn); //Equivale a dar enter

        // Layout principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(togglePanel, BorderLayout.CENTER);
        mainPanel.add(keypad, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // DIVISIÓN 1.4: MANEJO DE ENTRADA DE DATOS
    private void appendDigit(int digit) { //Si se selecciona la opcion usuario, el teclado numerico escribe en usuario, de lo contrario se escribe en contraseña
        if (enteringUser) {
            userInput.append(digit);
            userField.setText(userInput.toString());
        } else {
            passwordInput.append(digit);
            passField.setText(passwordInput.toString());
        }
    }

    private void deleteLast() { //Si se selecciona la opcion del, se borra el ultimo digito de la opcion seleccionada (usuario-contraseña)
        if (enteringUser && userInput.length() > 0) {
            userInput.deleteCharAt(userInput.length() - 1);
            userField.setText(userInput.toString());
        } else if (!enteringUser && passwordInput.length() > 0) {
            passwordInput.deleteCharAt(passwordInput.length() - 1);
            passField.setText(passwordInput.toString());
        }
    }

    // DIVISIÓN 1.5: VALIDACIÓN DE CREDENCIALES
    private void validateLogin() { //1ero se agarran los inputs dados
        String usuarioIngresado = userField.getText();
        String contraseñaIngresada = new String(passField.getPassword());
        
        if (usuarioIngresado.isEmpty() || contraseñaIngresada.isEmpty()) { //Esto pasa si no se ingreso nada xd
            JOptionPane.showMessageDialog(null, "Por favor ingrese usuario y contraseña.", 
                "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User usuario = usersMap.get(usuarioIngresado); //Se busca el usuario ingresado en el .csv
        //Verificamos que sea valido y coincida, si es correcto se da acceso, de lo contrario no te deja acceder a la pantalla principal
        if (usuario != null && usuario.contraseña.equals(contraseñaIngresada)) {
            UserSession.currentUser = usuario.usuario;
            UserSession.currentRole = usuario.rol;
            UserSession.currentName = usuario.nombre;
            
            String mensaje = String.format("¡Bienvenid@ %s!\nRol: %s", usuario.nombre, usuario.rol); //Te da una bienvenida custom dependiendo tu usuario
            JOptionPane.showMessageDialog(null, mensaje, "Acceso concedido", JOptionPane.INFORMATION_MESSAGE);
            
            SwingUtilities.getWindowAncestor(userField).dispose();
            Main.mostrarMenuInicio();
        } else {
            JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos.", 
                "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
        
        userInput.setLength(0); passwordInput.setLength(0);
        userField.setText(""); passField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginAppSwing::new);
    }
}