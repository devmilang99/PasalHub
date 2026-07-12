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

    private val _maxPrice = MutableStateFlow(500f)
    val maxPrice: StateFlow<Float> = _maxPrice.asStateFlow()

    private val _sellerLocation = MutableStateFlow("All Locations")
    val sellerLocation: StateFlow<String> = _sellerLocation.asStateFlow()

    private val _sortBy = MutableStateFlow("Relevance")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteIds: StateFlow<Set<Int>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val cartItemIds: StateFlow<Set<Int>> = repository.getCartItems()
        .map { items -> items.map { it.productId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    val isFilterActive: StateFlow<Boolean> = combine(
        _selectedCategory, _maxPrice, _sellerLocation, _sortBy
    ) { category, price, location, sort ->
        category != "all" || price < 500f || location != "All Locations" || sort != "Relevance"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val homeProductsState: StateFlow<Resource<List<ProductDto>>> = combine(
        combine(_selectedCategory, _searchQuery, _maxPrice, _sellerLocation, _sortBy) { c, q, p, l, s -> 
            Filters(c, q, p, l, s)
        },
        _refreshTrigger
    ) { filters, _ ->
        val productsFlow = if (filters.category == "all") {
            repository.getProducts()
        } else {
            repository.getProductsByCategory(filters.category)
        }
        
        productsFlow.map { resource ->
            when (resource) {
                is Resource.Loading -> Resource.Loading
                is Resource.Error -> Resource.Error(resource.message)
                is Resource.Success -> {
                    var filtered = resource.data.filter {
                        it.title.contains(filters.query, ignoreCase = true) ||
                                it.description.contains(filters.query, ignoreCase = true)
                    }
                    
                    // Filter by price
                    filtered = filtered.filter { it.price <= filters.maxPrice }
                    
                    // Sort
                    filtered = when (filters.sort) {
                        "Price: Low to High" -> filtered.sortedBy { it.price }
                        "Price: High to Low" -> filtered.sortedByDescending { it.price }
                        else -> filtered
                    }
                    
                    Resource.Success(filtered)
                }
            }
        }
    }.flatMapLatest { it }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading
    )

    private data class Filters(
        val category: String,
        val query: String,
        val maxPrice: Float,
        val location: String,
        val sort: String
    )

    fun setFilters(category: String, maxPrice: Float, location: String, sort: String) {
        _selectedCategory.value = category
        _maxPrice.value = maxPrice
        _sellerLocation.value = location
        _sortBy.value = sort
    }

    fun resetFilters() {
        _selectedCategory.value = "all"
        _maxPrice.value = 500f
        _sellerLocation.value = "All Locations"
        _sortBy.value = "Relevance"
    }

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
            if (cartItemIds.value.contains(product.id)) {
                repository.removeFromCart(product.id)
                _snackbarMessage.emit("${product.title} removed from cart")
            } else {
                repository.addToCart(product)
            }
        }
    }
}
