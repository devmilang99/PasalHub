package com.example.core.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.application.domain.AppPreferencesRepository
import com.example.core.database.data.UserEntity
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.Resource
import com.example.dashboard.products.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.core.content.edit
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainViewModel(
    private val repository: ProductRepository,
    private val appPrefs: AppPreferencesRepository,
) : ViewModel() {

    // Theme state (false = Light, true = Dark)
    // Note: Theme state is now primarily handled in InitialViewModel for the initial flow,
    // but kept here for backward compatibility with screens still using MainViewModel.
    // Ideally, this should be moved to a Shared UI State or Settings ViewModel.
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _lastEmail = MutableStateFlow("")

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("dark_theme", true)
        _lastEmail.value = prefs.getString("last_email", "") ?: ""
    }

    // Biometric State
    private val _biometricAuthenticated = MutableStateFlow(false)

    // Notification Event for UI triggers
    val notificationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)

    // User State from DB
    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    fun logout(context: Context) {
        viewModelScope.launch {
            val email = currentUser.value?.email ?: "guest"
            repository.clearUser()
            repository.clearCart()
            _biometricAuthenticated.value = false
            
            // Clear preferences related to this user and app settings
            val settingsPrefs = context.getSharedPreferences("pasalhub_settings", Context.MODE_PRIVATE)
            settingsPrefs.edit { 
                remove("last_email")
            }
            _lastEmail.value = ""

            val pointsPrefs = context.getSharedPreferences("pasalhub_points", Context.MODE_PRIVATE)
            pointsPrefs.edit {
                remove("pts_$email")
            }

            val favPrefs = context.getSharedPreferences("pasalhub_favorites", Context.MODE_PRIVATE)
            favPrefs.edit {
                remove("fav_set")
            }


            // Note: We keep onboarding_done and theme_set true as they are app-wide setup,
            // but if "all details" meant a factory reset, we'd clear those too.
            // Given "dont ask again" for flow, we keep those flags.
        }
    }



    // Products State
    private val _selectedCategory = MutableStateFlow("all")

    private val _searchQuery = MutableStateFlow("")

    private val _refreshTrigger = MutableStateFlow(0L)

    // AI Search State (Moved to AiSearchViewModel)

    // Home Products State (Traditional filtering)
    @OptIn(ExperimentalCoroutinesApi::class)
    val homeProductsState: StateFlow<Resource<List<ProductDto>>> = combine(
        _selectedCategory,
        _searchQuery,
        _refreshTrigger
    ) { category, query, _ ->
        Pair(category, query)
    }.flatMapLatest { (category, query) ->
        val flow = if (category == "all") {
            repository.getProducts()
        } else {
            repository.getProductsByCategory(category)
        }
        
        flow.map { resource ->
            when (resource) {
                is Resource.Loading -> Resource.Loading
                is Resource.Error -> Resource.Error(resource.message)
                is Resource.Success -> {
                    val filtered = resource.data.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true)
                    }
                    Resource.Success(filtered)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading
    )

    init {
        // Collect local notification events and emit them to the app-wide preferences
        viewModelScope.launch {
            notificationEvent.collect { message ->
                appPrefs.emitNotification(message)
            }
        }
    }
}
