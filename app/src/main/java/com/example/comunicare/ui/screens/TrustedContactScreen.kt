package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.comunicare.ui.components.AccessibleButton
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

@Composable
fun TrustedContactScreen(
    viewModel: HelpViewModel,
    onBack: () -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ScreenHeader(title = "Contacto de Confianza", onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "Seguridad y Recuperación",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Añade el nombre de usuario de una persona de confianza (como un familiar o administrador). Si olvidas tu contraseña, esta persona podrá ayudarte.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (currentUser?.trustedContactId != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Contacto actual: ${currentUser?.trustedContactId}",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Usuario del contacto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AccessibleButton(
                text = "Guardar Contacto",
                onClick = {
                    // En un sistema real buscaríamos si el usuario existe. 
                    // Aquí simplemente lo guardamos en el perfil actual.
                    viewModel.updateTrustedContact(contactName)
                    contactName = ""
                    // Mostrar feedback
                }
            )
        }
    }
}
