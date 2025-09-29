//Actualizar datos
import java.io.IOException;
import java.nio.file.*;
import javax.swing.*;

public class ClaseActualizar extends Thread {
    private Path carpeta;
    private Runnable refrescarCallback;

    // Variables para implementar el Debounce (Anti-rebote):
    // Esto evita que el callback se ejecute múltiples veces por un único cambio de archivo.
    private long lastUpdateTime = 0;
    // Mínimo de tiempo (en milisegundos) que debe pasar entre dos llamadas al callback.
    private static final long DEBOUNCE_TIME_MS = 100;

    public ClaseActualizar(String rutaCarpeta, Runnable refrescarCallback) {
        this.carpeta = Paths.get(rutaCarpeta);
        this.refrescarCallback = refrescarCallback;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = carpeta.getFileSystem().newWatchService();
            // Registra para el evento de MODIFICACIÓN
            carpeta.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (!Thread.currentThread().isInterrupted()) {
                // Bloquea hasta que haya un evento o el hilo sea interrumpido
                WatchKey key = watchService.take();

                // 1. Aplica Debounce: verifica si ha pasado el tiempo mínimo desde la última actualización exitosa.
                if (System.currentTimeMillis() - lastUpdateTime > DEBOUNCE_TIME_MS) {

                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path archivo = (Path) event.context();

                        // Si el evento es para un archivo CSV...
                        if (archivo != null && archivo.toString().endsWith(".csv")) {
                            // Llama al callback de actualización en el hilo de Swing (la interfaz)
                            SwingUtilities.invokeLater(refrescarCallback);

                            lastUpdateTime = System.currentTimeMillis(); // Reinicia el contador de tiempo

                            // 2. Clave para la corrección: Rompe el bucle interno.
                            // Solo se ejecuta una vez por ráfaga de eventos del WatchKey.
                            break;
                        }
                    }
                } else {
                    // Si no ha pasado el tiempo de debounce, consume los eventos sin llamar al callback
                    key.pollEvents();
                }

                // 3. Reinicia la llave para seguir monitoreando la carpeta
                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("El WatchKey ya no es válido. Saliendo del monitor de archivos.");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            // Se espera una InterruptedException cuando se llama a Thread.interrupt() al salir
            if (!(e instanceof InterruptedException)) {
                System.err.println("Error en ClaseActualizar: " + e.getMessage());
            }
        }
    }
}