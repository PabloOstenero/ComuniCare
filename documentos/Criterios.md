# ComuniCare - Evaluación de Criterios

## RA1 – Análisis y Creación de Interfaces

### RA1.a Analiza herramientas y librerías: 

Se ha seleccionado **Jetpack Compose** como motor de UI declarativa (ref: [`build.gradle.kts`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/build.gradle.kts)). Para la persistencia se utiliza Room y para la gestión de imágenes Coil. La arquitectura se basa en **Clean Architecture** con **UseCases** (ej: [`AddHelpRequestUseCase.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/domain/use_case/AddHelpRequestUseCase.kt)) para desacoplar la lógica de la interfaz.

### RA1.b Crea interfaz gráfica: 

La app dispone de un ecosistema completo de pantallas integradas: [`LoginScreen`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/LoginScreen.kt), [`RegisterScreen`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/RegisterScreen.kt), [`BeneficiaryHomeScreen`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt), [`AdminDashboardScreen`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/AdminDashboardScreen.kt), [`ChatScreen`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ChatScreen.kt) y [`ReportsScreen`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ReportsScreen.kt), todas comunicadas mediante un `NavHost` centralizado en [`MainActivity.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/MainActivity.kt).

### RA1.c Uso de layouts y posicionamiento: 

Implementación profesional de Scaffold para la estructura, **LazyColumn** para el renderizado eficiente de listas (en [`AdminDashboardScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/AdminDashboardScreen.kt)) y **Column/Row** con pesos (`weight`) para un diseño adaptativo.

### RA1.d Personalización de componentes: 

Creación de componentes a medida como el **SOSButton** y los **CategoryButton** en [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt), que utilizan elevaciones, formas redondeadas y colores semánticos para maximizar la accesibilidad.

### RA1.e Análisis del código: 

Aplicación estricta del patrón **MVVM**. El [`HelpViewModel.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/viewmodel/HelpViewModel.kt) centraliza el estado global usando `StateFlow`, asegurando que la UI sea puramente observacional y reactiva.

### RA1.f Modificación del código: 

Se ha personalizado el comportamiento de la `HelpRequestCard` para que el `status` y el `type` modifiquen dinámicamente el color de fondo y el icono, mejorando la respuesta cognitiva del usuario.

### RA1.g Asociación de eventos: 

Uso del patrón **State Hoisting**. Los clics en la UI disparan lambdas que ejecutan UseCases en el ViewModel, actualizando la base de datos de forma atómica.

### RA1.h App integrada: 

El proyecto es una solución robusta donde la persistencia de Room, la navegación y el hardware (Voz/Cámara) están totalmente sincronizados, permitiendo un flujo de usuario desde el registro hasta la resolución de emergencias.

## RA2 – Interfaces Naturales de Usuario

### RA2.a Herramientas NUI: 

Uso de la API nativa de Android `RecognizerIntent` para voz y los sensores de cámara y micrófono para comunicación multimedia en el chat.

### RA2.b Diseño conceptual NUI: 

Filosofía de "teclado cero". Se prioriza la entrada de datos mediante voz para usuarios con dificultades motoras, transformando la voz en solicitudes estructuradas en la base de datos.

### RA2.c Interacción por voz: 

Implementado en [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt). El `speechLauncher` captura el audio y el `HelpViewModel.processVoiceCommand()` analiza palabras clave como "ayuda" o "comida" para crear registros automáticos.

### RA2.d Interacción por gesto: 

Uso de gestos estándar de Android y optimización de áreas táctiles en `HelpRequestCard` para facilitar la gestión mediante toques simples y precisos.

### RA2.e Detección facial/corporal:

Se propone para futuras versiones integrar **BiometricPrompt** para que el beneficiario acceda a la app mediante reconocimiento facial, simplificando el login.

### RA2.f Realidad aumentada: 

Se plantea el uso de **ARCore** para que el voluntario visualice flechas de dirección en la cámara para localizar exactamente el domicilio del beneficiario en situaciones críticas.

## RA3 – Componentes Reutilizables

### RA3.a Herramientas de componentes: 

Se utiliza la potencia de las anotaciones `@Composable` y el motor de previsualización de Android Studio. Esto permite verificar la adaptabilidad de cada botón o tarjeta en modo claro, modo oscuro y diferentes escalas de fuente antes de integrarlos en la app.

### RA3.b Componentes reutilizables: 

El archivo [`CommonComponents.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/components/CommonComponents.kt) contiene la librería base: `AccessibleButton`, `HelpRequestCard`, `StatusBadge` y `ScreenHeader`, inyectados en toda la app.

### RA3.c Parámetros y defaults: 

Cada componente modular define valores por defecto inteligentes. Si un desarrollador olvida pasar un color o un icono, el componente utiliza una configuración de seguridad que garantiza que el elemento siga siendo visible y usable, evitando crasheos visuales.

### RA3.d Eventos en componentes: 

Los componentes son "Stateless" (sin estado interno). Reciben las acciones del usuario y las reenvían mediante lambdas al contenedor padre. Esto garantiza que la lógica de "qué hace el botón" esté centralizada en el ViewModel y no dispersa por la UI.

### RA3.f Documentación: 

Se ha seguido el estándar de **KDoc** para documentar la API de cada componente. Cada función describe detalladamente qué estados representa y qué efectos secundarios (como llamadas a base de datos) dispara su interacción.

### RA3.h Integración en la app: 

Existe una integración absoluta: todas las pantallas de ComuniCare están construidas ensamblando estas piezas modulares. Esto permite que un cambio estético en el archivo de componentes se propague instantáneamente por toda la aplicación.

## RA4 – Estándares de Usabilidad

### RA4.a Estándares: 

Cumplimiento estricto de las guías **Material Design 3**, asegurando que los componentes táctiles tengan un tamaño mínimo para evitar errores de pulsación.

### RA4.b Valoración de estándares: 

Se ha priorizado el estándar de Accesibilidad para mayores. Para esto se han usado fuentes grandes y un alto contraste para asegurar que el texto sea legible para usuarios con cataratas o visión reducida.

### RA4.c Menús: 

Uso de un menú lateral (**Navigation Drawer**), en lugar de menús de desbordamiento complejos. El acceso a este menú se centraliza en el componente reutilizable ScreenHeader, que contiene la acción onOpenMenu. Esta decisión de diseño garantiza que las opciones de navegación globales (como ir a "Informes" o "Cerrar Sesión") estén siempre accesibles pero sin saturar la interfaz principal. El menú es contextual, mostrando diferentes opciones según el rol del usuario (Beneficiario o Administrador), cumpliendo con el criterio de un menú funcional y bien integrado. 

### RA4.d Distribución de acciones: 

La distribución de acciones es clara y usable. En [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt), las acciones se agrupan por prioridad: las solicitudes comunes (`CategoryButton`) están en la parte superior, mientras que la acción crítica de **EMERGENCIA** ocupa todo el ancho de la pantalla y se aísla físicamente del resto para evitar pulsaciones accidentales, aplicando principios de psicología del diseño.

### RA4.e Distribución de controles: 

Distribución ergonómica; el botón de voz (`FloatingActionButton`) está en la zona de fácil alcance del pulgar.

### RA4.f Elección de controles: 

Uso de selectores (`RadioButton`), interruptores visuales (`VisibilityToggle` en password) y botones de gran superficie táctil.

### RA4.g Diseño visual: 

Estética limpia, uso de colores pastel para evitar fatiga visual y fuentes "Sans Serif" legibles. La elevación (`shadowElevation`) en los `CategoryButton` de [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt) mejora la jerarquía visual.

### RA4.h Claridad de mensajes: 

Los diálogos informan al usuario de qué está pasando (ej: "Ayuda enviada correctamente"). El `SnackbarHost` en [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt) proporciona feedback inmediato al procesar un comando de voz.

### RA4.i Pruebas usabilidad: 

Verificación de flujos cortos; el usuario puede pedir ayuda médica en menos de 3 clics o mediante un solo comando de voz.

### RA4.j Evaluación en dispositivos: 

Probado en múltiples resoluciones y en el dispositivo real **Vivo V50** para garantizar que los componentes no se solapen.

## RA5 – Informes y Análisis

### RA5.a Establece estructura: 

Pantalla [`ReportsScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ReportsScreen.kt) organizada con KPIs (indicadores clave), filtros y visualizaciones gráficas.

### RA5.b Genera informes: 

El sistema extrae datos de [`HelpRequestDao`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/data/local/dao/HelpRequestDao.kt) y genera un "Certificado de Gestión" dinámico mediante un `AlertDialog` personalizado.

### RA5.c Filtros: 

Implementación de una `ScrollableTabRow` que permite filtrar todas las estadísticas por tipo de ayuda (`HelpType`).

### RA5.d Cálculos y totales: 

El ViewModel procesa los datos de Room usando operadores de colección (`count`, `distinct`, `mapNotNull`) para calcular tasas de resolución y voluntarios activos.

### RA5.e Incluye gráficos generados a partir de los datos: 

Visualización profesional mediante la **API Canvas** de Android. Se dibujan manualmente gráficos de distribución y proporciones (PieCharts/BarCharts) basados en los datos reales de la base de datos.

### RA5.f Herramientas para informes: 

Uso de la potencia de cálculo de los procesadores modernos para procesar miles de registros en milisegundos y dibujarlos de forma fluida en pantalla.

### RA5.g Modificación del código: 

El motor de dibujo (`Canvas`) ha sido personalizado para que las barras cambien de color dinámicamente según el estado de la tarea (Verde/Naranja/Rojo).

### RA5.h App con informes integrados: 

El sistema de informes es una sección nativa de la app, permitiendo que el administrador tome decisiones informadas sin necesidad de exportar datos a herramientas externas.

## RA6 – Documentación y Ayuda

### RA6.a Identifica sistemas de ayuda: 

El proyecto integra un sistema de ayuda nativo centralizado en la clase [`HelpScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/HelpScreen.kt). Se ha optado por una solución "**In-App Help**" mediante componentes de Jetpack Compose, lo que garantiza que la documentación técnica y de usuario sea reactiva y comparta el mismo tema visual (colores y tipografía) que el resto de la aplicación, eliminando la dependencia de visualizadores de PDF externos o navegadores web que podrían fragmentar la experiencia del usuario senior.

### RA6.b Genera ayudas: 

Se han implementado ayudas en formatos visuales de lectura rápida. Destaca el uso de Diálogos de Alerta (`AlertDialog`) y Tarjetas Informativas (`Card`). Un ejemplo real se encuentra en [`ReportsScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ReportsScreen.kt), donde se utiliza un diálogo detallado ("Certificado de Gestión") que actúa como un informe de impacto individual, proporcionando al administrador una guía de referencia clara sobre sus métricas de rendimiento y logros en un formato digital estándar y profesional.

### RA6.c Ayuda sensible al contexto: 

La ayuda está integrada de forma contextual a través del componente `ScreenHeader`. En pantallas críticas como [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt), la cabecera recibe un título específico ("Mi Ayuda") y un callback de menú que permite al usuario acceder a instrucciones relevantes para esa vista. Además, se utiliza el SnackbarHost para proporcionar ayuda contextual inmediata (feedback) cuando el usuario utiliza el reconocimiento de voz, guiándole sobre si el comando ha sido procesado correctamente.

### RA6.d Documenta la estructura de la información persistente: 

La estructura de persistencia basada en **Room** está documentada mediante el uso de anotaciones y una arquitectura clara. En el [`build.gradle.kts`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/build.gradle.kts) se observa el uso de `libs.plugins.google.devtools.ksp`, lo que indica un preprocesamiento de la estructura de datos. Las entidades como `HelpRequest` y los tipos como `HelpType` y `RequestStatus` actúan como documentación viva del esquema de la base de datos, definiendo claramente las relaciones y estados de la información.

### RA6.e Manual de usuario y guía de referencia: 

La aplicación actúa como su propia guía de referencia mediante etiquetas descriptivas (`contentDescription`) en todos sus iconos (Micrófono, Informes, Chat). En la [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt), el uso de `CategoryButton` con etiquetas claras ("Comida", "Salud", "Paseo") sirve como manual visual intuitivo para el usuario mayor, reduciendo la curva de aprendizaje al mínimo.

### RA6.f Manual técnico de instalación/configuración: 

[Manual técnico]()

### RA6.g Confecciona tutoriales: 

Se ha confeccionado un flujo de aprendizaje implícito y reactivo. En [`BeneficiaryHomeScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/BeneficiaryHomeScreen.kt), el botón flotante (`FloatingActionButton`) de voz no solo activa el hardware, sino que lanza un diálogo visual (`RecognizerIntent.EXTRA_PROMPT`) con el texto: "¿En qué podemos ayudarte hoy?". Este mensaje actúa como un tutorial en tiempo real que indica al usuario la acción esperada, mientras que el `SnackbarHost` proporciona feedback instructivo sobre el procesamiento de los comandos de voz.

## RA7 – Distribución de Aplicaciones

### RA7.a Empaquetado: 

El proyecto está configurado para la generación de paquetes **APK** y **Android App Bundles (AAB)**. En el archivo [`build.gradle.kts`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/build.gradle.kts), se define el bloque `buildTypes` con una variante de `release` preparada para la optimización. Se utiliza un sistema de versionado profesional mediante `versionCode 1` y `versionName "1.0"`, asegurando que cada paquete distribuido tenga una trazabilidad clara dentro del ciclo de vida del software.

### RA7.b Personalización: 

La personalización se gestiona a través del `namespace` ("com.example.comunicare") y la configuración del `applicationId`. Estos identificadores garantizan que el instalador reconozca la marca única de la aplicación en el sistema operativo, permitiendo que el icono oficial y el nombre del proyecto se integren correctamente en el lanzador del dispositivo del usuario tras la instalación.

### RA7.c Paquete desde el entorno: 

El proceso de empaquetado está integrado en el entorno de Android Studio mediante el motor de **Gradle**. Se han configurado opciones de compatibilidad en `compileOptions` para asegurar que el paquete generado sea estable y ejecutable en cualquier dispositivo que cumpla con el `minSdk 24`, aprovechando las capacidades del `targetSdk 36` para la optimización de recursos en versiones modernas de Android.

### RA7.d Herramientas externas: 

Se ha definido una estrategia de distribución mediante **GitHub Releases**. Esta herramienta externa permite alojar los binarios (`.apk`) asociados a etiquetas de versión específicas, facilitando que los voluntarios y administradores accedan a la versión más reciente del software de forma segura, manteniendo un registro histórico de cambios (ChangeLog) accesible para toda la comunidad de desarrollo.

### RA7.e Firma digital: 

La aplicación está preparada para el proceso de firma digital necesario para la ejecución en dispositivos reales. Aunque en desarrollo se utiliza la firma de `debug`, el archivo [`build.gradle.kts`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/build.gradle.kts) está estructurado para integrar una `signingConfig` profesional vinculada a un almacén de claves (`.jks`), lo que garantiza la integridad del código y la autenticidad del desarrollador ante futuras actualizaciones del sistema.

### RA7.f Instalación desatendida: 

Gracias al uso de librerías estándar de Google y una arquitectura sin dependencias de sistema propietarias, ComuniCare es compatible con sistemas de **MDM (Mobile Device Management)**. Esto permite realizar instalaciones desatendidas y masivas en flotas de dispositivos para trabajadores sociales o centros de mayores, desplegando la app de forma remota sin necesidad de configuración manual por parte del usuario final.

### RA7.g Desinstalación: 

Se garantiza una desinstalación atómica y limpia. La aplicación utiliza el almacenamiento interno de la app para la base de datos `comunicare_db_v10` y para las **SharedPreferences** (`comunicare_prefs`). Al desinstalar la aplicación, el sistema operativo Android elimina automáticamente estos directorios, asegurando que no queden residuos de datos privados o mensajes de chat en el dispositivo, cumpliendo con las normativas de privacidad.

### RA7.h Canales de distribución: 

Distribución directa del APK firmado a través del repositorio oficial, permitiendo una rápida adopción por parte de la asociación comunitaria.

## RA8 – Calidad y Pruebas Avanzadas

### RA8.a Estrategia de pruebas: 

Se ha diseñado una estrategia de pruebas basada en la Pirámide de Tests. Se han implementado pruebas unitarias en [`HelpViewModelTest.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/test/java/com/example/comunicare/HelpViewModelTest.kt) para validar la lógica de negocio y pruebas de instrumentación en el directorio `androidTest` para validar la interfaz de usuario en Compose. La estrategia prioriza los flujos críticos como el registro, login y la emisión de alertas de emergencia.

### RA8.b Pruebas de integración: 

Se han realizado pruebas de integración exitosas entre el **ViewModel**, los **Casos de Uso** y el **Repositorio**. El archivo [`HelpViewModelTest.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/test/java/com/example/comunicare/HelpViewModelTest.kt) utiliza un `FakeHelpRepository` para verificar que la comunicación entre capas es correcta, asegurando que un cambio de estado en la UI (como asignar una tarea) se propague correctamente hasta la capa de persistencia simulada.

### RA8.c Pruebas de regresión: 

El proyecto cuenta con una suite de pruebas de regresión automatizadas mediante **JUnit** (configurado en `dependencies` del Gradle). Estas pruebas permiten verificar que la introducción de nuevas funciones multimedia en el [`ChatScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ChatScreen.kt) o cambios en los gráficos de [`ReportsScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ReportsScreen.kt) no rompan funcionalidades básicas ya existentes, como la autenticación de usuarios o la recuperación de sesiones persistentes.

### RA8.d Pruebas de volumen/estrés: 

Se ha validado el rendimiento de las listas mediante el componente `LazyColumn` en [`AdminDashboardScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/AdminDashboardScreen.kt). Este componente optimiza el uso de recursos al renderizar solo los elementos visibles. Se han realizado pruebas de volumen inyectando cientos de solicitudes de prueba para asegurar que la aplicación mantenga la fluidez del scroll y la estabilidad de la memoria RAM bajo carga de datos elevada.

### RA8.e Pruebas de seguridad: 

Se han implementado controles de seguridad por software. En [`AdminDashboardScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/AdminDashboardScreen.kt), el código incluye la validación: `val isMine = request.assignedVolunteerId == currentUser?.id`. Esta lógica de seguridad asegura que solo el administrador responsable pueda acceder al chat y finalizar la tarea, evitando accesos no autorizados a la información privada de los beneficiarios.

### RA8.f Uso de recursos: 

Se ha optimizado el uso de hardware crítico. El sistema de notificaciones en [`NotificationHelper.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/utils/NotificationHelper.kt) utiliza canales de importancia alta solo cuando es necesario, y la pantalla [`ChatScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ChatScreen.kt) gestiona el ciclo de vida del micrófono y la cámara de forma eficiente. Además, el uso de `Canvas nativo` en [`ReportsScreen.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/main/java/com/example/comunicare/ui/screens/ReportsScreen.kt) para dibujar gráficos reduce drásticamente el consumo de memoria en comparación con librerías externas pesadas.

### RA8.g Documentación pruebas: 

Los resultados de las pruebas se documentan mediante los informes de ejecución de Gradle y la consola de JUnit en Android Studio. El archivo [`HelpViewModelTest.kt`](https://github.com/PabloOstenero/ComuniCare/blob/main/app/src/test/java/com/example/comunicare/HelpViewModelTest.kt) sirve como registro documental de los casos de prueba exitosos (como `register and login success flow`), detallando las entradas, las acciones y los resultados esperados para cada componente crítico del sistema.

[Archivo con el resultado de las pruebas]()
