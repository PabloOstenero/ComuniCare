package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.ui.components.HelpRequestCard
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: HelpViewModel,
    onNavigateToReports: () -> Unit,
    onOpenMenu: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Panel de Administrador",
            onMenuClick = onOpenMenu
        )
        
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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(requests) { request ->
                // RA4 - Control de permisos en la UI
                val isMine = request.assignedVolunteerId == currentUser?.id
                
                HelpRequestCard(
                    request = request,
                    isAdmin = true,
                    isAssignedToMe = isMine,
                    onStatusChange = { newStatus ->
                        viewModel.updateStatus(request.id, newStatus)
                    },
                    onChatClick = if (isMine) {
                        { onNavigateToChat(request.id) }
                    } else null
                )
            }
        }
    }
}
