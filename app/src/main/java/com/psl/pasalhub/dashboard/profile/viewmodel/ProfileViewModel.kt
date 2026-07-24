package com.psl.pasalhub.dashboard.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.core.sync.SyncManager
import com.psl.pasalhub.core.sync.SyncType
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.dashboard.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    init {
        // Fetch latest favorites from other devices
        syncFavorites()
    }

    fun syncFavorites() {
        viewModelScope.launch {
            syncManager.triggerSync(SyncType.FAVORITES, fetch = true)
        }
    }

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteIds: StateFlow<Set<Int>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isSyncing: StateFlow<Boolean> = syncManager.isFavoritesSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val homeProductsState: StateFlow<Resource<List<ProductDto>>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    private val _memberPoints = MutableStateFlow(250)
    val memberPoints: StateFlow<Int> = _memberPoints.asStateFlow()

    private val _pointHistory =
        MutableStateFlow<List<com.psl.pasalhub.core.database.data.PointTransactionEntity>>(emptyList())
    val pointHistory: StateFlow<List<com.psl.pasalhub.core.database.data.PointTransactionEntity>> =
        _pointHistory.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pointHistoryPaged: kotlinx.coroutines.flow.Flow<PagingData<com.psl.pasalhub.core.database.data.PointTransactionEntity>> =
        currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getPointHistoryPaged(user.id).cachedIn(viewModelScope)
            } else {
                kotlinx.coroutines.flow.flowOf(PagingData.empty())
            }
        }

    private val _userPassword = MutableStateFlow("password")
    val userPassword: StateFlow<String> = _userPassword.asStateFlow()

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _passwordUpdateStatus =
        MutableStateFlow<PasswordUpdateStatus>(PasswordUpdateStatus.Idle)
    val passwordUpdateStatus: StateFlow<PasswordUpdateStatus> = _passwordUpdateStatus.asStateFlow()

    fun loadMemberPoints() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.getMemberPoints(user.email).collect {
                    _memberPoints.value = it
                }
            }
        }
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.getPointHistory(user.id).collect { history ->
                    if (history.isEmpty()) {
                        repository.addPoints(user.id, 250, "Welcome Bonus")
                    }
                    _pointHistory.value = history
                }
            }
        }
    }

    fun loadPassword(email: String) {
        viewModelScope.launch {
            repository.getPassword(email).collect {
                _userPassword.value = it
            }
        }
    }

    fun loadFavorites() {
        // Already loaded via favoriteIds stateIn
    }

    fun updateUserAddress(address: String) {
        viewModelScope.launch {
            repository.updateAddress(address)
        }
    }

    fun updatePassword(email: String, newPass: String) {
        viewModelScope.launch {
            _passwordUpdateStatus.value = PasswordUpdateStatus.Loading
            try {
                repository.updatePassword(email, newPass)
                _userPassword.value = newPass
                _passwordUpdateStatus.value = PasswordUpdateStatus.Success
            } catch (e: Exception) {
                _passwordUpdateStatus.value =
                    PasswordUpdateStatus.Error(e.message ?: "Failed to update password")
            }
        }
    }

    fun resetPasswordStatus() {
        _passwordUpdateStatus.value = PasswordUpdateStatus.Idle
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }
}

sealed class PasswordUpdateStatus {
    object Idle : PasswordUpdateStatus()
    object Loading : PasswordUpdateStatus()
    object Success : PasswordUpdateStatus()
    data class Error(val message: String) : PasswordUpdateStatus()
}
