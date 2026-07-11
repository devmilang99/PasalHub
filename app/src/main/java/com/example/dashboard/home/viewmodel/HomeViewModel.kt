package com.example.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboard.home.domain.HomeRepository
import com.example.core.database.data.UserEntity
import com.example.core.networking.remote.ProductDto
import com.example.dashboard.products.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteIds: StateFlow<Set<Int>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

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

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshProducts() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun updateUserAddress(address: String) {
        viewModelScope.launch {
            repository.updateUserAddress(address)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            repository.toggleTheme()
        }
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }

    fun addToCart(product: ProductDto) {
        viewModelScope.launch {
            repository.addToCart(product)
        }
    }
}
