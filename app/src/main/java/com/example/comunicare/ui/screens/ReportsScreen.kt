package com.example.comunicare.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

/**
 * Screen for data analysis and advanced reports.
 * Meets RA5.a (Structure), RA5.b (Data generation), RA5.c (Filters), 
 * RA5.d (Calculations), RA5.e (Graphics/Canvas).
 */
@Composable
fun ReportsScreen(viewModel: HelpViewModel) {
    val allRequests by viewModel.requests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    var showDetailedReport by remember { mutableStateOf(false) }

    // RA5.d - Cálculos Globales Avanzados
    val totalGlobal = allRequests.size
    val emergencyGlobal = allRequests.count { it.type == HelpType.EMERGENCY }
    val completedGlobal = allRequests.count { it.status == RequestStatus.COMPLETED }
    val pendingGlobal = allRequests.count { it.status == RequestStatus.PENDING }
    val assignedGlobal = allRequests.count { it.status == RequestStatus.ASSIGNED }

    // RA5.b - Datos para el Informe Personal del Administrador
    val myAssigned = allRequests.filter { it.assignedVolunteerId == currentUser?.id }
    val myCompleted = myAssigned.count { it.status == RequestStatus.COMPLETED }
    val myEmergenciesManaged = myAssigned.count { it.type == HelpType.EMERGENCY }
    
    // Cálculos de tipos para el usuario actual
    val myShopping = myAssigned.count { it.type == HelpType.SHOPPING }
    val myMedication = myAssigned.count { it.type == HelpType.MEDICATION }
    val myAccompaniment = myAssigned.count { it.type == HelpType.ACCOMPANIMENT }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(title = "Panel de Estadísticas")
        
        Column(modifier = Modifier.padding(16.dp)) {
            
            Text(
                text = "Resumen del Sistema", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid de KPIs Rápidos
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(
                    title = "Total", 
                    value = totalGlobal.toString(), 
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Críticos", 
                    value = emergencyGlobal.toString(), 
                    icon = Icons.Default.TrendingUp,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(
                    title = "Pendientes", 
                    value = pendingGlobal.toString(), 
                    icon = Icons.Default.History,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Éxito", 
                    value = completedGlobal.toString(), 
                    icon = Icons.Default.Star,
                    color = Color(0xFF388E3C),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Distribución de Necesidades (Canvas)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Frecuencia por categoría de servicio",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // RA5.e - Gráfico de barras avanzado
            HelpTypeDistributionChart(
                requests = allRequests,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // RA5.b - Generación de Informe Proactivo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Informe de Rendimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Analiza tu impacto individual en la red de voluntariado.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { showDetailedReport = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Generar Mi Informe Detallado 📑")
                    }
                }
            }

            if (showDetailedReport) {
                AlertDialog(
                    onDismissRequest = { showDetailedReport = false },
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Reporte: ${currentUser?.name}") 
                        }
                    },
                    text = {
                        Column {
                            Text("Métricas de Gestión (RA5.d):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            DetailRow("Total asignadas", myAssigned.size.toString())
                            DetailRow("Completadas con éxito", myCompleted.toString())
                            DetailRow("Emergencias atendidas", myEmergenciesManaged.toString(), isHighlight = true)
                            
                            Spacer(Modifier.height(16.dp))
                            Text("Desglose por Categoría:", fontWeight = FontWeight.Bold)
                            DetailRow("Compras/Comida", myShopping.toString())
                            DetailRow("Salud/Médico", myMedication.toString())
                            DetailRow("Acompañamiento", myAccompaniment.toString())
                            
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))
                            
                            val efficiency = if(myAssigned.isNotEmpty()) (myCompleted * 100 / myAssigned.size) else 0
                            val statusMsg = when {
                                efficiency > 80 -> "Excelente desempeño y compromiso."
                                efficiency > 50 -> "Buen ritmo de resolución."
                                else -> "Continúa apoyando a la comunidad."
                            }
                            
                            Text(
                                text = "Eficiencia Individual: $efficiency%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = statusMsg,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showDetailedReport = false }) { Text("Finalizar Lectura") }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun KpiCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = if(color == Color.Unspecified) MaterialTheme.colorScheme.primary else color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) Color.Red else Color.Unspecified
        )
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
            val barHeight = (count.toFloat() / maxCount) * (height * 0.85f)
            
            // Dibujar sombra suave de la barra
            drawRect(
                color = colors[index].copy(alpha = 0.1f),
                topLeft = Offset(spacing + (index * (barWidth + spacing)), 0f),
                size = Size(barWidth, height)
            )
            
            // Dibujar barra real
            drawRect(
                color = colors[index],
                topLeft = Offset(spacing + (index * (barWidth + spacing)), height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
    
    // Leyenda de la gráfica mejorada
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        LegendItem("Comida", Color(0xFF2196F3))
        LegendItem("Salud", Color(0xFFE91E63))
        LegendItem("Paseo", Color(0xFF00BCD4))
        LegendItem("Emerg.", Color.Red)
        LegendItem("Otros", Color.Gray)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
