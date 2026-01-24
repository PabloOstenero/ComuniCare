package com.example.comunicare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.comunicare.domain.model.*
import com.example.comunicare.domain.use_case.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel central de la aplicación ComuniCare.
 * Gestiona el estado global, la autenticación, las solicitudes de ayuda, la mensajería y la persistencia de sesión.
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
    private val getUserUseCase: GetUserUseCase // Para compatibilidad
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

        // Observar cambios en el usuario para cargar el nombre del contacto de confianza
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
            val user = getUserByPhoneNumberUseCase(phone)
            if (user != null && user.trustedContactId != null) {
                val code = (1000..9999).random().toString()
                val req = HelpRequest(beneficiaryId = user.id, beneficiaryName = user.name, type = HelpType.RECOVERY, description = "Código: $code", assignedVolunteerId = user.trustedContactId)
                addHelpRequestUseCase(req)
                _recoveryHint.value = "Aviso enviado al contacto de confianza."
            }
        }
    }

    fun verifyRecoveryCode(phone: String, code: String, onComplete: (User) -> Unit) {
        viewModelScope.launch {
            val user = getUserByPhoneNumberUseCase(phone) ?: return@launch
            val active = allRequests.first().find { it.beneficiaryId == user.id && it.type == HelpType.RECOVERY && it.status != RequestStatus.COMPLETED }
            if (active?.description?.contains(code) == true) {
                _currentUser.value = user
                saveSessionUseCase(user.id)
                withContext(Dispatchers.Main) { onComplete(user) }
            } else { _loginError.value = "Código inválido" }
        }
    }

    fun updateTrustedContact(phoneNumber: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val contact = getUserByPhoneNumberUseCase(phoneNumber)
                if (contact != null) {
                    val updatedUser = user.copy(trustedContactId = contact.id)
                    saveUserUseCase(updatedUser)
                    _currentUser.value = updatedUser
                    _trustedContactName.value = contact.name
                } else {
                    // Opcional: Feedback de contacto no encontrado
                }
            } catch (e: Exception) { e.printStackTrace() }
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

    fun updateStatus(id: String, status: RequestStatus) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val req = allRequests.first().find { it.id == id } ?: return@launch
            if (status == RequestStatus.ASSIGNED) assignHelpRequestUseCase(id, status, user.id)
            else if (status == RequestStatus.COMPLETED && req.assignedVolunteerId == user.id) updateHelpRequestStatusUseCase(id, status)
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
            when {
                c.contains("comida") -> requestHelp(HelpType.SHOPPING, "Pedido voz: Comida")
                c.contains("médico") -> requestHelp(HelpType.MEDICATION, "Pedido voz: Salud")
                c.contains("ayuda") -> sendEmergencyAlert()
                else -> requestHelp(HelpType.OTHER, "Pedido voz: $cmd")
            }
        }
    }
}
