package com.example.comunicare

import com.example.comunicare.domain.model.*
import com.example.comunicare.domain.repository.HelpRepository
import com.example.comunicare.domain.use_case.*
import com.example.comunicare.ui.viewmodel.HelpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * HelpViewModelTest: Pruebas unitarias para la lógica de negocio (RA8.a).
 * Valida flujos de autenticación, registro y gestión de solicitudes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HelpViewModelTest {

    private lateinit var viewModel: HelpViewModel
    private lateinit var repository: FakeHelpRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHelpRepository()
        
        // Inicialización de todos los casos de uso para el ViewModel (RA1.e)
        viewModel = HelpViewModel(
            getHelpRequestsUseCase = GetHelpRequestsUseCase(repository),
            addHelpRequestUseCase = AddHelpRequestUseCase(repository),
            updateHelpRequestStatusUseCase = UpdateHelpRequestStatusUseCase(repository),
            assignHelpRequestUseCase = AssignHelpRequestUseCase(repository),
            getChatMessagesUseCase = GetChatMessagesUseCase(repository),
            sendMessageUseCase = SendMessageUseCase(repository),
            getUserByPhoneNumberUseCase = GetUserByPhoneNumberUseCase(repository),
            saveUserUseCase = SaveUserUseCase(repository),
            getSavedSessionUseCase = GetSavedSessionUseCase(repository),
            saveSessionUseCase = SaveSessionUseCase(repository),
            clearSessionUseCase = ClearSessionUseCase(repository),
            getUserByIdUseCase = GetUserByIdUseCase(repository),
            getUserUseCase = GetUserUseCase(repository)
        )
    }

    @Test
    fun `register and login success flow`() = runTest {
        // Necesario para activar los StateFlows con WhileSubscribed (RA1.h)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.requests.collect() }

        val phone = "600000000"
        val name = "Test User"
        val pass = "1234"
        
        // Test Registro
        viewModel.register(name, pass, phone, UserRole.BENEFICIARY) { user ->
            assertEquals(name, user.name)
            assertEquals(phone, user.phoneNumber)
        }
        advanceUntilIdle()
        
        // Test Login
        viewModel.login(phone, pass) { user ->
            assertEquals(name, user.name)
            assertNotNull(viewModel.currentUser.value)
        }
        advanceUntilIdle()
    }

    @Test
    fun `login with wrong password should set error`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.requests.collect() }

        val phone = "611222333"
        // Guardamos un usuario con una contraseña específica
        repository.saveUser(User("1", "Ana", "correct_pass", phone, UserRole.ADMIN))
        
        // Intentamos login con contraseña errónea
        viewModel.login(phone, "wrong_pass") { }
        advanceUntilIdle()
        
        assertNull(viewModel.currentUser.value)
        // FIX: La aserción debe coincidir con el mensaje real del ViewModel
        assertEquals("Contraseña incorrecta.", viewModel.loginError.value)
    }

    @Test
    fun `requestHelp adds request and filters by role`() = runTest {
        // ACTIVACIÓN DEL FLUJO: Sin esto, requests.value siempre es [] debido a WhileSubscribed
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.requests.collect() }

        // 1. Preparamos un administrador y logueamos
        val admin = User("admin_999999999", "Admin", "123", "999999999", UserRole.ADMIN)
        repository.saveUser(admin)
        viewModel.login("999999999", "123") { }
        advanceUntilIdle()

        // 2. Creamos la solicitud de ayuda
        viewModel.requestHelp(HelpType.EMERGENCY, "Ayuda urgente")
        advanceUntilIdle()
        
        // 3. Verificamos que aparezca en la lista del admin (RA8.b)
        val requests = viewModel.requests.value
        assertEquals(1, requests.size)
        assertEquals(HelpType.EMERGENCY, requests[0].type)
    }

    @Test
    fun `logout clears session and user`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.requests.collect() }

        repository.saveSession("user_123")
        viewModel.logout()
        advanceUntilIdle()
        
        assertNull(viewModel.currentUser.value)
        assertNull(repository.getSavedSession())
    }
}

/**
 * FakeHelpRepository: Doble de prueba para el repositorio (RA8.b).
 */
class FakeHelpRepository : HelpRepository {
    private val _requests = MutableStateFlow<List<HelpRequest>>(emptyList())
    private val _users = mutableMapOf<String, User>()
    private var _savedSessionId: String? = null

    override fun getRequests(): Flow<List<HelpRequest>> = _requests

    override suspend fun addRequest(request: HelpRequest) {
        _requests.value += request
    }

    override suspend fun updateRequestStatus(requestId: String, status: RequestStatus) {
        _requests.value = _requests.value.map {
            if (it.id == requestId) it.copy(status = status) else it
        }
    }

    override suspend fun assignRequest(requestId: String, status: RequestStatus, volunteerId: String) {
        _requests.value = _requests.value.map {
            if (it.id == requestId) it.copy(status = status, assignedVolunteerId = volunteerId) else it
        }
    }

    override suspend fun deleteRequest(requestId: String) {
        _requests.value = _requests.value.filter { it.id != requestId }
    }

    override fun getMessagesForRequest(requestId: String): Flow<List<ChatMessage>> = MutableStateFlow(emptyList())
    override suspend fun sendMessage(message: ChatMessage) {}

    override suspend fun getUserByName(name: String): User? = _users.values.find { it.name == name }
    override suspend fun getUserByPhoneNumber(phoneNumber: String): User? = _users.values.find { it.phoneNumber == phoneNumber }
    override suspend fun getUserById(id: String): User? = _users[id]
    override suspend fun saveUser(user: User) { _users[user.id] = user }

    override suspend fun saveSession(userId: String) { _savedSessionId = userId }
    override suspend fun getSavedSession(): String? = _savedSessionId
    override suspend fun clearSession() { _savedSessionId = null }
}
