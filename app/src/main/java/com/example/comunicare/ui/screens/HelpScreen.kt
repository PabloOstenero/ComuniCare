package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicare.domain.model.UserRole
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

/**
 * Pantalla de Ayuda y Manual de Usuario Detallado.
 * Presenta contenido dinámico basado en el rol del usuario actual.
 * Cumple con RA6.a, RA6.b, RA6.c, RA6.e, RA6.f y RA6.g.
 */
@Composable
fun HelpScreen(viewModel: HelpViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val role = currentUser?.role

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Manual de Usuario")

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bienvenido al Centro de Ayuda de ComuniCare",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Usted está viendo el manual específico para el rol de ${if (role == UserRole.ADMIN) "Administrador" else "Beneficiario"}.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (role == UserRole.ADMIN) {
                AdminManual()
            } else {
                BeneficiaryManual()
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun BeneficiaryManual() {
    SectionTitle("1. Solicitar Asistencia")
    HelpText(
        "Utilice los botones de categorías en la pantalla principal para pedir ayuda específica:\n" +
        "• Comida: Para compras de supermercado o alimentación.\n" +
        "• Salud: Para recogida de medicamentos o citas médicas.\n" +
        "• Paseo: Para acompañamiento o paseos por el barrio.\n" +
        "• Otros: Para cualquier otra necesidad no categorizada."
    )

    SectionTitle("2. Uso de la Voz (NUI - RA2.c)")
    HelpText(
        "Si tiene dificultades para escribir, pulse el botón flotante del micrófono. Diga comandos como 'necesito ayuda con la comida' o 'emergencia' para crear una solicitud automáticamente."
    )

    SectionTitle("3. ¡EMERGENCIA!")
    HelpText(
        "El botón rojo de gran tamaño envía una alerta crítica inmediata. Todos los administradores recibirán una notificación prioritaria en sus dispositivos."
    )

    SectionTitle("4. Chat y Seguimiento")
    HelpText(
        "Una vez que un voluntario acepte su solicitud, podrá hablar con él mediante el chat privado. Puede enviar fotos de recetas o tickets y recibir notas de voz."
    )

    SectionTitle("5. Seguridad y Contraseña")
    HelpText(
        "En la sección 'Seguridad', vincule a un administrador de confianza mediante su teléfono. Si olvida su clave, solicite una recuperación; su contacto le dará un código de 4 dígitos para entrar."
    )
}

@Composable
fun AdminManual() {
    SectionTitle("1. Gestión de la Red")
    HelpText(
        "En su panel principal aparecerán todos los avisos de los beneficiarios. Las solicitudes en rojo son EMERGENCIAS que requieren atención inmediata."
    )

    SectionTitle("2. Asignación de Tareas")
    HelpText(
        "Pulse el botón 'Asignar' en una solicitud para hacerse cargo de ella. Solo usted podrá ver el chat y finalizar la tarea una vez asignada."
    )

    SectionTitle("3. Chat Multimedia")
    HelpText(
        "Utilice el chat para coordinar la entrega o asistencia. Puede usar la cámara para enviar fotos de comprobantes y el micrófono para mensajes rápidos de voz."
    )

    SectionTitle("4. Estadísticas e Informes (RA5)")
    HelpText(
        "Acceda al panel de estadísticas para visualizar gráficas dinámicas de la demanda por categorías y generar su informe de impacto personal."
    )

    SectionTitle("5. Validar Accesos")
    HelpText(
        "Si un usuario que le tiene como 'Contacto de Confianza' olvida su clave, usted recibirá una solicitud de recuperación. Al aceptarla, el sistema le enviará automáticamente el código de seguridad al usuario."
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun HelpText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
