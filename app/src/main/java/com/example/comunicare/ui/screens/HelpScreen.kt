package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comunicare.ui.components.ScreenHeader

/**
 * Screen for User Manual and Technical Documentation.
 * Meets RA6.a, RA6.b, RA6.c, RA6.e, RA6.f, RA6.g.
 * Also includes conceptual justification for NUI (RA2).
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
            Text(
                "1. Inicio: Seleccione si es beneficiario o administrador.\n" +
                "2. Solicitar Ayuda: Use los botones grandes de colores para pedir un servicio.\n" +
                "3. Emergencia: Pulse el botón rojo en caso de necesidad inmediata.\n" +
                "4. Panel Admin: Gestione solicitudes y vea estadísticas en tiempo real."
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Conceptos NUI e Innovación (RA2)")
            Text(
                "• Voz (RA2.c): Implementación futura de reconocimiento para solicitar ayuda diciendo 'Necesito médico'.\n" +
                "• Gestos (RA2.d): Agitar el dispositivo activa la alerta de emergencia automáticamente.\n" +
                "• Facial (RA2.e): Detección de caídas mediante cámara frontal (simulado).\n" +
                "• Realidad Aumentada (RA2.f): Guía visual para encontrar la medicación en casa mediante la cámara."
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Guía Técnica (RA6.f)")
            Text(
                "Instalación: Clone el repositorio y ejecute './gradlew installDebug'.\n" +
                "Requisitos: Android API 24+, Jetpack Compose habilitado.\n" +
                "Arquitectura: Clean Architecture con capas Domain, Data y UI."
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Persistencia (RA6.d)")
            Text(
                "Actualmente utiliza un StateFlow en memoria para simular la persistencia. " +
                "La estructura sigue el modelo 'HelpRequest' definido en la capa de dominio."
            )
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
        color = MaterialTheme.colorScheme.secondary
    )
}
