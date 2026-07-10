package com.example.dashboard.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboard.profile.domain.ProfileRepository
import com.example.core.database.data.UserEntity
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteIds: StateFlow<Set<Int>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val homeProductsState: StateFlow<Resource<List<ProductDto>>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    private val _memberPoints = MutableStateFlow(250)
    val memberPoints: StateFlow<Int> = _memberPoints.asStateFlow()

    private val _userPassword = MutableStateFlow("password")
    val userPassword: StateFlow<String> = _userPassword.asStateFlow()

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun loadMemberPoints() {
        viewModelScope.launch {
            currentUser.value?.email?.let { email ->
                repository.getMemberPoints(email).collect {
                    _memberPoints.value = it
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
            repository.updatePassword(email, newPass)
            _userPassword.value = newPass
        }
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }
}
