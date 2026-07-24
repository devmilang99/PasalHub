package com.psl.pasalhub.dashboard.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.psl.pasalhub.core.database.data.UserEntity
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.home.domain.HomeRepository
import com.psl.pasalhub.dashboard.products.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favoriteIds: StateFlow<Set<Int>> = repository.getFavoriteIds()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val cartItemIds: StateFlow<Set<Int>> = repository.getCartItems()
        .map { items -> items.map { it.productId }.toSet() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isFilterActive: StateFlow<Boolean> = combine(
        _selectedCategory, _maxPrice, _sellerLocation, _sortBy
    ) { category, price, location, sort ->
        category != "all" || price < 500f || location != "All Locations" || sort != "Relevance"
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val paginatedProducts: Flow<PagingData<ProductDto>> = combine(
        _selectedCategory,
        _refreshTrigger
    ) { category, _ ->
        repository.getProductsPaged(category).cachedIn(viewModelScope)
    }.flatMapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val homeProductsState: StateFlow<Resource<List<ProductDto>>> = combine(
        combine(
            _selectedCategory,
            _searchQuery,
            _maxPrice,
            _sellerLocation,
            _sortBy
        ) { c, q, p, l, s ->
            Filters(c, q, p, l, s)
        }.debounce(300.milliseconds).distinctUntilChanged(),
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
            } else {
                repository.addToCart(product)
            }
        }
    }
}
