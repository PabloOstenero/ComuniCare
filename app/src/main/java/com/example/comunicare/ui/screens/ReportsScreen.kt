package com.example.comunicare.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

@Composable
fun ReportsScreen(viewModel: HelpViewModel) {
    val allRequests by viewModel.requests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    var showDetailedReport by remember { mutableStateOf(false) }

    val totalGlobal = allRequests.size
    val emergencyGlobal = allRequests.count { it.type == HelpType.EMERGENCY }
    val completedGlobal = allRequests.count { it.status == RequestStatus.COMPLETED }
    val pendingGlobal = allRequests.count { it.status == RequestStatus.PENDING }

    val myAssigned = allRequests.filter { it.assignedVolunteerId == currentUser?.id }
    val myCompleted = myAssigned.count { it.status == RequestStatus.COMPLETED }
    val myEmergenciesManaged = myAssigned.count { it.type == HelpType.EMERGENCY }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(title = "Panel de Estadísticas")
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Resumen del Sistema", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("Total", totalGlobal.toString(), Icons.Default.Assessment, Modifier.weight(1f))
                KpiCard("Críticos", emergencyGlobal.toString(),
                    Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f), Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("Pendientes", pendingGlobal.toString(), Icons.Default.History, Modifier.weight(1f), Color(0xFFF57C00))
                KpiCard("Éxito", completedGlobal.toString(), Icons.Default.Star, Modifier.weight(1f), Color(0xFF388E3C))
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Distribución por Tipo (Canvas)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            HelpTypeDistributionChart(
                requests = allRequests,
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showDetailedReport = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generar Mi Informe Detallado 📊")
            }

            if (showDetailedReport) {
                AlertDialog(
                    onDismissRequest = { showDetailedReport = false },
                    title = { Text("Impacto de ${currentUser?.name}") },
                    text = {
                        Column {
                            DetailRow("Tareas asignadas", myAssigned.size.toString())
                            DetailRow("Tareas cerradas", myCompleted.toString())
                            DetailRow("Emergencias reales", myEmergenciesManaged.toString(), true)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            val efficiency = if(myAssigned.isNotEmpty()) (myCompleted * 100 / myAssigned.size) else 0
                            Text("Tasa de éxito: $efficiency%", modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = { Button(onClick = { showDetailedReport = false }) { Text("Cerrar") } }
                )
            }
        }
    }
}

@Composable
fun HelpTypeDistributionChart(requests: List<com.example.comunicare.domain.model.HelpRequest>, modifier: Modifier = Modifier) {
    val types = listOf(HelpType.SHOPPING, HelpType.MEDICATION, HelpType.ACCOMPANIMENT, HelpType.EMERGENCY, HelpType.OTHER)
    val counts = types.map { type -> requests.count { it.type == type } }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val colors = listOf(Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF00BCD4), Color.Red, Color.Gray)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / (types.size * 1.8f)
        val spacing = (width - (barWidth * types.size)) / (types.size + 1)

        counts.forEachIndexed { index, count ->
            val barHeight = (count.toFloat() / maxCount) * height
            drawRect(
                color = colors[index],
                topLeft = Offset(spacing + (index * (barWidth + spacing)), height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun KpiCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = if(color == Color.Unspecified) MaterialTheme.colorScheme.primary else color)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label)
        Text(text = value, fontWeight = FontWeight.Bold, color = if (isHighlight) Color.Red else Color.Unspecified)
    }
}
