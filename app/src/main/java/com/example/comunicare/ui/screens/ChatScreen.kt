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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.comunicare.domain.model.ChatMessage
import com.example.comunicare.domain.model.MessageType
import com.example.comunicare.ui.components.ScreenHeader
import com.example.comunicare.ui.viewmodel.HelpViewModel
import java.io.File

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

    // --- CÁMARA ---
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempPhotoUri?.let { viewModel.sendMessage(requestId, it.toString(), MessageType.IMAGE) }
    }

    // --- GALERÍA ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMessage(requestId, it.toString(), MessageType.IMAGE) }
    }

    // --- GRABACIÓN AUDIO ---
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
            Toast.makeText(context, "Permisos concedidos", Toast.LENGTH_SHORT).show()
        }
    }

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "record_${System.currentTimeMillis()}.mp3")
            audioFile = file
            @Suppress("DEPRECATION")
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
        } catch (e: Exception) { Toast.makeText(context, "Error al grabar", Toast.LENGTH_SHORT).show() }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            audioFile?.let { viewModel.sendMessage(requestId, Uri.fromFile(it).toString(), MessageType.AUDIO) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Scaffold(
        topBar = { ScreenHeader(title = "Chat de Ayuda", onBackClick = onBack) },
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth().imePadding()) {
                Row(
                    modifier = Modifier.padding(8.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val file = File(context.cacheDir, "cam_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri)
                        } else { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA)) }
                    }) { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }

                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = {
                        if (isRecording) stopRecording() else {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                startRecording()
                            } else { permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO)) }
                        }
                    }) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(if (isRecording) "Grabando..." else "Escribe...") },
                        enabled = !isRecording,
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(requestId, messageText, MessageType.TEXT)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank() && !isRecording
                    ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) }
                }
            }
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message -> ChatBubble(message = message, isMine = message.senderId == currentUserId) }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    val context = LocalContext.current
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = alignment) {
        Surface(color = bubbleColor, shape = RoundedCornerShape(12.dp), tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(12.dp)) {
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
                                    setDataSource(context, Uri.parse(message.content))
                                    prepare()
                                    start()
                                }
                            } catch (e: Exception) { Toast.makeText(context, "Error al reproducir", Toast.LENGTH_SHORT).show() }
                        }) { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                        Text("Audio enviado", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
