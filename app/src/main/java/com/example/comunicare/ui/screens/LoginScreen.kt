package com.example.comunicare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.comunicare.domain.model.UserRole
import com.example.comunicare.ui.components.AccessibleButton
import com.example.comunicare.ui.viewmodel.HelpViewModel

@Composable
fun LoginScreen(
    viewModel: HelpViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isRecoveryMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val loginError by viewModel.loginError.collectAsState()
    val recoveryHint by viewModel.recoveryHint.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ComuniCare",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Cuidando de nuestros vecinos",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = !isRecoveryMode
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isRecoveryMode) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        } else {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Código de verificación") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = { Text("Introduce el código") }
            )
        }

        if (loginError != null) {
            Text(
                text = loginError!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isRecoveryMode) {
            AccessibleButton(
                text = "Entrar",
                onClick = { 
                    viewModel.login(phoneNumber, password) { user ->
                        onLoginSuccess(user.role)
                    }
                },
                enabled = phoneNumber.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }

            TextButton(
                onClick = { 
                    if (phoneNumber.isNotBlank()) {
                        viewModel.requestRecovery(phoneNumber)
                        isRecoveryMode = true
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("¿Has olvidado tu contraseña?")
            }
        } else {
            AccessibleButton(
                text = "Verificar Código",
                onClick = { 
                    viewModel.verifyRecoveryCode(phoneNumber, verificationCode) { user ->
                        onLoginSuccess(user.role)
                    }
                },
                enabled = verificationCode.isNotBlank()
            )

            TextButton(
                onClick = { isRecoveryMode = false },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Volver al inicio")
            }
        }

        if (recoveryHint != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = recoveryHint!!,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
