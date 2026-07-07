package com.example.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.GeminiSearchRouter
import com.example.ai.domain.model.SearchIntent
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AiSearchViewModel(
    private val repository: ShopRepository,
    private val geminiRouter: GeminiSearchRouter
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0L)

    // AI Search State
    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _aiSearchError = MutableStateFlow<String?>(null)
    val aiSearchError: StateFlow<String?> = _aiSearchError.asStateFlow()

    private val _aiSearchIntent = MutableStateFlow<SearchIntent?>(null)
    val aiSearchIntent: StateFlow<SearchIntent?> = _aiSearchIntent.asStateFlow()

    private val _recommendationMessage = MutableStateFlow<String?>(null)
    val recommendationMessage: StateFlow<String?> = _recommendationMessage.asStateFlow()

    val notificationEvent = MutableSharedFlow<String>()

    // AI Products State (AI-driven filtering)
    val aiProductsState: StateFlow<Resource<List<ProductDto>>> = combine(
        _aiSearchIntent,
        _refreshTrigger
    ) { aiIntent, _ ->
        aiIntent
    }.flatMapLatest { aiIntent ->
        if (aiIntent == null) {
            _recommendationMessage.value = null
            return@flatMapLatest flow { emit(Resource.Success(emptyList<ProductDto>())) }
        }

        _recommendationMessage.value = aiIntent.product_summary

        val flow = if (aiIntent.fields?.category != null && aiIntent.fields.category != "all") {
            repository.getProductsByCategory(aiIntent.fields.category!!)
        } else {
            repository.getProducts()
        }
        
        flow.map { resource ->
            when (resource) {
                is Resource.Loading -> Resource.Loading
                is Resource.Error -> Resource.Error(resource.message)
                is Resource.Success -> {
                    var filtered = resource.data
                    val fields = aiIntent.fields
                    
                    if (fields != null) {
                        // Advanced Filtering Logic
                        fun matchesKeywords(product: ProductDto, keywords: String?): Boolean {
                            if (keywords == null) return true
                            val searchWords = keywords.lowercase().split(" ").filter { it.length > 2 }
                            if (searchWords.isEmpty()) return product.title.contains(keywords, ignoreCase = true)
                            return searchWords.any { word ->
                                product.title.contains(word, ignoreCase = true) || 
                                product.description.contains(word, ignoreCase = true)
                            }
                        }

                        fun matchesColor(product: ProductDto, color: String?): Boolean {
                            if (color == null) return true
                            return product.title.contains(color, ignoreCase = true) || 
                                   product.description.contains(color, ignoreCase = true)
                        }

                        fun matchesBrand(product: ProductDto, brand: String?): Boolean {
                            if (brand == null) return true
                            return product.title.contains(brand, ignoreCase = true)
                        }

                        // 1. Try strict filtering
                        val strictResults = filtered.filter { product ->
                            val matchKeywords = matchesKeywords(product, fields.keywords)
                            val matchColor = matchesColor(product, fields.color)
                            val matchPrice = fields.price_max == null || product.price <= fields.price_max
                            val matchBrand = matchesBrand(product, fields.brand)
                            val matchRating = fields.min_rating == null || (3..5).random() >= fields.min_rating // Sample rating simulation
                            
                            matchKeywords && matchColor && matchPrice && matchBrand && matchRating
                        }
                        
                        if (strictResults.isNotEmpty()) {
                            filtered = strictResults
                        } else {
                            // 2. Fallback logic: Relax constraints
                            val relaxedResults = filtered.filter { product ->
                                matchesKeywords(product, fields.keywords) && 
                                (fields.price_max == null || product.price <= fields.price_max * 1.2) // 20% price buffer
                            }
                            
                            if (relaxedResults.isNotEmpty()) {
                                _recommendationMessage.value = "We found some great options similar to your request!"
                                filtered = relaxedResults
                            } else {
                                filtered = emptyList()
                            }
                        }
                    }

                    // Apply sorting from AI
                    filtered = when (aiIntent.sort_by) {
                        "price_asc" -> filtered.sortedBy { it.price }
                        "price_desc" -> filtered.sortedByDescending { it.price }
                        else -> filtered
                    }
                    
                    Resource.Success(filtered)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Success(emptyList())
    )

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    fun performAiSearch(query: String) {
        if (query.isBlank()) return
        
        // Add to history
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query)
        currentHistory.add(0, query)
        _searchHistory.value = currentHistory.take(5)

        viewModelScope.launch {
            _isAiProcessing.value = true
            _aiSearchError.value = null
            _recommendationMessage.value = null
            _aiSearchIntent.value = null // Clear previous intent to not preserve state
            
            try {
                val intent = geminiRouter.routeSearch(query)
                
                if (intent != null && intent.is_valid) {
                    _aiSearchIntent.value = intent
                    _aiSearchError.value = null
                    
                    notificationEvent.emit(intent.display_message ?: "AI found matches for your request!")
                } else {
                    _aiSearchIntent.value = null
                    _aiSearchError.value = intent?.fallback_message ?: "I couldn't understand that search. Try being more specific about products."
                    notificationEvent.emit(_aiSearchError.value ?: "AI search failed")
                }
            } catch (e: Exception) {
                _aiSearchError.value = "AI Search is currently unavailable."
            } finally {
                _isAiProcessing.value = false
            }
        }
    }

    fun clearSearch() {
        _aiSearchIntent.value = null
        _aiSearchError.value = null
        _recommendationMessage.value = null
        _isAiProcessing.value = false
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

    fun refreshProducts() {
        _refreshTrigger.value = System.currentTimeMillis()
    }
}
