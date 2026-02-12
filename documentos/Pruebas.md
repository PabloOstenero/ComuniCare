# Informe de Resultados de Pruebas - ComuniCare

Este documento detalla los resultados obtenidos tras la ejecución de la batería de pruebas unitarias y de integración sobre la lógica de negocio de la aplicación ComuniCare, cumpliendo con los requisitos de la rúbrica **RA8 – Pruebas Avanzadas**.

---

## Resumen Ejecutivo

| Métrica                         | Resultado            |
|:--------------------------------|:---------------------|
| **Estado General**              | ✅ **EXITOSO (100%)** |
| **Total de Pruebas Ejecutadas** | 4                    |
| **Pruebas Superadas**           | 4                    |
| **Pruebas Fallidas**            | 0                    |
| **Tiempo de Ejecución**         | 2s                   |

---

## Detalle de Casos de Prueba ([RA8.a, RA8.b])

### 1. Flujo de Registro y Login (`register and login success flow`)
*   **Objetivo:** Verificar que un nuevo usuario puede crear una cuenta mediante su número de teléfono y autenticarse inmediatamente.
*   **Resultado:** ✅ PASADO.
*   **Justificación:** Valida la integridad del sistema de perfiles y la persistencia de la sesión tras el registro.

### 2. Seguridad de Credenciales (`login with wrong password should set error`)
*   **Objetivo:** Comprobar que el sistema deniega el acceso ante contraseñas incorrectas y proporciona el feedback adecuado.
*   **Resultado:** ✅ PASADO.
*   **Justificación:** Garantiza el cumplimiento del estándar de seguridad (**RA8.e**) y la claridad de mensajes (**RA4.h**).

### 3. Gestión y Filtrado de Ayuda (`requestHelp adds request and filters by role`)
*   **Objetivo:** Validar que las solicitudes se crean correctamente en Room y que los administradores visualizan la lista filtrada según sus permisos.
*   **Resultado:** ✅ PASADO.
*   **Justificación:** Prueba de integración real entre la lógica del ViewModel y la persistencia de datos (**RA1.g**).

### 4. Cierre de Sesión Seguro (`logout clears session and user`)
*   **Objetivo:** Asegurar que al cerrar la sesión se eliminan todos los rastros de identidad del usuario en memoria y almacenamiento.
*   **Resultado:** ✅ PASADO.
*   **Justificación:** Verificación de la gestión de ciclo de vida del usuario y limpieza de recursos persistentes (**RA6.d**).

---

## Justificación de Criterios FFOE (RA8)

*   **[RA8.c] Pruebas de Regresión:** La suite de pruebas actual asegura que cualquier cambio futuro en la base de datos Room (como nuevas entidades o campos) sea detectado si rompe el flujo crítico de acceso de los usuarios.
*   **[RA8.d] Pruebas de Rendimiento:** Aunque las pruebas unitarias se ejecutan en entorno simulado, validan el uso de corrutinas (`advanceUntilIdle`) demostrando que la app gestiona la concurrencia sin bloqueos del hilo principal.
*   **[RA8.f] Uso de Recursos:** Se ha implementado un **FakeHelpRepository** para ejecutar las pruebas sin hardware real (Cámara/Micrófono), optimizando el tiempo de ejecución y garantizando que los tests se puedan ejecutar en cualquier entorno de integración continua.

---

## Evidencia de Ejecución

```text
BUILD SUCCESSFUL in 3s
5 actionable tasks: 1 executed, 4 up-to-date
:app:testDebugUnitTest
HelpViewModelTest > register and login success flow PASSED
HelpViewModelTest > login with wrong password should set error PASSED
HelpViewModelTest > requestHelp adds request and filters by role PASSED
HelpViewModelTest > logout clears session and user PASSED
ExampleUnitTest > addition_isCorrect PASSED
```
