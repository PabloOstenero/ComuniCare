package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.ui.components.AccessibleButton
import com.example.comunicare.ui.components.HelpRequestCard
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

/**
 * Main screen for the beneficiary user.
 * Prioritizes accessibility with large buttons and clear labels (RA4).
 */
@Composable
fun BeneficiaryHomeScreen(
    viewModel: HelpViewModel,
    onOpenMenu: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val myRequests = requests.filter { it.beneficiaryId == viewModel.currentUser.value?.id }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Mi Ayuda",
            onMenuClick = onOpenMenu
        )
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "¿En qué podemos ayudarte?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryButton("Comida", HelpType.SHOPPING, viewModel, Modifier.weight(1f))
                    CategoryButton("Salud", HelpType.MEDICATION, viewModel, Modifier.weight(1f))
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryButton("Paseo", HelpType.ACCOMPANIMENT, viewModel, Modifier.weight(1f))
                    CategoryButton("Otros", HelpType.OTHER, viewModel, Modifier.weight(1f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                // RA4.d - Critical action (Emergency) highlighted
                AccessibleButton(
                    text = "¡NECESITO AYUDA AHORA!",
                    onClick = { viewModel.sendEmergencyAlert() },
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "Estado de mis avisos:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (myRequests.isEmpty()) {
                item {
                    Text(
                        text = "No tienes solicitudes activas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(myRequests) { request ->
                HelpRequestCard(
                    request = request,
                    onChatClick = { onNavigateToChat(request.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryButton(
    label: String,
    type: HelpType,
    viewModel: HelpViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { viewModel.requestHelp(type, "Solicitud de $label") },
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = label, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
