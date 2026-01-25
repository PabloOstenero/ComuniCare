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

```kotlin
@HiltViewModel
class HelpViewModel @Inject constructor(
    private val repository: HelpRepository
) : ViewModel() {

    // Estado observable que contiene los datos listos para consumir
    private val _uiState = MutableStateFlow<HelpUiState>(HelpUiState.Loading)
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    // Lógica de negocio ejecutada en segundo plano (viewModelScope)
    fun loadRequests() {
        viewModelScope.launch {
            try {
                // El repositorio decide si saca datos de Room o de una API
                repository.getAllRequests().collect { requests ->
                    _uiState.value = HelpUiState.Success(requests)
                }
            } catch (e: Exception) {
                _uiState.value = HelpUiState.Error("Error al cargar datos")
            }
        }
    }
}
```
[📸 INSERTAR CAPTURA AQUÍ: Pantalla principal de la app cargando o mostrando la lista de solicitudes]

## 2. Persistencia de Datos: Tecnología Room (RA6.d)
Para el almacenamiento local (RA6.d), utilizamos **Room Persistence Library**. Room es una capa de abstracción sobre SQLite que nos permite interactuar con la base de datos utilizando objetos Kotlin (POJOs) en lugar de escribir SQL crudo manualmente, lo que reduce errores en tiempo de compilación.

### ¿Cómo funciona Room internamente en este proyecto?

### A. Entidades (La estructura de la tabla)

Definimos las tablas como clases de datos (`data class`) anotadas con `@Entity`. Room convierte automáticamente las propiedades de la clase en columnas de la base de datos.

**Archivo:** `UserEntity.kt`

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Clave primaria autogenerada
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "role") val role: String // 'VOLUNTARIO' o 'BENEFICIARIO'
)
```

### B. DAO (Data Access Object)

Es la interfaz donde definimos las operaciones. Room verifica en tiempo de compilación que las consultas SQL sean correctas.

**Archivo:** `UserDao.kt`

```kotlin
@Dao
interface UserDao {
    // Inserción eficiente: Si el usuario ya existe, lo reemplaza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Consulta que retorna un Flow (flujo de datos continuo)
    // Si la BD cambia, este flujo emite el nuevo valor automáticamente
    @Query("SELECT * FROM users WHERE role = 'VOLUNTARIO'")
    fun getVolunteers(): Flow<List<UserEntity>>
}
```

## 3. RA5 – Informes y Análisis (Criterios FFOE)

Hemos desarrollado un módulo de informes que procesa los datos almacenados en Room para generar estadísticas de impacto social.

### RA5.f - Herramientas de generación (Canvas API)
Para la visualización, utilizamos la API nativa de gráficos **Canvas**. Esto nos permite dibujar gráficos circulares y de barras calculando matemáticamente los ángulos y coordenadas, sin depender de librerías externas que aumenten el peso de la app.

### RA5.g - Modificación del código y Cálculos
El informe no es estático. El sistema realiza cálculos matemáticos sobre los datos crudos obtenidos del DAO.

**Lógica de cálculo en `ReportsScreen.kt`:**

```kotlin
// 1. Obtención de datos crudos
val totalRequests = requests.size
val solvedRequests = requests.count { it.status == "COMPLETED" }

// 2. Cálculo matemático para el gráfico (Regla de tres)
// Calculamos el ángulo de barrido (sweepAngle) para el gráfico circular
val successRate = if (totalRequests > 0) (solvedRequests.toFloat() / totalRequests) else 0f
val sweepAngle = successRate * 360f 

// 3. Dibujado dinámico
Canvas(modifier = Modifier.size(200.dp)) {
    drawArc(
        color = Color.Green,
        startAngle = -90f,
        sweepAngle = sweepAngle, // El ángulo depende del cálculo anterior
        useCenter = true
    )
}
```

### RA5.h - Integración
La pantalla de informes es parte integral del flujo de navegación de la aplicación, accesible para el perfil de Administrador.

[📸 INSERTAR CAPTURA AQUÍ: Pantalla de la app mostrando los gráficos estadísticos]

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

```kotlin
@Test
fun `verify efficiency calculation returns correct percentage`() {
    // Datos simulados
    val total = 100
    val completed = 25
    
    // Ejecución de la lógica
    val efficiency = CalculationUtils.calculateEfficiency(total, completed)
    
    // Verificación (Assert)
    assertEquals(25.0f, efficiency)
}
```

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

```kotlin
// Lanzador de actividad para resultado
val speechLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    // Procesamiento del resultado
    val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
    if (spokenText != null) {
        viewModel.onDescriptionChanged(spokenText)
    }
}
```

## Video Explicativo del Funcionamiento
A continuación, se adjunta un video demostrativo cubriendo:

1. Registro de usuario y persistencia en Room.

2. Uso del reconocimiento de voz para crear una alerta.

3. Generación de informes gráficos.

[🎥 INSERTAR VIDEO AQUÍ]
 