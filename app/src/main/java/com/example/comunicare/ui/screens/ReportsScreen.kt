package com.example.comunicare.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de Estadísticas e Informes Avanzados de ComuniCare.
 * Proporciona un análisis exhaustivo de la gestión comunitaria y el rendimiento individual.
 * Cumple con RA5.a, RA5.b, RA5.c, RA5.d y RA5.e.
 */
@Composable
fun ReportsScreen(viewModel: HelpViewModel) {
    val allRequests by viewModel.requests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    var showDetailedReport by remember { mutableStateOf(false) }

    // --- CÁLCULOS GLOBALES DEL SISTEMA (RA5.d) ---
    val totalGlobal = allRequests.size
    val emergencyGlobal = allRequests.count { it.type == HelpType.EMERGENCY }
    val completedGlobal = allRequests.count { it.status == RequestStatus.COMPLETED }
    val pendingGlobal = allRequests.count { it.status == RequestStatus.PENDING }
    val assignedGlobal = allRequests.count { it.status == RequestStatus.ASSIGNED }
    
    // Tasa de resolución global
    val resolutionRate = if (totalGlobal > 0) (completedGlobal.toFloat() / totalGlobal * 100).toInt() else 0
    
    // Voluntarios activos (basado en IDs únicos asignados)
    val activeVolunteers = allRequests.mapNotNull { it.assignedVolunteerId }.distinct().size

    // --- DATOS DEL INFORME PERSONAL (RA5.b) ---
    val myAssigned = allRequests.filter { it.assignedVolunteerId == currentUser?.id }
    val myCompleted = myAssigned.filter { it.status == RequestStatus.COMPLETED }
    val myEmergencies = myAssigned.count { it.type == HelpType.EMERGENCY }
    
    // Desglose de servicios personales por tipo
    val myShopping = myAssigned.count { it.type == HelpType.SHOPPING }
    val myMeds = myAssigned.count { it.type == HelpType.MEDICATION }
    val myWalks = myAssigned.count { it.type == HelpType.ACCOMPANIMENT }
    
    // Impacto relativo en la comunidad (RA5.d)
    val myImpactPercentage = if (totalGlobal > 0) (myCompleted.size.toFloat() / totalGlobal * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(title = "Análisis de Datos")
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen Operativo del Barrio", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tasa de resolución: $resolutionRate%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Voluntarios activos: $activeVolunteers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Grid de KPIs Principales (RA5.d)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Total Solicitudes", totalGlobal.toString(), Icons.Default.FactCheck, Modifier.weight(1f))
                KpiCard("Alertas Críticas", emergencyGlobal.toString(), Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f), Color.Red)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("En Espera", pendingGlobal.toString(), Icons.Default.HourglassEmpty, Modifier.weight(1f), Color(0xFFF57C00))
                KpiCard("Completadas", completedGlobal.toString(), Icons.Default.CheckCircle, Modifier.weight(1f), Color(0xFF388E3C))
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Gráfico de Barras: Demanda por categoría (RA5.e)
            Text(text = "Demanda de Servicios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Análisis de frecuencia por tipo de ayuda", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            HelpTypeDistributionChart(
                requests = allRequests,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Gráfico Circular: Estado de la Gestión (RA5.e)
            Text(text = "Estado de Resolución Global", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            StatusPieChart(
                pending = pendingGlobal,
                assigned = assignedGlobal,
                completed = completedGlobal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Actividad Reciente Estratégica (RA5.b)
            Text(text = "Últimas Alertas de la Red", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (allRequests.isEmpty()) {
                Text("Sin actividad registrada.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                allRequests.take(3).forEach { req ->
                    RecentActivityItem(req)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Generador de Informe Personal Profesional (RA5.b)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Informe de Impacto Personal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Genera un análisis profesional de tu rendimiento y contribución social.", style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showDetailedReport = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Generar Informe Detallado 📄")
                    }
                }
            }

            if (showDetailedReport) {
                AlertDialog(
                    onDismissRequest = { showDetailedReport = false },
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("Certificado de Gestión") 
                        }
                    },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("Usuario: ${currentUser?.name}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Métricas de Rendimiento:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Total avisos gestionados", myAssigned.size.toString())
                            DetailRow("Tareas cerradas con éxito", myCompleted.size.toString())
                            DetailRow("Emergencias críticas atendidas", myEmergencies.toString(), isHighlight = true)
                            DetailRow("Aportación a la red global", "$myImpactPercentage%")
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Desglose por Áreas de Ayuda:", fontWeight = FontWeight.Bold)
                            DetailRow("Compras y Alimentación", myShopping.toString())
                            DetailRow("Atención Médica", myMeds.toString())
                            DetailRow("Paseos y Compañía", myWalks.toString())
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Historial de Logros Recientes:", fontWeight = FontWeight.Bold)
                            if (myCompleted.isEmpty()) {
                                Text("Aún no se han registrado tareas completadas.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            } else {
                                myCompleted.take(5).forEach { req ->
                                    val date = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(req.timestamp))
                                    Text("• ${req.type.name} ($date)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val efficiency = if(myAssigned.isNotEmpty()) (myCompleted.size * 100 / myAssigned.size) else 0
                                val msg = when {
                                    efficiency > 80 && myCompleted.size > 5 -> "¡Lideras el impacto comunitario! Excelente labor."
                                    efficiency > 50 -> "Gran compromiso. Tu ayuda es vital para los vecinos."
                                    else -> "Tu tiempo y dedicación están transformando el barrio."
                                }
                                Text(
                                    text = msg,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDetailedReport = false }) { 
                            Text("Cerrar Informe", fontWeight = FontWeight.Bold) 
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun HelpTypeDistributionChart(requests: List<HelpRequest>, modifier: Modifier = Modifier) {
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
            
            // Dibujar fondo de la barra para profundidad visual
            drawRect(
                color = colors[index].copy(alpha = 0.1f),
                topLeft = Offset(spacing + (index * (barWidth + spacing)), 0f),
                size = Size(barWidth, height)
            )
            
            // Dibujar barra de datos real
            drawRect(
                color = colors[index],
                topLeft = Offset(spacing + (index * (barWidth + spacing)), height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
    
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        types.forEach { 
            val label = when(it) {
                HelpType.SHOPPING -> "Com."
                HelpType.MEDICATION -> "Salud"
                HelpType.ACCOMPANIMENT -> "Pas."
                HelpType.EMERGENCY -> "Emer."
                else -> "Otro"
            }
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant) 
        }
    }
}

@Composable
fun StatusPieChart(pending: Int, assigned: Int, completed: Int, modifier: Modifier = Modifier) {
    val total = (pending + assigned + completed).coerceAtLeast(1)
    val pAngle = 360f * (pending.toFloat() / total)
    val aAngle = 360f * (assigned.toFloat() / total)
    val cAngle = 360f * (completed.toFloat() / total)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(color = Color.Red, startAngle = 0f, sweepAngle = pAngle, useCenter = false, style = Stroke(12.dp.toPx()))
                drawArc(color = Color(0xFF2196F3), startAngle = pAngle, sweepAngle = aAngle, useCenter = false, style = Stroke(12.dp.toPx()))
                drawArc(color = Color(0xFF388E3C), startAngle = pAngle + aAngle, sweepAngle = cAngle, useCenter = false, style = Stroke(12.dp.toPx()))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(completed * 100 / total)}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Éxito", style = MaterialTheme.typography.labelSmall)
            }
        }
        
        Spacer(modifier = Modifier.width(32.dp))
        
        Column {
            LegendItem("Pendiente (${(pending*100/total)}%)", Color.Red)
            LegendItem("En Proceso (${(assigned*100/total)}%)", Color(0xFF2196F3))
            LegendItem("Completado (${(completed*100/total)}%)", Color(0xFF388E3C))
        }
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun KpiCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = if(color == Color.Unspecified) MaterialTheme.colorScheme.primary else color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Bold, 
            color = if (isHighlight) Color.Red else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RecentActivityItem(request: HelpRequest) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if(request.type == HelpType.EMERGENCY) Icons.Default.Warning else Icons.Default.Circle
            Icon(
                icon, 
                contentDescription = null, 
                tint = if(request.type == HelpType.EMERGENCY) Color.Red else MaterialTheme.colorScheme.secondary, 
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("${request.type.name} - ${request.beneficiaryName}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(request.description, style = MaterialTheme.typography.bodySmall, maxLines = 1, color = Color.Gray)
            }
        }
    }
}
