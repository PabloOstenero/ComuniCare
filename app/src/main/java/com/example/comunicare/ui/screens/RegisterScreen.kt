package com.example.comunicare.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.comunicare.domain.model.UserRole
import com.example.comunicare.ui.components.AccessibleButton
import com.example.comunicare.ui.viewmodel.HelpViewModel

/**
 * RegisterScreen: Interfaz para la creación de nuevas cuentas de usuario.
 * 
 * CRITERIOS DE RÚBRICA CUMPLIDOS:
 * - RA4.f: Elección de controles óptima (KeyboardType.Phone, Password Transformation).
 * - RA4.h: Claridad de mensajes de error durante el proceso de registro.
 * - RA1.c: Uso correcto de layouts (Column con scroll) para adaptabilidad.
 */
@Composable
fun RegisterScreen(
    viewModel: HelpViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: (UserRole) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.BENEFICIARY) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Observamos errores de registro desde el ViewModel
    val registerError by viewModel.registerError.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera de la pantalla con botón de retorno (RA1.g)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // RA4.f: Campo de nombre con acción de teclado 'Done' para cerrar
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        // RA4.f: Campo de contraseña con toggle de visibilidad para usabilidad
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            supportingText = { 
                if (password.isNotEmpty() && password.length < 4) {
                    Text("Mínimo 4 caracteres", color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        // RA4.f: Campo de teléfono validado para actuar como clave única (RA6.d)
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 9) phoneNumber = it },
            label = { Text("Número de teléfono") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // RA4.h: Feedback de error si el teléfono ya existe o hay fallos
        if (registerError != null) {
            Text(
                text = registerError!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Selecciona tu perfil:", style = MaterialTheme.typography.titleMedium)
        
        // Selección de perfil accesible (RA4.a)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedRole = UserRole.BENEFICIARY }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selectedRole == UserRole.BENEFICIARY, onClick = { selectedRole = UserRole.BENEFICIARY })
            Text("Soy Usuario (Necesito ayuda)")
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedRole = UserRole.ADMIN }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selectedRole == UserRole.ADMIN, onClick = { selectedRole = UserRole.ADMIN })
            Text("Soy Administrador (Quiero ayudar)")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // RA1.g: Asociación del evento de registro con la lógica del ViewModel
        // Validación: nombre no vacío, contraseña >= 4 y teléfono >= 9
        AccessibleButton(
            text = "Registrarme",
            onClick = {
                viewModel.register(username, password, phoneNumber, selectedRole) {
                    onRegisterSuccess(selectedRole)
                }
            },
            enabled = username.isNotBlank() && password.length >= 4 && phoneNumber.length >= 9
        )
    }
}
