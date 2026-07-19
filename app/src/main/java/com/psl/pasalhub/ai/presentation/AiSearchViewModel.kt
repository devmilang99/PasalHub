package com.psl.pasalhub.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.ai.data.GeminiSearchRouter
import com.psl.pasalhub.ai.domain.model.SearchFields
import com.psl.pasalhub.ai.domain.model.SearchIntent
import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.content
import org.json.JSONArray
import org.json.JSONObject
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.dashboard.products.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AiSearchViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val geminiRouter: GeminiSearchRouter
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _manualProductList = MutableStateFlow<List<ProductDto>?>(null)
    private val chat by lazy { geminiRouter.startChat() }

    // AI Search State
    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _aiSearchError = MutableStateFlow<String?>(null)
    val aiSearchError: StateFlow<String?> = _aiSearchError.asStateFlow()

    private val _aiSearchIntent = MutableStateFlow<SearchIntent?>(null)

    private val _recommendationMessage = MutableStateFlow<String?>(null)
    val recommendationMessage: StateFlow<String?> = _recommendationMessage.asStateFlow()

    val notificationEvent = MutableSharedFlow<String>()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // AI Products State (AI-driven filtering)
    val aiProductsState: StateFlow<Resource<List<ProductDto>>> = combine(
        _aiSearchIntent,
        _manualProductList,
        _refreshTrigger
    ) { aiIntent, manualList, _ ->
        aiIntent to manualList
    }.flatMapLatest { (aiIntent, manualList) ->
        if (manualList != null) {
            return@flatMapLatest flow { emit(Resource.Success(manualList)) }
        }
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
                    var filtered = applySearchFilters(resource.data, aiIntent.fields)

                    if (filtered.isEmpty() && aiIntent.fields != null) {
                        _recommendationMessage.value =
                            "We couldn't find exact matches, showing all products instead."
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


    fun performAiSearch(query: String) {
        if (query.isBlank()) return

        _manualProductList.value = null
        // Add to history
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query)
        currentHistory.add(0, query)
        _searchHistory.value = currentHistory.take(5)

        viewModelScope.launch {
            _isAiProcessing.value = true
            _aiSearchError.value = null
            _recommendationMessage.value = null
            _aiSearchIntent.value = null

            try {
                var response = chat.sendMessage(query)

                // Function Calling Loop
                var functionCalls = response.functionCalls
                while (functionCalls.isNotEmpty()) {
                    val parts = mutableListOf<FunctionResponsePart>()
                    for (call in functionCalls) {
                        val result = handleFunctionCall(call)
                        parts.add(FunctionResponsePart(call.name, result))
                    }

                    response = chat.sendMessage(content("function") {
                        parts.forEach { part(it) }
                    })
                    functionCalls = response.functionCalls
                }

                val finalMessage = response.text
                if (!finalMessage.isNullOrBlank()) {
                    _recommendationMessage.value = finalMessage
                    notificationEvent.emit(finalMessage.take(100))
                }
            } catch (e: Exception) {
                _aiSearchError.value = "AI Assistant is currently unavailable."
                notificationEvent.emit("Error: ${e.message}")
            } finally {
                _isAiProcessing.value = false
            }
        }
    }

    private suspend fun handleFunctionCall(call: FunctionCallPart): JSONObject {
        return when (call.name) {
            "search_products" -> {
                val args = call.args
                val keywords = args["keywords"]
                val category = args["category"]
                val priceMax = args["price_max"]?.toDoubleOrNull()
                val sortBy = args["sort_by"]

                val fields = SearchFields(
                    keywords = keywords,
                    category = category,
                    brand = args["brand"],
                    color = args["color"],
                    size = null,
                    price_max = priceMax,
                    min_rating = null
                )

                // Execute search
                val searchIntent = SearchIntent(
                    is_valid = true,
                    fields = fields,
                    sort_by = sortBy,
                    fallback_message = null
                )

                // Fetch actual data to return to Gemini
                val allProducts = if (category != null && category != "all") {
                    repository.getProductsByCategory(category).first()
                } else {
                    repository.getProducts().first()
                }

                val filtered = if (allProducts is Resource.Success) {
                    var results = applySearchFilters(allProducts.data, fields)
                    results = when (sortBy) {
                        "price_asc" -> results.sortedBy { it.price }
                        "price_desc" -> results.sortedByDescending { it.price }
                        else -> results
                    }
                    results.take(5)
                } else emptyList()

                // We update the intent to trigger the existing filtering logic in aiProductsState
                _aiSearchIntent.value = searchIntent

                JSONObject().apply {
                    put("status", "success")
                    put("count", filtered.size)
                    val productsArray = JSONArray()
                    filtered.forEach { product ->
                        productsArray.put(JSONObject().apply {
                            put("id", product.id)
                            put("title", product.title)
                            put("price", product.price)
                            put("category", product.category)
                        })
                    }
                    put("products", productsArray)
                }
            }

            "get_product_details" -> {
                val productId = call.args["product_id"]?.toIntOrNull() ?: 0
                val product = repository.getProductById(productId)
                if (product != null) {
                    _manualProductList.value = listOf(product)
                    JSONObject().apply {
                        put("status", "success")
                        put("title", product.title)
                        put("description", product.description)
                        put("price", product.price)
                        put("category", product.category)
                    }
                } else {
                    JSONObject().apply {
                        put("status", "error")
                        put("message", "Product not found.")
                    }
                }
            }

            "apply_discount" -> {
                val code = call.args["coupon_code"]
                if (code?.uppercase() == "PASAL10") {
                    JSONObject().apply {
                        put("status", "success")
                        put("discount", "10%")
                        put("message", "Coupon applied! You saved 10%.")
                    }
                } else {
                    JSONObject().apply {
                        put("status", "invalid")
                        put("message", "This coupon is invalid or expired.")
                    }
                }
            }

            else -> JSONObject().apply { put("status", "unknown_function") }
        }
    }

    fun clearSearch() {
        _aiSearchIntent.value = null
        _manualProductList.value = null
        _aiSearchError.value = null
        _recommendationMessage.value = null
        _isAiProcessing.value = false
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

    private fun applySearchFilters(
        products: List<ProductDto>,
        fields: SearchFields?
    ): List<ProductDto> {
        if (fields == null) return products

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
        val strictResults = products.filter { product ->
            val matchKeywords = matchesKeywords(product, fields.keywords)
            val matchColor = matchesColor(product, fields.color)
            val matchPrice = fields.price_max == null || product.price <= fields.price_max
            val matchBrand = matchesBrand(product, fields.brand)
            val matchRating = fields.min_rating == null || (3..5).random() >= fields.min_rating

            matchKeywords && matchColor && matchPrice && matchBrand && matchRating
        }

        if (strictResults.isNotEmpty()) return strictResults

        // 2. Fallback logic: Relax constraints
        return products.filter { product ->
            matchesKeywords(product, fields.keywords) &&
                    (fields.price_max == null || product.price <= fields.price_max * 1.2)
        }
    }
}
