package com.psl.pasalhub.ai.presentation

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psl.pasalhub.ai.data.Content
import com.psl.pasalhub.ai.data.FunctionCall
import com.psl.pasalhub.ai.data.FunctionResponse
import com.psl.pasalhub.ai.data.GeminiSearchRouter
import com.psl.pasalhub.ai.data.InlineData
import com.psl.pasalhub.ai.data.Part
import com.psl.pasalhub.ai.domain.model.AiChatMessage
import com.psl.pasalhub.ai.domain.model.SearchFields
import com.psl.pasalhub.ai.domain.model.SearchIntent
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.networking.remote.ProductDto
import com.psl.pasalhub.dashboard.products.repository.ProductRepository
import com.psl.pasalhub.dashboard.products.repository.Resource
import com.psl.pasalhub.visualsearch.VisualSearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AiSearchViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val geminiRouter: GeminiSearchRouter,
    private val visualSearchEngine: VisualSearchEngine,
    private val appPrefs: AppPreferencesRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _manualProductList = MutableStateFlow<List<ProductDto>?>(null)
    private val _chatHistory = mutableListOf<Content>()
    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private var searchJob: Job? = null

    companion object {
        private const val MAX_CHAT_HISTORY = 10
        const val MAX_REQUESTS = 5
    }

    // AI Search State
    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _showCancelDialog = MutableStateFlow(false)
    val showCancelDialog: StateFlow<Boolean> = _showCancelDialog.asStateFlow()

    private val _isPaused = MutableStateFlow(false)

    private val _aiSearchError = MutableStateFlow<String?>(null)
    val aiSearchError: StateFlow<String?> = _aiSearchError.asStateFlow()

    private val _aiSearchIntent = MutableStateFlow<SearchIntent?>(null)

    private val _recommendationMessage = MutableStateFlow<String?>(null)
    val recommendationMessage: StateFlow<String?> = _recommendationMessage.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private var firstPrompt: String? = null
    private var firstBitmap: Bitmap? = null
    private val _retryCount = MutableStateFlow(0)
    val retryCount: StateFlow<Int> = _retryCount.asStateFlow()

    private val _requestCount = MutableStateFlow(0)
    val requestCount: StateFlow<Int> = _requestCount.asStateFlow()

    init {
        Log.d("AiSearchViewModel", "Initializing AI Search System...")
        _requestCount.value = appPrefs.getAiRequestCount()
        viewModelScope.launch {
            try {
                val modelList = geminiRouter.listAvailableModels()
                val models = modelList.models.map { it.name.substringAfterLast("/") }
                Log.d("AiSearchViewModel", "Available Gemini Models: $models")
            } catch (e: Exception) {
                Log.e("AiSearchViewModel", "Failed to list models: ${e.message}")
            }
        }
    }

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
            repository.getProductsByCategory(aiIntent.fields.category)
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

        if (firstPrompt == null && firstBitmap == null) {
            firstPrompt = query
        }

        searchJob?.cancel()
        _isAiProcessing.value = true
        _aiSearchError.value = null
        _manualProductList.value = null
        _aiSearchIntent.value = null

        // Add to UI messages
        _messages.value = _messages.value + AiChatMessage(text = query, isUser = true)

        // Add to history
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.remove(query)
        currentHistory.add(0, query)
        _searchHistory.value = currentHistory.take(5)

        executeSearch(Content(role = "user", parts = listOf(Part(text = query))))
    }

    fun performVisualSearch(bitmap: Bitmap) {
        if (firstPrompt == null && firstBitmap == null) {
            firstBitmap = bitmap
        }
        _isAiProcessing.value = true
        _manualProductList.value = null

        // Add to UI messages
        _messages.value = _messages.value + AiChatMessage(image = bitmap, isUser = true)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _aiSearchError.value = null
            _recommendationMessage.value = "Processing image..."

            try {
                // 1. Local TFLite Embedding Extraction (for future vector search)
                _recommendationMessage.value = "Extracting visual features..."
                when (val result = visualSearchEngine.extractEmbeddings(bitmap)) {
                    is VisualSearchEngine.VisualSearchResult.Success -> {
                        Log.d(
                            "AiSearchViewModel",
                            "Visual Search: Local embeddings extracted (Size: ${result.embeddings.size})"
                        )
                    }

                    is VisualSearchEngine.VisualSearchResult.InterpreterNull -> {
                        Log.e("AiSearchViewModel", "Visual Search: Interpreter is null")
                    }

                    is VisualSearchEngine.VisualSearchResult.Error -> {
                        Log.e(
                            "AiSearchViewModel",
                            "Visual Search extraction failed: ${result.message}"
                        )
                    }
                }

                // 2. Multi-modal identification via Gemini
                _recommendationMessage.value = "Identifying product with AI..."
                val base64Image = bitmapToBase64(bitmap)
                val visualContent = Content(
                    role = "user",
                    parts = listOf(
                        Part(text = "Identify the product in this image and search for similar items in the catalog. Be concise."),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )

                executeSearchInternal(visualContent)
            } catch (e: Exception) {
                Log.e("AiSearchViewModel", "Visual Search failed", e)
                _aiSearchError.value = "Failed to process image. Please try again after 5 minutes."
                _isAiProcessing.value = false
            }
        }
    }

    private fun executeSearch(userContent: Content) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearchInternal(userContent)
        }
    }

    private suspend fun executeSearchInternal(userContent: Content) {
        if (_requestCount.value >= MAX_REQUESTS) {
            _aiSearchError.value =
                "You've reached the AI usage limit for today. Please come back tomorrow."
            _isAiProcessing.value = false
            return
        }

        _isAiProcessing.value = true
        _aiSearchError.value = null
        _aiSearchIntent.value = null
        _isPaused.value = false
        _showCancelDialog.value = false

        try {
            _requestCount.value += 1
            appPrefs.incrementAiRequestCount()
            addToHistory(userContent)
            checkPause()
            var response = geminiRouter.routeSearchWithContent(_chatHistory)

            // Function Calling Loop
            var candidate = response.candidates?.firstOrNull()
            var parts = candidate?.content?.parts ?: emptyList()
            var functionCalls = parts.mapNotNull { it.functionCall ?: it.functionCallLegacy }

            while (functionCalls.isNotEmpty()) {
                checkPause()
                val intermediateText = parts.firstOrNull { it.text != null }?.text
                if (!intermediateText.isNullOrBlank()) {
                    _messages.value += AiChatMessage(text = intermediateText, isUser = false)
                }

                addToHistory(candidate!!.content!!)

                val responseParts = mutableListOf<Part>()
                for (call in functionCalls) {
                    val result = handleFunctionCall(call)
                    responseParts.add(
                        Part(
                            functionResponse = FunctionResponse(
                                name = call.name,
                                response = result,
                                id = call.id
                            )
                        )
                    )
                }

                val responseContent = Content(role = "user", parts = responseParts)
                addToHistory(responseContent)

                checkPause()
                response = geminiRouter.routeSearchWithContent(_chatHistory)
                candidate = response.candidates?.firstOrNull()
                parts = candidate?.content?.parts ?: emptyList()
                functionCalls = parts.mapNotNull { it.functionCall }
            }

            val finalMessage = parts.firstOrNull { it.text != null }?.text
            if (!finalMessage.isNullOrBlank()) {
                _messages.value += AiChatMessage(text = finalMessage, isUser = false)
                _recommendationMessage.value = finalMessage
                appPrefs.emitNotification(finalMessage.take(100))
                addToHistory(candidate!!.content!!)
            } else if (functionCalls.isEmpty()) {
                _aiSearchError.value = "AI Assistant is currently unavailable."
            }
        } catch (e: Exception) {
            Log.e("AiSearchViewModel", "AI Search failed: ${e.message}", e)
            val errorMessage = e.message ?: ""
            if (errorMessage.contains("429") ||
                errorMessage.contains("quota", ignoreCase = true) ||
                errorMessage.contains("RESOURCE_EXHAUSTED")
            ) {
                _aiSearchError.value =
                    "You've reached the AI usage limit for today. Please come back tomorrow."
            } else {
                _aiSearchError.value =
                    "AI Assistant is currently unavailable. Please try again after 5 minutes."
            }
            appPrefs.emitNotification("Error: ${e.message}")
        } finally {
            _isAiProcessing.value = false
        }
    }

    private fun addToHistory(content: Content) {
        _chatHistory.add(content)
        while (_chatHistory.size > MAX_CHAT_HISTORY) {
            _chatHistory.removeAt(0)
        }
    }

    fun requestCancel() {
        if (_isAiProcessing.value) {
            _isPaused.value = true
            _showCancelDialog.value = true
        }
    }

    fun resumeSearch() {
        _isPaused.value = false
        _showCancelDialog.value = false
    }

    fun confirmCancelSearch() {
        _isPaused.value = false
        _showCancelDialog.value = false
        cancelSearch()
    }

    private fun isResultReady(): Boolean {
        val products = aiProductsState.value
        return products is Resource.Success && products.data.isNotEmpty()
    }

    private suspend fun checkPause() {
        if (_isPaused.value) {
            _isPaused.first { !it }
        }
    }

    private suspend fun handleFunctionCall(call: FunctionCall): JsonObject {
        val args = call.args ?: buildJsonObject { }
        return when (call.name) {
            "search_products" -> {
                val keywords = args["keywords"]?.jsonPrimitive?.content
                val category = args["category"]?.jsonPrimitive?.content
                val priceMax = args["price_max"]?.jsonPrimitive?.doubleOrNull
                val sortBy = args["sort_by"]?.jsonPrimitive?.content
                val brand = args["brand"]?.jsonPrimitive?.content
                val color = args["color"]?.jsonPrimitive?.content

                val fields = SearchFields(
                    keywords = keywords,
                    category = category,
                    brand = brand,
                    color = color,
                    size = null,
                    price_max = priceMax,
                    min_rating = null
                )

                Log.d(
                    "AiSearchViewModel",
                    "Tool 'search_products' called. Category: $category, Keywords: $keywords"
                )

                // Fetch actual data
                val allProductsResource = if (category != null && category != "all") {
                    repository.getProductsByCategory(category).filter { it !is Resource.Loading }
                        .first()
                } else {
                    repository.getProducts().filter { it !is Resource.Loading }.first()
                }

                val filtered = if (allProductsResource is Resource.Success) {
                    var results = applySearchFilters(allProductsResource.data, fields)
                    results = when (sortBy) {
                        "price_asc" -> results.sortedBy { it.price }
                        "price_desc" -> results.sortedByDescending { it.price }
                        else -> results
                    }
                    results
                } else {
                    emptyList()
                }

                // Update UI immediately with results
                _manualProductList.value = filtered
                _aiSearchIntent.value = SearchIntent(
                    is_valid = true,
                    fields = fields,
                    sort_by = sortBy,
                    fallback_message = null,
                    product_summary = "Finding products for you..."
                )

                buildJsonObject {
                    put("status", "success")
                    put("count", filtered.size)
                    putJsonArray("products") {
                        filtered.take(5).forEach { product ->
                            add(buildJsonObject {
                                put("id", product.id)
                                put("title", product.title)
                                put("price", product.price)
                                put("category", product.category)
                            })
                        }
                    }
                }
            }

            "get_product_details" -> {
                val productId = args["product_id"]?.jsonPrimitive?.intOrNull ?: 0
                val product = repository.getProductById(productId)
                if (product != null) {
                    _manualProductList.value = listOf(product)
                    buildJsonObject {
                        put("status", "success")
                        put("title", product.title)
                        put("description", product.description)
                        put("price", product.price)
                        put("category", product.category)
                    }
                } else {
                    buildJsonObject {
                        put("status", "error")
                        put("message", "Product not found.")
                    }
                }
            }

            "apply_discount" -> {
                val code = args["coupon_code"]?.jsonPrimitive?.content
                if (code?.uppercase() == "PASAL10") {
                    buildJsonObject {
                        put("status", "success")
                        put("discount", "10%")
                        put("message", "Coupon applied! You saved 10%.")
                    }
                } else {
                    buildJsonObject {
                        put("status", "invalid")
                        put("message", "This coupon is invalid or expired.")
                    }
                }
            }

            else -> buildJsonObject { put("status", "unknown_function") }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _aiSearchIntent.value = null
        _manualProductList.value = null
        _aiSearchError.value = null
        _recommendationMessage.value = null
        _isAiProcessing.value = false
        _chatHistory.clear()
        _messages.value = emptyList()
        firstPrompt = null
        firstBitmap = null
        _retryCount.value = 0
    }

    fun handleRetry() {
        if (_retryCount.value == 0) {
            _retryCount.value = 1
            val prompt = firstPrompt
            val bitmap = firstBitmap

            // We don't call clearSearch() here because we want to keep firstPrompt/firstBitmap
            // but we do need to clear the current error and processing state
            searchJob?.cancel()
            _aiSearchError.value = null
            _aiSearchIntent.value = null
            _manualProductList.value = null
            _messages.value = emptyList()
            _chatHistory.clear()

            if (prompt != null) {
                performAiSearch(prompt)
            } else if (bitmap != null) {
                performVisualSearch(bitmap)
            }
        } else {
            clearSearch()
        }
    }

    fun cancelSearch() {
        if (searchJob?.isActive == true) {
            searchJob?.cancel()
            _isAiProcessing.value = false
            _recommendationMessage.value = "Search stopped."
            _messages.value = _messages.value + AiChatMessage(
                text = "Processing stopped by user.",
                isUser = false
            )
        }
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun applySearchFilters(
        products: List<ProductDto>,
        fields: SearchFields?
    ): List<ProductDto> {
        if (fields == null) return products

        val keywords = fields.keywords?.lowercase()?.trim()
        val isGenericSearch = keywords in listOf(
            "latest",
            "best",
            "deals",
            "top",
            "trending"
        ) || keywords.isNullOrBlank()

        fun matchesKeywords(product: ProductDto, queryKeywords: String?): Boolean {
            if (queryKeywords == null || isGenericSearch) return true
            val searchWords = queryKeywords.lowercase().split(" ").filter { it.length > 2 }
            if (searchWords.isEmpty()) return product.title.contains(
                queryKeywords,
                ignoreCase = true
            )
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

        // 2. Fallback logic: Relax keywords
        val relaxedResults = products.filter { product ->
            (isGenericSearch || matchesKeywords(product, fields.keywords)) &&
                    (fields.price_max == null || product.price <= fields.price_max * 1.5)
        }

        if (relaxedResults.isNotEmpty()) return relaxedResults

        // 3. Final Fallback: If no keywords match, but we have a category, just show the products in that category
        return if (!fields.category.isNullOrBlank() && fields.category != "all") {
            products.take(10)
        } else {
            emptyList()
        }
    }
}
