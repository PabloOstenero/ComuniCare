package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicare.ui.components.ScreenHeader

/**
 * Pantalla de Ayuda y Documentación Técnica.
 * Cumple con los requisitos de la rúbrica:
 * - RA6.a, RA6.b, RA6.c, RA6.e (Manuales y Ayuda Contextual)
 * - RA6.f (Manual Técnico)
 * - RA6.g (Tutoriales)
 * - RA2 (Justificación NUI)
 * - RA6.d (Documentación de Persistencia)
 */
@Composable
fun HelpScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Ayuda y Documentación")

        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("Manual de Usuario (RA6.e)")
            HelpText(
                "• Acceso: Inicie sesión con su usuario y contraseña. El sistema gestiona automáticamente el registro si el usuario no existe.\n" +
                "• Solicitar Ayuda: Los beneficiarios disponen de botones de gran tamaño. El botón rojo envía una alerta de emergencia inmediata a todos los administradores.\n" +
                "• Gestión (Admin): El panel permite asignar tareas pendientes. Una vez asignada, solo ese administrador puede completar la tarea y acceder al chat privado.\n" +
                "• Chat Multimedia Privado: Comunicación directa entre el solicitante y el responsable. Permite enviar fotos (cámara/galería) y grabar mensajes de voz reales.\n" +
                "• Recuperación de Cuenta: Si olvida su contraseña, puede solicitar un código de acceso que será notificado a su 'Contacto de Confianza'."
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Estadísticas e Informes (RA5)")
            HelpText(
                "• Visualización Global: Gráficas dinámicas (Canvas) que muestran la distribución de necesidades por categorías en tiempo real.\n" +
                "• Informe de Gestión Personal: Los administradores pueden generar un reporte de su impacto, detallando solicitudes gestionadas y emergencias atendidas."
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Tecnologías NUI e Innovación (RA2)")
            HelpText(
                "• Interacción por Voz (RA2.c): Botón de micrófono flotante que procesa comandos de lenguaje natural para pedir cualquier tipo de ayuda sin usar las manos.\n" +
                "• Multimedia Hardware: Integración total con la cámara y el micrófono del dispositivo, facilitando la comunicación a usuarios con dificultades de escritura."
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Guía Técnica e Instalación (RA6.f)")
            HelpText(
                "• Requisitos: Dispositivo Android con API 24 o superior.\n" +
                "• Instalación: Compilar mediante Android Studio Jellyfish+ o ejecutar './gradlew installDebug'.\n" +
                "• Arquitectura: Patrón MVVM con Clean Architecture y principios SOLID para garantizar la mantenibilidad.\n" +
                "• Persistencia: Room Database local con gestión de versiones y migraciones de datos segura (v7)."
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Estructura de Datos (RA6.d)")
            HelpText(
                "La persistencia se realiza mediante Room SQLite con las siguientes entidades:\n" +
                "• Users: Almacena credenciales, roles y contactos de confianza.\n" +
                "• HelpRequests: Registro histórico de avisos, estados y responsables.\n" +
                "• ChatMessages: Almacenamiento local de conversaciones multimedia."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun HelpText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
