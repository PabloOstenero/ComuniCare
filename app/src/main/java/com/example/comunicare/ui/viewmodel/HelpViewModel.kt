package com.example.comunicare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comunicare.domain.model.*
import com.example.comunicare.domain.use_case.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class HelpViewModel(
    private val getHelpRequestsUseCase: GetHelpRequestsUseCase,
    private val addHelpRequestUseCase: AddHelpRequestUseCase,
    private val updateHelpRequestStatusUseCase: UpdateHelpRequestStatusUseCase,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val saveUserUseCase: SaveUserUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _recoveryHint = MutableStateFlow<String?>(null)
    val recoveryHint: StateFlow<String?> = _recoveryHint.asStateFlow()

    private val _generatedRecoveryCode = MutableStateFlow<String?>(null)

    private val _notificationEvent = MutableSharedFlow<Pair<String, String>>()
    val notificationEvent = _notificationEvent.asSharedFlow()

    private val allRequests: Flow<List<HelpRequest>> = getHelpRequestsUseCase()

    // Control de notificaciones ya mostradas para evitar spam
    private val notifiedEmergencyIds = mutableSetOf<String>()

    val requests: StateFlow<List<HelpRequest>> = combine(allRequests, currentUser) { requests, user ->
        when (user?.role) {
            UserRole.ADMIN -> {
                requests.filter { 
                    if (it.type == HelpType.RECOVERY) {
                        it.assignedVolunteerId == user.id
                    } else {
                        true
                    }
                }
            }
            UserRole.BENEFICIARY -> requests.filter { it.beneficiaryId == user.id }
            null -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // RA8 - Sistema de escucha activa para emergencias
        viewModelScope.launch {
            allRequests.collect { requestsList ->
                val user = _currentUser.value
                // Solo notificamos si el usuario actual es administrador
                if (user?.role == UserRole.ADMIN) {
                    requestsList.forEach { request ->
                        if (request.type == HelpType.EMERGENCY && 
                            request.status == RequestStatus.PENDING && 
                            !notifiedEmergencyIds.contains(request.id)) {
                            
                            _notificationEvent.emit(
                                "¡EMERGENCIA de ${request.beneficiaryName}!" to 
                                "El usuario necesita ayuda inmediata. Revisa los detalles en el panel."
                            )
                            notifiedEmergencyIds.add(request.id)
                        }
                    }
                }
            }
        }
    }

    fun login(username: String, password: String, role: UserRole, onComplete: (User) -> Unit) {
        viewModelScope.launch {
            try {
                _loginError.value = null
                _recoveryHint.value = null
                
                var user = getUserUseCase(username)
                
                if (user == null) {
                    val id = if (role == UserRole.ADMIN) "admin_$username" else "user_$username"
                    val newUser = User(id, username, password, role, "Pista: Tu contraseña es '$password'")
                    saveUserUseCase(newUser)
                    user = newUser
                } else if (user.password != password) {
                    _loginError.value = "Contraseña incorrecta"
                    return@launch
                }

                _currentUser.value = user
                withContext(Dispatchers.Main) {
                    onComplete(user!!)
                }
            } catch (e: Exception) {
                _loginError.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun requestRecovery(username: String) {
        viewModelScope.launch {
            try {
                val user = getUserUseCase(username)
                if (user != null && user.trustedContactId != null) {
                    val requestsList = allRequests.first()
                    val activeRecovery = requestsList.find { 
                        it.beneficiaryId == user.id && 
                        it.type == HelpType.RECOVERY && 
                        it.status != RequestStatus.COMPLETED 
                    }

                    if (activeRecovery != null) {
                        val codeRegex = Regex("Código de seguridad: (\\d{4})")
                        val match = codeRegex.find(activeRecovery.description)
                        val existingCode = match?.groupValues?.get(1)
                        
                        if (existingCode != null) {
                            _generatedRecoveryCode.value = existingCode
                            _recoveryHint.value = "Solicitud activa. Contacta con: ${user.trustedContactId}"
                            return@launch
                        }
                    }

                    val code = (1000..9999).random().toString()
                    _generatedRecoveryCode.value = code
                    
                    val recoveryRequest = HelpRequest(
                        beneficiaryId = user.id,
                        beneficiaryName = user.name,
                        type = HelpType.RECOVERY,
                        description = "SOLICITUD DE ACCESO: El usuario ha olvidado su contraseña. Código de seguridad: $code",
                        assignedVolunteerId = user.trustedContactId
                    )
                    addHelpRequestUseCase(recoveryRequest)
                    _recoveryHint.value = "Aviso enviado a tu contacto: ${user.trustedContactId}."

                    _notificationEvent.emit(
                        "Recuperación de Cuenta" to 
                        "El usuario ${user.name} solicita ayuda para entrar. Revisa tu panel."
                    )

                } else if (user != null) {
                    _recoveryHint.value = "No tienes un contacto de confianza configurado."
                } else {
                    _loginError.value = "Usuario no encontrado"
                }
            } catch (e: Exception) {
                _loginError.value = "Error al procesar recuperación"
            }
        }
    }

    fun verifyRecoveryCode(username: String, inputCode: String, onComplete: (User) -> Unit) {
        viewModelScope.launch {
            val user = getUserUseCase(username)
            if (user == null) {
                _loginError.value = "Usuario no encontrado"
                return@launch
            }

            val requestsList = allRequests.first()
            val activeRecovery = requestsList.find { 
                it.beneficiaryId == user.id && 
                it.type == HelpType.RECOVERY && 
                it.status != RequestStatus.COMPLETED 
            }

            val codeRegex = Regex("Código de seguridad: (\\d{4})")
            val match = activeRecovery?.let { codeRegex.find(it.description) }
            val validCode = match?.groupValues?.get(1)

            if (validCode == inputCode && inputCode.isNotBlank()) {
                _currentUser.value = user
                withContext(Dispatchers.Main) {
                    onComplete(user)
                }
            } else {
                _loginError.value = "Código incorrecto"
            }
        }
    }

    fun updateTrustedContact(contactName: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val contactId = if (contactName.startsWith("admin_")) contactName else "admin_$contactName"
                val updatedUser = user.copy(trustedContactId = contactId)
                saveUserUseCase(updatedUser)
                _currentUser.value = updatedUser
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
        _recoveryHint.value = null
        _generatedRecoveryCode.value = null
    }

    fun requestHelp(type: HelpType, description: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val newRequest = HelpRequest(
                    beneficiaryId = user.id,
                    beneficiaryName = user.name,
                    type = type,
                    description = description
                )
                addHelpRequestUseCase(newRequest)
                
                // Confirmación local al usuario
                if (type == HelpType.EMERGENCY) {
                    _notificationEvent.emit("Emergencia de ${user.name} enviada" to "Estamos avisando a todos los administradores.")
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun sendEmergencyAlert() {
        requestHelp(HelpType.EMERGENCY, "¡EMERGENCIA! El usuario necesita ayuda inmediata.")
    }

    fun updateStatus(requestId: String, status: RequestStatus) {
        viewModelScope.launch {
            try { 
                updateHelpRequestStatusUseCase(requestId, status)
                
                if (status == RequestStatus.ASSIGNED) {
                    val requestsList = allRequests.first()
                    val request = requestsList.find { it.id == requestId }
                    if (request != null && request.type == HelpType.RECOVERY) {
                        val codeRegex = Regex("Código de seguridad: (\\d{4})")
                        val match = codeRegex.find(request.description)
                        val code = match?.groupValues?.get(1) ?: "****"
                        
                        _notificationEvent.emit("Código de Acceso" to "Tu contacto ha validado tu identidad. Código: $code")
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun getMessagesForRequest(requestId: String): Flow<List<ChatMessage>> {
        return getChatMessagesUseCase(requestId)
    }

    fun sendMessage(requestId: String, content: String, type: MessageType = MessageType.TEXT) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val message = ChatMessage(
                    requestId = requestId,
                    senderId = user.id,
                    senderName = user.name,
                    content = content,
                    type = type
                )
                sendMessageUseCase(message)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
