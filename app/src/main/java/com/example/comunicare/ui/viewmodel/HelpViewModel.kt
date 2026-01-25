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

/**
 * ViewModel central de la aplicación ComuniCare.
 * Gestiona el estado global, la autenticación, las solicitudes de ayuda y la mensajería.
 * RA1.h - Aplicación integrada y estable.
 */
class HelpViewModel(
    private val getHelpRequestsUseCase: GetHelpRequestsUseCase,
    private val addHelpRequestUseCase: AddHelpRequestUseCase,
    private val updateHelpRequestStatusUseCase: UpdateHelpRequestStatusUseCase,
    private val assignHelpRequestUseCase: AssignHelpRequestUseCase,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getUserByPhoneNumberUseCase: GetUserByPhoneNumberUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val getSavedSessionUseCase: GetSavedSessionUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _trustedContactName = MutableStateFlow<String?>(null)
    val trustedContactName: StateFlow<String?> = _trustedContactName.asStateFlow()

    private val _isSessionLoaded = MutableStateFlow(false)
    val isSessionLoaded: StateFlow<Boolean> = _isSessionLoaded.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    private val _recoveryHint = MutableStateFlow<String?>(null)
    val recoveryHint: StateFlow<String?> = _recoveryHint.asStateFlow()

    private val _notificationEvent = MutableSharedFlow<Pair<String, String>>()
    val notificationEvent = _notificationEvent.asSharedFlow()

    private val allRequests: Flow<List<HelpRequest>> = getHelpRequestsUseCase()
    private val notifiedEmergencyIds = mutableSetOf<String>()

    val requests: StateFlow<List<HelpRequest>> = combine(allRequests, currentUser) { requests, user ->
        when (user?.role) {
            UserRole.ADMIN -> requests.filter { it.type != HelpType.RECOVERY || it.assignedVolunteerId == user.id }
            UserRole.BENEFICIARY -> requests.filter { it.beneficiaryId == user.id }
            null -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSavedSession()
        
        viewModelScope.launch {
            allRequests.collect { list ->
                val user = _currentUser.value
                if (user?.role == UserRole.ADMIN) {
                    list.forEach { req ->
                        if (req.type == HelpType.EMERGENCY && req.status == RequestStatus.PENDING && !notifiedEmergencyIds.contains(req.id)) {
                            _notificationEvent.emit("¡EMERGENCIA de ${req.beneficiaryName}!" to "Requiere ayuda inmediata.")
                            notifiedEmergencyIds.add(req.id)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user?.trustedContactId != null) {
                    val contact = getUserByIdUseCase(user.trustedContactId)
                    _trustedContactName.value = contact?.name ?: "Desconocido"
                } else {
                    _trustedContactName.value = null
                }
            }
        }
    }

    private fun loadSavedSession() {
        viewModelScope.launch {
            try {
                val id = getSavedSessionUseCase()
                if (id != null) {
                    val user = getUserByIdUseCase(id)
                    if (user != null) _currentUser.value = user
                }
            } finally { _isSessionLoaded.value = true }
        }
    }

    fun login(phoneNumber: String, password: String, onComplete: (User) -> Unit) {
        viewModelScope.launch {
            try {
                _loginError.value = null
                val user = getUserByPhoneNumberUseCase(phoneNumber)
                if (user == null) _loginError.value = "Cuenta no encontrada."
                else if (user.password != password) _loginError.value = "Contraseña incorrecta."
                else {
                    _currentUser.value = user
                    saveSessionUseCase(user.id)
                    withContext(Dispatchers.Main) { onComplete(user) }
                }
            } catch (_: Exception) { _loginError.value = "Error de conexión" }
        }
    }

    fun register(name: String, password: String, phone: String, role: UserRole, onComplete: (User) -> Unit) {
        viewModelScope.launch {
            try {
                _registerError.value = null
                val existing = getUserByPhoneNumberUseCase(phone)
                if (existing != null) _registerError.value = "Este teléfono ya está registrado."
                else {
                    val id = if (role == UserRole.ADMIN) "admin_$phone" else "user_$phone"
                    val newUser = User(id, name, password, phone, role, "Pista: $password")
                    saveUserUseCase(newUser)
                    _currentUser.value = newUser
                    saveSessionUseCase(id)
                    withContext(Dispatchers.Main) { onComplete(newUser) }
                }
            } catch (_: Exception) { _registerError.value = "Error en el registro" }
        }
    }

    fun logout() { viewModelScope.launch { clearSessionUseCase(); _currentUser.value = null } }

    fun requestRecovery(phone: String) {
        viewModelScope.launch {
            try {
                val user = getUserByPhoneNumberUseCase(phone)
                if (user != null && user.trustedContactId != null) {
                    // Solo mandar un código mientras no esté completado el anterior
                    val active = allRequests.first().find { 
                        it.beneficiaryId == user.id && it.type == HelpType.RECOVERY && it.status != RequestStatus.COMPLETED 
                    }
                    
                    if (active != null) {
                        _recoveryHint.value = "Ya tienes una solicitud de acceso activa."
                        return@launch
                    }

                    val code = (1000..9999).random().toString()
                    val req = HelpRequest(
                        beneficiaryId = user.id, 
                        beneficiaryName = user.name, 
                        type = HelpType.RECOVERY, 
                        description = "SOLICITUD DE ACCESO: Código de seguridad: $code", 
                        assignedVolunteerId = user.trustedContactId
                    )
                    addHelpRequestUseCase(req)
                    
                    // Notificar al contacto de confianza (simulado RA8)
                    _notificationEvent.emit("Solicitud de Recuperación" to "El usuario ${user.name} necesita recuperar su acceso.")
                    _recoveryHint.value = "Aviso enviado al contacto de confianza."
                } else {
                    _recoveryHint.value = "No se pudo procesar la recuperación."
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun verifyRecoveryCode(phone: String, inputCode: String, onComplete: (User) -> Unit) {
        viewModelScope.launch {
            val user = getUserByPhoneNumberUseCase(phone) ?: return@launch
            val active = allRequests.first().find { it.beneficiaryId == user.id && it.type == HelpType.RECOVERY && it.status != RequestStatus.COMPLETED }
            val validCode = active?.let { Regex("seguridad: (\\d{4})").find(it.description)?.groupValues?.get(1) }

            if (validCode == inputCode && inputCode.isNotBlank()) {
                _currentUser.value = user
                saveSessionUseCase(user.id)
                withContext(Dispatchers.Main) { onComplete(user) }
            } else { _loginError.value = "Código incorrecto" }
        }
    }

    fun updateTrustedContact(phoneNumber: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val contact = getUserByPhoneNumberUseCase(phoneNumber)
            if (contact != null) {
                val updatedUser = user.copy(trustedContactId = contact.id)
                saveUserUseCase(updatedUser)
                _currentUser.value = updatedUser
            }
        }
    }

    fun requestHelp(type: HelpType, desc: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val req = HelpRequest(beneficiaryId = user.id, beneficiaryName = user.name, type = type, description = desc)
            addHelpRequestUseCase(req)
            if (type == HelpType.EMERGENCY) _notificationEvent.emit("¡EMERGENCIA!" to "Notificando a administradores.")
        }
    }

    fun sendEmergencyAlert() = requestHelp(HelpType.EMERGENCY, "¡SOLICITUD CRÍTICA!")

    fun updateStatus(requestId: String, status: RequestStatus) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val requestsList = allRequests.first()
            val request = requestsList.find { it.id == requestId } ?: return@launch
            
            if (status == RequestStatus.ASSIGNED) {
                assignHelpRequestUseCase(requestId, status, user.id)
            } else if (status == RequestStatus.COMPLETED && request.assignedVolunteerId == user.id) {
                updateHelpRequestStatusUseCase(requestId, status)
            }

            // Notificar al propietario de la cuenta cuando se valida el acceso
            if (status == RequestStatus.ASSIGNED && request.type == HelpType.RECOVERY) {
                val code = Regex("seguridad: (\\d{4})").find(request.description)?.groupValues?.get(1) ?: "****"
                _notificationEvent.emit("Acceso Validado" to "Tu contacto aprobó tu entrada. Tu código es: $code")
            }
        }
    }

    fun getMessagesForRequest(id: String) = getChatMessagesUseCase(id)

    fun sendMessage(id: String, msg: String, type: MessageType = MessageType.TEXT) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val m = ChatMessage(requestId = id, senderId = user.id, senderName = user.name, content = msg, type = type)
            sendMessageUseCase(m)
        }
    }

    fun processVoiceCommand(cmd: String) {
        viewModelScope.launch {
            val c = cmd.lowercase()
            if (c.contains("ayuda")) sendEmergencyAlert()
            else if (c.contains("comida")) requestHelp(HelpType.SHOPPING, "Pedido voz")
            else if (c.contains("médico")) requestHelp(HelpType.MEDICATION, "Pedido voz")
        }
    }
}
