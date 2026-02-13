# ComuniCare – Plataforma de Apoyo y Gestión Comunitaria

**ComuniCare** es una aplicación móvil nativa desarrollada para Android mediante Jetpack Compose. Su objetivo principal es fortalecer el tejido social permitiendo que personas en situación de necesidad (beneficiarios) conecten con voluntarios y administradores para recibir asistencia en tareas cotidianas, salud o emergencias.

El proyecto se ha desarrollado como **Proyecto Final del módulo DIN**, aplicando una arquitectura moderna de software, integración con hardware del dispositivo y una gestión robusta de la persistencia de datos.

---

## Objetivos del proyecto
*   **Centralizar la ayuda:** Gestionar solicitudes de asistencia (compras, acompañamiento, salud) en una sola plataforma.
*   **Comunicación Inmediata:** Facilitar el contacto directo mediante un chat multimedia privado.
*   **Diferenciación de Roles:** Control de acceso estricto mediante perfiles de Beneficiario y Administrador.
*   **Accesibilidad NUI:** Implementar reconocimiento de voz para facilitar el uso a personas con movilidad reducida.
*   **Análisis de Impacto:** Generar informes estadísticos dinámicos basados en datos reales de la comunidad.
*   **Arquitectura Profesional:** Aplicar principios de Clean Architecture para garantizar un código mantenible y testable.

---

## Arquitectura y tecnologías
*   **Lenguaje:** Kotlin 2.0.21
*   **Interfaz Gráfica:** Jetpack Compose + Material 3 (Diseño Accesible)
*   **Arquitectura:** MVVM (Model-View-ViewModel) + Principios de Clean Architecture
*   **Persistencia Local:** [Room SQLite](app/src/main/java/com/example/comunicare/data/local/database/AppDatabase.kt)
*   **Gestión de Sesiones:** SharedPreferences (Sesión permanente)
*   **Asincronía:** Kotlin Coroutines + Flow (Reactividad en tiempo real)
*   **Hardware:** Integración con Cámara, Micrófono (Audio nativo) y Speech-to-Text

---

## Funcionalidades principales
*   **Registro Seguro:** Creación de cuenta mediante número de teléfono como clave única.
*   **Gestión de Ayuda:** Creación, asignación y finalización de solicitudes de servicio.
*   **Chat Multimedia:** Comunicación bidireccional con envío de fotos y notas de voz reales.
*   **Botón de Emergencia:** Alerta crítica destacada para asistencia inmediata.
*   **Informes Avanzados:** Panel de estadísticas con gráficos dinámicos (Canvas) y métricas de impacto.
*   **Seguridad:** Recuperación de cuenta mediante "Contacto de Confianza" y cambio de contraseña obligatorio.
*   **Interacción por Voz:** Petición de ayuda mediante comandos de voz naturales.

---

## Roles de usuario
| Rol               | Funcionalidad                                                                              |
|:------------------|:-------------------------------------------------------------------------------------------|
| **Administrador** | Gestión global de la red, asignación de tareas, acceso a informes y chat de soporte.       |
| **Beneficiario**  | Creación de solicitudes, gestión de perfil de seguridad y chat con el voluntario asignado. |

---

## Estructura del repositorio
```text
.
├── app/                      # Código fuente de la aplicación (Kotlin/Compose)
├── documentos/               # Documentación oficial del proyecto
│   ├── Manual_Tecnico.md     # Requisitos, Arquitectura y Persistencia (RA6.f, RA6.d)
│   ├── Pruebas.md            # Informe de resultados y estrategia de tests (RA8)
│   └── Criterios.md          # Informe detallando los criterios 
└── README.md                 # Descripción general y justificación de rúbrica

```

### Manuales y Guías
*   **Manual Técnico:** [Manual Técnico](documentos/Manual_Tecnico.md)
*   **Informe de Pruebas:** [Pruebas](documentos/Pruebas.md)
*   **Informe de los Criterios:** [Criterios](documentos/Criterios.md)
*   **Video Demostrativo:** [Video Explicativo](https://github.com/PabloOstenero/ComuniCare/blob/main/capturas/videos)

---

## Pruebas y Calidad (RA8)
El proyecto incluye pruebas unitarias y de integración centradas en la lógica de negocio y la estabilidad de los flujos de datos, utilizando:
*   **JUnit 4** para validaciones lógicas.
*   **Kotlinx-Coroutines-Test** para la sincronización de flujos asíncronos.
*   **Fake Repositories** para simular la persistencia sin dependencias de hardware.

**Ejecución de tests:**
```bash
./gradlew test
```

---

## Distribución (RA7)
La aplicación genera paquetes optimizados para su despliegue:
*   **Android App Bundle (AAB):** Para distribución profesional en Google Play.
*   **APK Firmado:** Disponible en la sección de [Releases de GitHub](https://github.com/PabloOstenero/ComuniCare/releases).

---

## Mejoras futuras
*   **Sincronización en la Nube:** Migración a Firebase para respaldo externo de datos.
*   **Geolocalización:** Rastreo en tiempo real para servicios de emergencia.

---

## Autor
Proyecto desarrollado por **Pablo Ostenero Reyes**  
Ciclo Formativo de Grado Superior – Desarrollo de Aplicaciones Multiplataforma (2º DAM)  
*Desarrollo de Interfaces (DIN)*
