# Manual Técnico de Instalación y Configuración - ComuniCare

Este documento detalla los requisitos técnicos, la arquitectura del sistema y los pasos necesarios para el despliegue y mantenimiento de la plataforma **ComuniCare**.

---

## 1. Requisitos del Sistema

### 1.1 Entorno de Desarrollo
*   **IDE:** Android Studio Jellyfish (2023.3.1) o superior.
*   **JDK:** Java 17 o superior.
*   **Versión de Kotlin:** 2.0.21.
*   **Gradle:** Versión 8.7.3.

### 1.2 Requisitos del Dispositivo (Hardware)
*   **Sistema Operativo:** Android 7.0 (API 24) o superior.
*   **Memoria RAM:** Mínimo 2GB (4GB recomendado).
*   **Periféricos necesarios:** 
    *   Cámara frontal/trasera (para envío de imágenes en chat).
    *   Micrófono (para notas de voz y comandos NUI).
    *   Conexión a Internet (para actualizaciones, aunque la persistencia es local).

---

## 2. Arquitectura del Software 

ComuniCare implementa una arquitectura basada en **Clean Architecture** y el patrón de diseño **MVVM (Model-View-ViewModel)**, lo que garantiza el desacoplamiento de la lógica de negocio y la interfaz.

### 2.1 Capas del Proyecto
1.  **Capa Data:** Implementa el acceso a datos mediante **Room Persistence Library**. Contiene las entidades, DAOs y la implementación del repositorio.
2.  **Capa Domain:** Contiene los modelos puros y los **Casos de Uso** (UseCases) que definen las reglas de negocio aisladas.
3.  **Capa UI:** Construida íntegramente con **Jetpack Compose**. Gestiona el estado de la vista mediante el `HelpViewModel`.

---

## 3. Configuración de Persistencia 

La aplicación utiliza una base de datos relacional local gestionada por Room.

### 3.1 Estructura de la Base de Datos
*   **Versión Actual:** 10.
*   **Archivo físico:** `comunicare_db_v10`.
*   **Entidades Principales:**
    *   `users`: Almacena perfiles, números de teléfono (clave única) y roles.
    *   `help_requests`: Registro histórico de todas las solicitudes de ayuda.
    *   `chat_messages`: Almacenamiento local de conversaciones multimedia.

### 3.2 Gestión de Sesiones
Se utiliza la API `SharedPreferences` para persistir el `userId` de la sesión activa, permitiendo que la cuenta permanezca abierta tras el cierre de la aplicación ([RA6.d]).

---

## 4. Guía de Instalación 

1.  **Clonación:** Descargar el código fuente desde el repositorio oficial.
2.  **Sincronización:** Abrir el proyecto en Android Studio y esperar a que finalice el `Gradle Sync`.
3.  **Compilación de Desarrollo:**
    *   Ejecutar el comando `./gradlew assembleDebug` para generar el APK de pruebas.
4.  **Generación de Paquete de Distribución ([RA7.c]):**
    *   Ir a `Build > Generate Signed Bundle / APK`.
    *   Seleccionar **Android App Bundle (AAB)** para optimización de recursos.
    *   Cargar el archivo **Keystore** privado para la firma digital ([RA7.e]).

---

## 5. Tecnologías de Interfaz Natural (NUI) 

*   **Comandos de Voz:** La aplicación utiliza `RecognizerIntent` para procesar audio en tiempo real. Configurado en `BeneficiaryHomeScreen.kt`.
*   **Multimedia:** Se integra la API `MediaRecorder` para la captura de notas de voz en el chat, configurando el formato de salida en `MPEG_4` para alta compresión y calidad.

---

## 6. Desinstalación 
La aplicación permite una desinstalación limpia mediante la gestión estándar de Android. Al desinstalar, el sistema limpia la caché multimedia y la base de datos Room, a menos que el usuario tenga activa la copia de seguridad de Google Drive para la app.
