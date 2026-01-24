package com.example.comunicare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.domain.model.RequestStatus

/**
 * Botón accesible de gran tamaño diseñado para usuarios con dificultades motoras.
 * @param text Texto visible en el botón.
 * @param onClick Acción a ejecutar al pulsar.
 * @param enabled Estado de habilitación del componente.
 */
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.LightGray
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Tarjeta de solicitud de ayuda con gestión de permisos y estados.
 * RA4.f - Elección de controles óptima para la visualización de datos.
 */
@Composable
fun HelpRequestCard(
    request: HelpRequest,
    isAdmin: Boolean = false,
    isAssignedToMe: Boolean = false,
    onStatusChange: (RequestStatus) -> Unit = {},
    onChatClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (request.type == HelpType.EMERGENCY) 
                Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Traducción del tipo de ayuda (RA4.h)
                val typeDisplay = when(request.type) {
                    HelpType.SHOPPING -> "Comida/Compra"
                    HelpType.ACCOMPANIMENT -> "Paseo/Compañía"
                    HelpType.MEDICATION -> "Salud/Medicina"
                    HelpType.EMERGENCY -> "¡EMERGENCIA!"
                    HelpType.RECOVERY -> "Recuperación de Cuenta"
                    HelpType.OTHER -> "Otros"
                }
                
                Text(text = typeDisplay, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                StatusBadge(request.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "De: ${request.beneficiaryName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = request.description, style = MaterialTheme.typography.bodyMedium)

            if (request.status == RequestStatus.ASSIGNED && !isAssignedToMe && isAdmin) {
                Text(text = "Gestionado por otro administrador", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isAdmin && request.status != RequestStatus.COMPLETED) {
                        if (request.status == RequestStatus.PENDING) {
                            Button(onClick = { onStatusChange(RequestStatus.ASSIGNED) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                                Text("Asignar")
                            }
                        }
                        if (request.status == RequestStatus.ASSIGNED && isAssignedToMe) {
                            Button(onClick = { onStatusChange(RequestStatus.COMPLETED) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                                Text("Finalizar")
                            }
                        }
                    }
                }
                if (onChatClick != null && request.status != RequestStatus.PENDING) {
                    IconButton(onClick = onChatClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "Ir al chat", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: RequestStatus) {
    // Traducción del estado (RA4.h)
    val (text, color) = when (status) {
        RequestStatus.PENDING -> "Pendiente" to Color.Red
        RequestStatus.ASSIGNED -> "En proceso" to Color(0xFFF57C00)
        RequestStatus.COMPLETED -> "Completado" to Color(0xFF388E3C)
    }
    
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp), border = androidx.compose.foundation.BorderStroke(1.dp, color)) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

/**
 * Cabecera estándar de pantalla con soporte para navegación y menús.
 * RA4.c - Menús integrados profesionalmente.
 */
@Composable
fun ScreenHeader(
    title: String,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                }
            } else if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Text(text = title, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
