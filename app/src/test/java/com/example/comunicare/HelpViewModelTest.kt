package com.example.comunicare

import com.example.comunicare.domain.model.HelpRequest
import com.example.comunicare.domain.model.HelpType
import com.example.comunicare.domain.model.RequestStatus
import com.example.comunicare.domain.repository.HelpRepository
import com.example.comunicare.domain.use_case.AddHelpRequestUseCase
import com.example.comunicare.domain.use_case.GetHelpRequestsUseCase
import com.example.comunicare.domain.use_case.UpdateHelpRequestStatusUseCase
import com.example.comunicare.ui.viewmodel.HelpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HelpViewModelTest {

    private lateinit var viewModel: HelpViewModel
    private lateinit var repository: FakeHelpRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHelpRepository()
        val getHelpRequestsUseCase = GetHelpRequestsUseCase(repository)
        val addHelpRequestUseCase = AddHelpRequestUseCase(repository)
        val updateHelpRequestStatusUseCase = UpdateHelpRequestStatusUseCase(repository)
        viewModel = HelpViewModel(getHelpRequestsUseCase, addHelpRequestUseCase, updateHelpRequestStatusUseCase)
    }

    @Test
    fun `requestHelp adds a new request to the list`() = runTest {
        viewModel.requestHelp(HelpType.SHOPPING, "Comprar pan", "Juan")
        advanceUntilIdle()
        
        val requests = viewModel.requests.value
        assertEquals(1, requests.size)
        assertEquals("Comprar pan", requests[0].description)
        assertEquals(HelpType.SHOPPING, requests[0].type)
    }

    @Test
    fun `updateStatus changes the status of an existing request`() = runTest {
        viewModel.requestHelp(HelpType.MEDICATION, "Medicina", "Ana")
        advanceUntilIdle()
        
        val requestId = viewModel.requests.value[0].id
        viewModel.updateStatus(requestId, RequestStatus.COMPLETED)
        advanceUntilIdle()
        
        assertEquals(RequestStatus.COMPLETED, viewModel.requests.value[0].status)
    }
}

class FakeHelpRepository : HelpRepository {
    private val _requests = MutableStateFlow<List<HelpRequest>>(emptyList())
    override fun getRequests(): Flow<List<HelpRequest>> = _requests

    override suspend fun addRequest(request: HelpRequest) {
        _requests.value += request
    }

    override suspend fun updateRequestStatus(requestId: String, status: RequestStatus) {
        _requests.value = _requests.value.map {
            if (it.id == requestId) it.copy(status = status) else it
        }
    }

    override suspend fun deleteRequest(requestId: String) {
        _requests.value = _requests.value.filter { it.id != requestId }
    }
}
