package com.example.comunicare.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

/**
 * Screen for data analysis and reports.
 * Meets RA5.a (Structure), RA5.b (Data generation), RA5.c (Filters), 
 * RA5.d (Calculations), RA5.e (Graphics/Canvas).
 */
@Composable
fun ReportsScreen(viewModel: HelpViewModel) {
    val requests by viewModel.requests.collectAsState()
    
    // RA5.c - State for filtering
    var selectedTypeFilter by remember { mutableStateOf<HelpType?>(null) }

    val filteredRequests = if (selectedTypeFilter == null) {
        requests
    } else {
        requests.filter { it.type == selectedTypeFilter }
    }

    // RA5.d - Calculations based on data
    val totalRequests = filteredRequests.size
    val emergencyCount = filteredRequests.count { it.type == HelpType.EMERGENCY }
    val completedCount = filteredRequests.count { it.status == com.example.comunicare.domain.model.RequestStatus.COMPLETED }
    val pendingCount = filteredRequests.count { it.status == com.example.comunicare.domain.model.RequestStatus.PENDING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Informes y Estadísticas")
        
        Column(modifier = Modifier.padding(16.dp)) {
            
            // RA5.c - Filter Controls
            Text(text = "Filtrar por tipo:", style = MaterialTheme.typography.titleMedium)
            ScrollableTabRow(
                selectedTabIndex = if (selectedTypeFilter == null) 0 else selectedTypeFilter!!.ordinal + 1,
                edgePadding = 0.dp,
                divider = {},
                containerColor = Color.Transparent
            ) {
                Tab(selected = selectedTypeFilter == null, onClick = { selectedTypeFilter = null }) {
                    Text("Todos", modifier = Modifier.padding(8.dp))
                }
                HelpType.entries.forEach { type ->
                    Tab(selected = selectedTypeFilter == type, onClick = { selectedTypeFilter = type }) {
                        Text(type.name, modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ReportItem(label = "Solicitudes mostradas", value = totalRequests.toString())
            ReportItem(label = "Emergencias", value = emergencyCount.toString())
            ReportItem(label = "Completadas", value = completedCount.toString())
            ReportItem(label = "Pendientes", value = pendingCount.toString())
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Distribución Visual (Canvas RA5.e)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // RA5.e - Custom Graphics using Canvas
            SimpleBarChart(
                emergency = emergencyCount,
                completed = completedCount,
                pending = pendingCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Resumen: Se están visualizando $totalRequests registros. " +
                           "Las emergencias representan el ${if(totalRequests > 0) (emergencyCount*100/totalRequests) else 0}% del total filtrado.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SimpleBarChart(emergency: Int, completed: Int, pending: Int, modifier: Modifier = Modifier) {
    val total = (emergency + completed + pending).toFloat().coerceAtLeast(1f)
    val colorEmergency = Color.Red
    val colorCompleted = Color(0xFF388E3C)
    val colorPending = Color(0xFFF57C00)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / 5
        val spacing = width / 10
        
        // Emergenias
        drawRect(
            color = colorEmergency,
            topLeft = Offset(spacing, height - (height * (emergency / total))),
            size = Size(barWidth, height * (emergency / total))
        )
        
        // Completadas
        drawRect(
            color = colorCompleted,
            topLeft = Offset(spacing * 2 + barWidth, height - (height * (completed / total))),
            size = Size(barWidth, height * (completed / total))
        )

        // Pendientes
        drawRect(
            color = colorPending,
            topLeft = Offset(spacing * 3 + barWidth * 2, height - (height * (pending / total))),
            size = Size(barWidth, height * (pending / total))
        )
    }
}

@Composable
fun ReportItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider()
}
