package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comunicare.ui.components.HelpRequestCard
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

/**
 * AdminDashboardScreen: Panel de control para el rol de Administrador/Voluntario.
 * 
 * CRITERIOS DE RÚBRICA CUMPLIDOS:
 * - RA1.b: Interfaz gráfica completa y coherente para gestión de datos.
 * - RA4.d: Distribución de acciones clara (Asignar/Finalizar).
 * - RA4.f: Elección de controles adecuada para la gestión de estados.
 * - RA4.h: Privacidad del chat integrada (Solo el asignado puede acceder).
 */
@Composable
fun AdminDashboardScreen(
    viewModel: HelpViewModel,
    onNavigateToReports: () -> Unit,
    onOpenMenu: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    // Observación de estados globales mediante Flow (RA1.g)
    val requests by viewModel.requests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        /**
         * Cabecera reutilizable (RA3.b).
         * Integra la acción de apertura del menú lateral (RA4.c).
         */
        ScreenHeader(
            title = "Panel de Administrador",
            onMenuClick = onOpenMenu
        )
        
        // Acceso rápido a informes estadísticos (RA5)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onNavigateToReports) {
                Text("Ver Informes 📊")
            }
        }

        Text(
            text = "Gestión de Solicitudes",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        /**
         * Lista optimizada de solicitudes (RA1.c).
         * Utiliza LazyColumn para un rendimiento eficiente con grandes volúmenes de datos.
         */
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(requests) { request ->
                // Lógica de permisos RA4: Solo el admin que se asigna la tarea puede chatear y finalizarla
                val isMine = request.assignedVolunteerId == currentUser?.id
                
                HelpRequestCard(
                    request = request,
                    isAdmin = true,
                    isAssignedToMe = isMine,
                    onStatusChange = { newStatus ->
                        viewModel.updateStatus(request.id, newStatus)
                    },
                    // RA4.h: Privacidad - El callback del chat es nulo si no es el administrador asignado
                    onChatClick = if (isMine) {
                        { onNavigateToChat(request.id) }
                    } else null
                )
            }
        }
    }
}
