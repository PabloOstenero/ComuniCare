package com.example.comunicare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.model.MessageType
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel
import java.io.File
import androidx.core.net.toUri

/**
 * Pantalla de Chat con distribución optimizada estilo WhatsApp.
 * RA4.e - Jerarquía y distribución de controles perfecta.
 * Corrige el posicionamiento del teclado eliminando huecos y elevaciones excesivas.
 */
@Composable
fun ChatScreen(
    viewModel: HelpViewModel,
    requestId: String,
    currentUserId: String,
    currentUserName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.getMessagesForRequest(requestId).collectAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // --- MULTIMEDIA ---
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempPhotoUri?.let { viewModel.sendMessage(requestId, it.toString(), MessageType.IMAGE) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMessage(requestId, it.toString(), MessageType.IMAGE) }
    }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "record_${System.currentTimeMillis()}.mp3")
            audioFile = file
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
        } catch (_: Exception) { Toast.makeText(context, "Error al grabar", Toast.LENGTH_SHORT).show() }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            isRecording = false
            audioFile?.let { viewModel.sendMessage(requestId, Uri.fromFile(it).toString(), MessageType.AUDIO) }
        } catch (_: Exception) {}
    }

    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Scaffold(
        topBar = { ScreenHeader(title = "Chat de Ayuda", onBackClick = onBack) },
        bottomBar = {
            // RA4.e - Barra de entrada en slot bottomBar para gestión nativa de insets
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Respeta la barra del sistema
                    .imePadding() // Se pega al teclado perfectamente
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically // Alineación central corregida
                ) {
                    // Burbuja de entrada principal
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe un mensaje...", fontSize = 16.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 4
                        )

                        IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar", tint = Color.Gray)
                        }

                        if (messageText.isEmpty()) {
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    val file = File(context.cacheDir, "cam_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Cámara", tint = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón circular de acción (Audio/Enviar) perfectamente alineado
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(requestId, messageText, MessageType.TEXT)
                                    messageText = ""
                                } else {
                                    if (isRecording) stopRecording() else {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            startRecording()
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (messageText.isNotBlank()) Icons.AutoMirrored.Filled.Send 
                                         else if (isRecording) Icons.Default.Stop 
                                         else Icons.Default.Mic,
                            contentDescription = "Acción",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { message -> ChatBubble(message = message, isMine = message.senderId == currentUserId) }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    val context = LocalContext.current
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isMine) Color(0xFFE7FFDB) else Color.White

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = alignment) {
        Surface(
            color = bubbleColor, 
            shape = RoundedCornerShape(
                topStart = 12.dp, topEnd = 12.dp,
                bottomStart = if (isMine) 12.dp else 0.dp,
                bottomEnd = if (isMine) 0.dp else 12.dp
            ), 
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isMine) {
                    Text(message.senderName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                when (message.type) {
                    MessageType.TEXT -> Text(text = message.content, style = MaterialTheme.typography.bodyMedium)
                    MessageType.IMAGE -> AsyncImage(
                        model = message.content,
                        contentDescription = null,
                        modifier = Modifier.sizeIn(maxWidth = 240.dp, maxHeight = 320.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    MessageType.AUDIO -> Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            try {
                                MediaPlayer().apply {
                                    setDataSource(context, message.content.toUri())
                                    prepare(); start()
                                }
                            } catch (_: Exception) { Toast.makeText(context, "Error al reproducir", Toast.LENGTH_SHORT).show() }
                        }) { Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir") }
                        Text("Audio enviado", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
