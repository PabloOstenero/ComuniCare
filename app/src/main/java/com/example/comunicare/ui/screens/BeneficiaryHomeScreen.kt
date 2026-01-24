package com.example.comunicare.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.launch

@Composable
fun BeneficiaryHomeScreen(
    viewModel: HelpViewModel,
    onOpenMenu: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val myRequests = requests.filter { it.beneficiaryId == currentUser?.id }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            if (spokenText.isNotBlank()) {
                viewModel.processVoiceCommand(spokenText)
                scope.launch { snackbarHostState.showSnackbar("Procesando: \"$spokenText\"") }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "¿En qué puedo ayudarte?")
                    }
                    try { speechLauncher.launch(intent) } catch (_: Exception) {}
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) { Icon(Icons.Default.Mic, contentDescription = "Voz") }
        },
        topBar = {
            ScreenHeader(
                title = "Mi Ayuda",
                onMenuClick = onOpenMenu // Corregido el nombre del parámetro
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Hola, ${currentUser?.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryButton("Comida", HelpType.SHOPPING, viewModel, Modifier.weight(1f))
                    CategoryButton("Salud", HelpType.MEDICATION, viewModel, Modifier.weight(1f))
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryButton("Paseo", HelpType.ACCOMPANIMENT, viewModel, Modifier.weight(1f))
                    CategoryButton("Otros", HelpType.OTHER, viewModel, Modifier.weight(1f))
                }
            }

            item {
                AccessibleButton(
                    text = "¡EMERGENCIA!",
                    onClick = { viewModel.sendEmergencyAlert() },
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
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
fun CategoryButton(label: String, type: HelpType, viewModel: HelpViewModel, modifier: Modifier = Modifier) {
    Surface(
        onClick = { viewModel.requestHelp(type, "Solicitud de $label") },
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
