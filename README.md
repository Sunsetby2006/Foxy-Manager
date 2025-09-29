# Foxy Manager

**Proyecto del certificado de Estructura de Datos 2025**

Bienvenid@ a nuestro proyecto. Para el buen funcionamiento de **Foxy
Manager**, le recomendamos lo siguiente:

-   Descargue el repositorio completo (**Code → Download ZIP**) y
    extraiga la carpeta.  
-   Abra toda la carpeta `proyecto` en Visual Studio Code (esto es
    importante, ya que contiene otras carpetas necesarias para que
    funcione correctamente).  
-   Dentro de `proyecto` encontrará la carpeta `src`. Ingrese en ella
    para localizar el código fuente.  
-   Abra el archivo `Main.java` dentro de `src`.  
-   Ejecute únicamente ese archivo (donde dice "Run" encima de
    `public static void main`) y se abrirá la ventana principal de
    nuestro proyecto.  
-   Adicionalmente, asegúrese de tener **Java** instalado en la
    computadora. Enlace de descarga:
    [Java Download](https://www.java.com/es/download/manual.jsp)  
-   Nota: También puede ejecutar directamente el archivo **FoxyManager.jar**
    para mayor sencillez (Java debe estar instalado).

Si desea conocer más acerca de la visión y los detalles del proyecto,
también contamos con una página web:  
[Foxy Manager en GitHub Pages](https://sunsetby2006.github.io/Foxy-Manager/)

Además, recomendamos leer el documento de Word adjunto en el repositorio,
ahí se encuentra toda la documentación detallada del proyecto.

---

## 📋 Descripción del Proyecto

**Foxy Manager** es un sistema avanzado de gestión de pedidos desarrollado en **Java** que implementa diversas estructuras de datos para optimizar el flujo de trabajo en restaurantes de comida rápida.

El sistema asigna **prioridades inteligentes** a los pedidos basándose en su origen (autoservicio o caja) y la complejidad del combo, garantizando una operación eficiente y una experiencia superior para el cliente.

---

## 🚀 Características Principales

### Versión 1.0
- **Gestión de Pedidos**: creación, visualización y finalización.  
- **Sistema de Prioridades**: cálculo automático de urgencia.  
- **Estructuras de Datos**:
  - Pila para pedidos de autoservicio.  
  - Cola de Prioridad para pedidos normales.  
  - Listas para historial y clientes en espera.  
- **Interfaz Gráfica**: desarrollada con **Java Swing**.  
- **Persistencia de Datos**: almacenamiento en **archivos CSV**.  
- **Panel de Cocina**: visualización de pedidos pendientes ordenados por urgencia.  

### Versión 2.0
- 🔐 **Sistema de Autenticación**: login con roles (Gerente/Empleado).  
- ⏰ **Cronómetro Inteligente**: tiempos estimados con indicadores de color.  
- 🌳 **Árbol Binario de Búsqueda**: búsqueda eficiente en el historial.  
- 🔄 **Actualización en Tiempo Real**: monitor de cambios en archivos CSV.  
- 📊 **Permisos por Rol**: accesos diferenciados según rol.  
- 🔍 **Búsqueda Avanzada**: filtrado por nombre en historial.  
- 🎨 **Interfaz Mejorada**: colores tipo semáforo en pedidos.  

---

## 🛠️ Requisitos del Sistema

| ID | Requisito | Responsable | Estado |
|----|-----------|-------------|--------|
| 1  | Implementar estructuras de datos (pila, cola, lista) con operaciones básicas | Ivan Silverio & Demian Quiroga | ✅ Completado |
| 2  | Desarrollar interfaz de usuario intuitiva con gestión completa | Ivan Silverio & Demian Quiroga | ✅ Completado |
| 3  | Visualización de tareas pendientes ordenadas por urgencia | Demian Quiroga & Diego Medellín | ✅ Completado |
| 4  | Diseño claro, organizado y documentado | Omar Guevara | ✅ Completado |
| 5  | Garantizar eficiencia y lógica estructurada | Omar Guevara | ✅ Completado |
| 6  | Pruebas exhaustivas de todas las funcionalidades | Omar Guevara & Diego Medellín | ✅ Completado |
| 7  | Actualización en tiempo real del historial | Demian Quiroga | ✅ Completado |
| 8  | Optimización de colas de prioridad | Ivan Silverio | ✅ Completado |
| 9  | Implementación de árbol binario en el historial | Omar Guevara | ✅ Completado |
| 10 | Sistema de tiempos estimados con indicadores visuales | Demian Quiroga & Diego Medellín | ✅ Completado |
| 11 | Almacenamiento de datos con HashMaps | Ivan Silverio | ✅ Completado |
| 12 | Pantalla de login con autenticación | Omar Guevara | ✅ Completado |
| 13 | Gestión de colas de prioridad mejorada | Ivan Silverio & Demian Quiroga | ✅ Completado |
| 14 | Métodos de ordenamiento y búsqueda | Omar Guevara | ✅ Completado |
| 15 | Representación con grafos implícitos | Diego Medellín | ✅ Completado |

---

## 🏗️ Arquitectura del Sistema

### Estructuras de Datos Implementadas
- **Pila (Stack):** gestión de pedidos de autoservicio (LIFO).  
- **Cola de Prioridad (PriorityQueue):** ordenamiento por urgencia.  
- **Listas (ArrayList):** historial y clientes en espera.  
- **Árbol Binario:** búsqueda eficiente en historial.  
- **HashMaps:** almacenamiento de precios y configuraciones.  
- **Grafos Implícitos:** modelado de dependencias del sistema.  

### Flujo de Procesamiento
1. **Recepción:** cliente realiza pedido (autoservicio/caja).  
2. **Clasificación:** cálculo de prioridad automática.  
3. **Almacenamiento:** distribución en pila o cola según tipo.  
4. **Procesamiento:** atención por personal de cocina.  
5. **Finalización:** movimiento al historial y registro CSV.  

---

## 👥 Roles de Usuario

### Gerente
- Acceso completo al sistema.  
- Visualización del historial completo.  
- Búsquedas avanzadas en registros.  
- Estadísticas de ventas y desempeño.  

### Empleado
- Gestión de pedidos nuevos.  
- Consulta en cocina.  
- Finalización de pedidos.  
- Visualización de clientes en espera.  

---

## 🌐 Página Web del Proyecto
Para conocer más sobre nuestra visión, metodología y equipo, visite nuestra página oficial:  
[Foxy Manager en GitHub Pages](https://sunsetby2006.github.io/Foxy-Manager/)

---

## 📊 Evidencias y Documentación
- **Código Fuente:** [Repositorio GitHub](https://github.com/sunsetby2006/Foxy-Manager)  
- **Documentación Completa:** Archivo PDF en el repositorio.  
- **Diagramas y Diseños:** [Canva Whiteboard 1](https://www.canva.com/design/DAGcYSFTWug/oogIERxGGY-7id__t5AAqA/view?utm_content=DAGcYSFTWug&utm_campaign=designshare&utm_medium=link2&utm_source=uniquelinks&utlId=hee2dd4f859) / [Canva Whiteboard 2](https://www.canva.com/design/DAGzcrnr0lk/QKX5J8MaEQBi_gnUp8RWfQ/view?utm_content=DAGzcrnr0lk&utm_campaign=designshare&utm_medium=link2&utm_source=uniquelinks&utlId=h5dab3a6f9c)  
- **Presentación del Proyecto:** [Ver en Canva](https://www.canva.com/design/DAG0T0ljkSw/a_Jlok2IhXgwP8JXdDlEZA/view?utm_content=DAG0T0ljkSw&utm_campaign=designshare&utm_medium=link2&utm_source=uniquelinks&utlId=h329f0a9a15)  
- Visita la [página oficial de Java](https://www.java.com/es/download/manual.jsp) para descargarlo.



---

## 👨‍💻 Equipo de Desarrollo

| Rol | Integrante | Responsabilidades |
|-----|------------|-------------------|
| Arquitecto y Web | Omar Fernando Guevara Cavazos | Arquitectura, web, árbol binario, login |
| Desarrollador | Iván Gerardo Tenorio Silverio | Estructuras, colas de prioridad, HashMaps |
| Analista y Escritor técnico | Diego Alejandro Medellín Méndez | Requisitos, documentación, grafos, pruebas |
| Desarrollador | Demian Kalil Quiroga Suárez | Pedidos, actualización en tiempo real, cronómetros |

---

## 🎯 Conclusión

**Foxy Manager** representa la aplicación práctica de estructuras de datos en un contexto empresarial real.  

Este proyecto no solo fortaleció nuestras habilidades técnicas en **Java, GitHub y desarrollo de software**, sino que también demostró la importancia del **trabajo colaborativo** y la **planificación estratégica** en el desarrollo de soluciones tecnológicas innovadoras.  

El sistema está diseñado para escalar y adaptarse a las necesidades cambiantes de los restaurantes, ofreciendo una plataforma robusta y eficiente para la gestión moderna de establecimientos de comida rápida.
