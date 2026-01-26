# ComuniCare - Memoria Técnica y Documentación del Proyecto
**ComuniCare** es una plataforma móvil nativa desarrollada en Kotlin. Este documento detalla la ingeniería del software aplicada, la arquitectura de datos y la justificación técnica frente a los criterios de evaluación (RA), enfocándose en el rendimiento y la escalabilidad del código.

---

## 1. Arquitectura y Lógica de Negocio (Clean Architecture)
El proyecto sigue una arquitectura por capas estricta para garantizar que la lógica de negocio esté desacoplada de la interfaz.

### 1.1 Patrón MVVM (Model-View-ViewModel)
Utilizamos este patrón para gestionar el ciclo de vida de los datos de forma segura.

* **Model (Repositorio y Datos):** Gestiona la fuente de verdad (Base de datos Room).

* **ViewModel:** Es el intermediario. No conoce la interfaz gráfica. Su función es exponer los datos mediante flujos reactivos (`StateFlow`) y ejecutar la lógica de negocio en hilos secundarios.

**Ejemplo de Lógica Asíncrona (`HelpViewModel.kt`):** 
Utilizamos **Corrutinas** para no bloquear el hilo principal mientras se realizan operaciones pesadas (como leer de la BD).

https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/viewmodel/HelpViewModel.kt#L98-L108

## 2. Persistencia de Datos: Tecnología Room (RA6.d)
Para el almacenamiento local (RA6.d), utilizamos **Room Persistence Library**. Room es una capa de abstracción sobre SQLite que nos permite interactuar con la base de datos utilizando objetos Kotlin (POJOs) en lugar de escribir SQL crudo manualmente, lo que reduce errores en tiempo de compilación.

### ¿Cómo funciona Room internamente en este proyecto?

### A. Entidades (La estructura de la tabla)

Definimos las tablas como clases de datos (`data class`) anotadas con `@Entity`. Room convierte automáticamente las propiedades de la clase en columnas de la base de datos.

**Archivo:** `UserEntity.kt`

https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/data/local/entity/UserEntity.kt#L8-L43

### B. DAO (Data Access Object)

Es la interfaz donde definimos las operaciones. Room verifica en tiempo de compilación que las consultas SQL sean correctas.

**Archivo:** `UserDao.kt`

https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/data/local/dao/UserDao.kt#L6-L19

## 3. RA5 – Informes y Análisis (Criterios FFOE)

Hemos desarrollado un módulo de informes que procesa los datos almacenados en Room para generar estadísticas de impacto social.

### RA5.f - Herramientas de generación (Canvas API)
Para la visualización, utilizamos la API nativa de gráficos **Canvas**. Esto nos permite dibujar gráficos circulares y de barras calculando matemáticamente los ángulos y coordenadas, sin depender de librerías externas que aumenten el peso de la app.

### RA5.g - Modificación del código y Cálculos
El informe no es estático. El sistema realiza cálculos matemáticos sobre los datos crudos obtenidos del DAO.

**Lógica de cálculo en `ReportsScreen.kt`:**

https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ReportsScreen.kt#L302-L329

### RA5.h - Integración
La pantalla de informes es parte integral del flujo de navegación de la aplicación, accesible para el perfil de Administrador.

![INSERTAR CAPTURA AQUÍ: Pantalla de la app mostrando los gráficos estadísticos](https://github.com/PabloOstenero/ComuniCare/blob/main/capturas/Pantalla%20de%20estad%C3%ADsticas.jpeg)

## 4. RA7 – Distribución y Despliegue (GitHub Releases)

La estrategia de distribución se centra en la seguridad y la accesibilidad mediante repositorios públicos.

### RA7.c - Paquete desde el Entorno (Android Studio)

En lugar de automatizar la firma en Gradle (lo cual podría exponer contraseñas en texto plano), utilizamos las herramientas integradas del entorno de desarrollo (**Build > Generate Signed Bundle / APK**).

* Esto garantiza que el binario final (APK) se compile en modo `release`, eliminando logs de depuración y optimizando el código.

### RA7.e - Firma Digital Segura
La aplicación se firma digitalmente utilizando un almacén de claves (`Keystore`) privado gestionado manualmente durante la compilación.

* Esto asegura la integridad de la aplicación: el APK generado no puede ser modificado por terceros sin invalidar la firma.

### RA7.h - Canales de Distribución (GitHub Releases)
Utilizamos **GitHub** como canal de distribución profesional.

1. El código fuente se versiona con Git.

2. Se crea un **Release** (Lanzamiento) etiquetado (ej. `v1.0.0`).

3. El APK firmado se sube como "Asset" (binario adjunto) en el release, permitiendo a los usuarios descargar la versión estable directamente desde el repositorio oficial del proyecto.

## 5. RA8 – Calidad y Pruebas

### RA8.c - Pruebas de Regresión (Unit Testing)

Utilizamos **JUnit** para probar la lógica aislada. Validamos que las funciones matemáticas y de transformación de datos funcionen como se espera antes de desplegar.

**Ejemplo (`HelpViewModelTest.kt`):**

https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/test/java/com/example/comunicare/HelpViewModelTest.kt#L52-L74

### RA8.d - Pruebas de Estrés y Rendimiento

Para manejar grandes volúmenes de datos (ej. 5000 voluntarios), utilizamos técnicas de **virtualización de listas** (Lazy Loading).

* **Funcionamiento:** El sistema solo mantiene en memoria los elementos visibles en la pantalla. A medida que el usuario hace scroll, las celdas que salen de la pantalla se reciclan para mostrar los nuevos datos. Esto evita el desbordamiento de memoria (OutOfMemoryError).

## 6. Tecnologías NUI (RA2)

Implementamos tecnologías de Interacción Natural (NUI) para mejorar la accesibilidad mediante hardware del dispositivo.

### RA2.c - Reconocimiento de Voz (Speech-to-Text)

Utilizamos el `Intent` de reconocimiento de voz de Android para permitir la entrada de datos sin teclado.

### Implementación técnica:

1. Se lanza un `Intent` con la acción `ACTION_RECOGNIZE_SPEECH`.

2. El sistema operativo procesa el audio y devuelve una lista de posibles textos.

3. La app captura el resultado y rellena los campos automáticamente.

https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt#L76-L86

## Video Explicativo del Funcionamiento
A continuación, se adjunta un video demostrativo cubriendo:

1. Registro de usuario y persistencia en Room.

2. Uso del reconocimiento de voz para crear una alerta.

3. Generación de informes gráficos.

[Video mostrando la app](https://github.com/PabloOstenero/ComuniCare/blob/main/capturas/video_app.mp4)
 
