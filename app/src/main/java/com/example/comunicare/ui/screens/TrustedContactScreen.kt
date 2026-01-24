package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.comunicare.ui.components.AccessibleButton
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel

/**
 * Pantalla de configuración de seguridad.
 * Permite vincular un administrador de confianza mediante su número de teléfono.
 * RA6.c - Ayuda sensible al contexto para la recuperación de cuenta.
 */
@Composable
fun TrustedContactScreen(
    viewModel: HelpViewModel,
    onBack: () -> Unit
) {
    var contactPhone by remember { mutableStateOf("") }
    val trustedContactName by viewModel.trustedContactName.collectAsState()

    Scaffold(
        topBar = { ScreenHeader(title = "Seguridad", onBackClick = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "Contacto de Confianza",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Introduzca el número de teléfono del administrador que desea vincular. Esta persona podrá facilitarle el código de acceso si olvida su contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Visualización del nombre real del contacto (RA4.h)
            if (trustedContactName != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Contacto actualmente vinculado:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = trustedContactName!!,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { if (it.length <= 9) contactPhone = it },
                label = { Text("Teléfono del contacto") },
                placeholder = { Text("Ej: 600000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            AccessibleButton(
                text = "Actualizar Vínculo",
                onClick = {
                    if (contactPhone.length >= 9) {
                        viewModel.updateTrustedContact(contactPhone)
                        contactPhone = ""
                    }
                },
                enabled = contactPhone.length >= 9
            )
        }
    }
}
