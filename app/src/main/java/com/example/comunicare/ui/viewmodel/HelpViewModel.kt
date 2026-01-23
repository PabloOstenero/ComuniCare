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

    private val allRequests: Flow<List<HelpRequest>> = getHelpRequestsUseCase()

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
                    // Buscar si ya existe una solicitud de recuperación activa
                    val requestsList = allRequests.first()
                    val activeRecovery = requestsList.find { 
                        it.beneficiaryId == user.id && 
                        it.type == HelpType.RECOVERY && 
                        it.status != RequestStatus.COMPLETED 
                    }

                    if (activeRecovery != null) {
                        // Intentar extraer el código de la descripción existente
                        val codeRegex = Regex("Código de seguridad: (\\d{4})")
                        val match = codeRegex.find(activeRecovery.description)
                        val existingCode = match?.groupValues?.get(1)
                        
                        if (existingCode != null) {
                            _generatedRecoveryCode.value = existingCode
                            _recoveryHint.value = "Ya tienes una solicitud de acceso activa. Pídele el código a: ${user.trustedContactId}"
                            return@launch
                        }
                    }

                    // Si no hay activa, generar nuevo código y mandar aviso
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
                    _recoveryHint.value = "Aviso enviado a tu contacto de confianza: ${user.trustedContactId}. Pídele el código de acceso."
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
        if (_generatedRecoveryCode.value == inputCode && inputCode.isNotBlank()) {
            loginWithRecovery(username, onComplete)
        } else {
            _loginError.value = "Código de verificación incorrecto"
        }
    }

    private fun loginWithRecovery(username: String, onComplete: (User) -> Unit) {
        viewModelScope.launch {
            val user = getUserUseCase(username)
            if (user != null) {
                _currentUser.value = user
                _generatedRecoveryCode.value = null 
                withContext(Dispatchers.Main) {
                    onComplete(user)
                }
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun sendEmergencyAlert() {
        val user = _currentUser.value ?: return
        requestHelp(HelpType.EMERGENCY, "¡EMERGENCIA! Necesito ayuda inmediata.")
    }

    fun updateStatus(requestId: String, status: RequestStatus) {
        viewModelScope.launch {
            updateHelpRequestStatusUseCase(requestId, status)
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
