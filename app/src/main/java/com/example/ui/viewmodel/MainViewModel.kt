package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.GeminiSearchRouter
import com.example.core.application.domain.AppPreferencesRepository
import com.example.core.database.data.CartItem
import com.example.core.database.data.OrderEntity
import com.example.core.database.data.UserEntity
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.core.content.edit

class MainViewModel(
    private val repository: ShopRepository,
    private val appPrefs: AppPreferencesRepository,
    private val geminiRouter: GeminiSearchRouter
) : ViewModel() {

    // Theme state (false = Light, true = Dark)
    // Note: Theme state is now primarily handled in InitialViewModel for the initial flow,
    // but kept here for backward compatibility with screens still using MainViewModel.
    // Ideally, this should be moved to a Shared UI State or Settings ViewModel.
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme(context: android.content.Context) {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit { putBoolean("dark_theme", newValue) }
    }

    fun setTheme(context: android.content.Context, dark: Boolean) {
        _isDarkTheme.value = dark
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit { putBoolean("dark_theme", dark).putBoolean("theme_set", true)}
    }

    private val _lastEmail = MutableStateFlow("")
    val lastEmail: StateFlow<String> = _lastEmail.asStateFlow()

    fun loadSettings(context: android.content.Context) {
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("dark_theme", true)
        _lastEmail.value = prefs.getString("last_email", "") ?: ""
    }

    // Biometric State
    private val _biometricAuthenticated = MutableStateFlow(false)
    val biometricAuthenticated: StateFlow<Boolean> = _biometricAuthenticated.asStateFlow()

    fun setBiometricAuthenticated(authenticated: Boolean) {
        _biometricAuthenticated.value = authenticated
    }

    // User State from DB
    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun registerUser(context: android.content.Context, name: String, email: String, dateOfBirth: String, address: String, rememberMe: Boolean, isGoogleUser: Boolean = false, profileImage: String? = null) {
        viewModelScope.launch {
            val user = UserEntity(
                email = email,
                name = name,
                dateOfBirth = dateOfBirth,
                address = address,
                isRemembered = rememberMe,
                isGoogleUser = isGoogleUser,
                profileImage = profileImage
            )
            repository.saveUser(user)
            
            // Save last email for autofill
            _lastEmail.value = email
            val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
            prefs.edit { putString("last_email", email) }
        }
    }

    fun logout(context: android.content.Context) {
        viewModelScope.launch {
            val email = currentUser.value?.email ?: "guest"
            repository.clearUser()
            repository.clearCart()
            _biometricAuthenticated.value = false
            
            // Clear preferences related to this user and app settings
            val settingsPrefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
            settingsPrefs.edit { 
                remove("last_email")
            }
            _lastEmail.value = ""

            val pointsPrefs = context.getSharedPreferences("pasalhub_points", android.content.Context.MODE_PRIVATE)
            pointsPrefs.edit {
                remove("pts_$email")
            }
            _memberPoints.value = 250

            val favPrefs = context.getSharedPreferences("pasalhub_favorites", android.content.Context.MODE_PRIVATE)
            favPrefs.edit {
                remove("fav_set")
            }
            _favoriteIds.value = emptySet()
            
            // Note: We keep onboarding_done and theme_set true as they are app-wide setup,
            // but if "all details" meant a factory reset, we'd clear those too.
            // Given "dont ask again" for flow, we keep those flags.
        }
    }

    fun isValidatedUser(email: String, pass: String): Boolean {
        // Simulation: Only specific users allowed
        val validUsers = mapOf(
            "admin@pasalhub.com" to "admin123",
            "premium@pasalhub.com" to "luxury",
            "test@example.com" to "password"
        )
        return validUsers[email] == pass
    }

    // Products State
    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)

    // AI Search State (Moved to AiSearchViewModel)

    // Home Products State (Traditional filtering)
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

    // AI Products State (Moved to AiSearchViewModel)

    fun refreshProducts() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // performAiSearch (Moved to AiSearchViewModel)

    // Cart State from DB
    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartTotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addToCart(product: ProductDto) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == product.id }
            if (existing != null) {
                repository.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                repository.addToCart(
                    CartItem(
                        productId = product.id,
                        title = product.title,
                        price = product.price,
                        description = product.description,
                        category = product.category,
                        image = product.image,
                        quantity = 1
                    )
                )
            }
            appPrefs.emitNotification("Successfully added ${product.title} to your cart!")
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartItem(item.copy(quantity = item.quantity + 1))
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity > 1) {
                repository.updateCartItem(item.copy(quantity = item.quantity - 1))
            } else {
                repository.deleteCartItem(item)
            }
        }
    }

    fun deleteCartItem(item: CartItem) {
        viewModelScope.launch {
            repository.deleteCartItem(item)
        }
    }

    fun deleteMultipleCartItems(itemsToDelete: List<CartItem>) {
        viewModelScope.launch {
            itemsToDelete.forEach {
                repository.deleteCartItem(it)
            }
        }
    }

    private val _memberPoints = MutableStateFlow(250) // starts with 250 welcome points
    val memberPoints: StateFlow<Int> = _memberPoints.asStateFlow()

    fun loadMemberPoints(context: android.content.Context) {
        val email = currentUser.value?.email ?: "guest"
        val prefs = context.getSharedPreferences("pasalhub_points", android.content.Context.MODE_PRIVATE)
        _memberPoints.value = prefs.getInt("pts_$email", 250)
    }

    fun addPoints(context: android.content.Context, points: Int) {
        val email = currentUser.value?.email ?: "guest"
        val prefs = context.getSharedPreferences("pasalhub_points", android.content.Context.MODE_PRIVATE)
        val current = prefs.getInt("pts_$email", 250)
        val updated = current + points
        prefs.edit().putInt("pts_$email", updated).apply()
        _memberPoints.value = updated
    }

    fun checkoutSelected(context: android.content.Context, selectedItems: List<CartItem>, finalTotal: Double, paymentMethod: String, appliedVoucher: String) {
        viewModelScope.launch {
            if (selectedItems.isNotEmpty()) {
                val summary = selectedItems.joinToString { "${it.title} x${it.quantity}" }
                val order = OrderEntity(
                    totalAmount = finalTotal,
                    itemsSummary = summary,
                    status = "Placing", // starts in 10s cancel window
                    quantity = selectedItems.sumOf { it.quantity },
                    price = selectedItems.firstOrNull()?.price ?: 0.0,
                    address = currentUser.value?.address ?: "Default Address, New York",
                    seller = "Pasal Hub",
                    date = System.currentTimeMillis()
                )
                repository.placeOrder(order)
                selectedItems.forEach {
                    repository.deleteCartItem(it)
                }
                
                // Rewards: 10% of total amount as member points, minimum 50
                val pointsReward = (finalTotal / 10).toInt().coerceAtLeast(50)
                addPoints(context, pointsReward)
            }
        }
    }

    init {
        // Resume simulation for any transitional orders
        viewModelScope.launch {
            repository.getOrders().first().forEach { order ->
                if (order.status in listOf("Placed", "Packaging", "Sent for Delivery")) {
                    startOrderTrackingSimulation(order.orderId)
                }
            }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isNotEmpty()) {
                val summary = items.joinToString { "${it.title} x${it.quantity}" }
                val order = OrderEntity(
                    totalAmount = cartTotal.value,
                    itemsSummary = summary,
                    status = "Placing", // starts in 10s cancel window
                    quantity = items.sumOf { it.quantity },
                    price = items.firstOrNull()?.price ?: 0.0,
                    address = currentUser.value?.address ?: "Default Address, New York",
                    seller = "Pasal Hub",
                    date = System.currentTimeMillis()
                )
                repository.placeOrder(order)
                appPrefs.emitNotification("Your order #ORD-${1000 + order.orderId} has been placed successfully!")
            }
        }
    }

    fun startOrderTrackingSimulation(orderId: Int) {
        viewModelScope.launch {
            val statusSequence = listOf("Placed", "Packaging", "Sent for Delivery", "Delivered", "Completed")
            while (true) {
                kotlinx.coroutines.delay(12000) // progress every 12 seconds
                val currentOrders = repository.getOrders().first()
                val order = currentOrders.find { it.orderId == orderId } ?: break
                
                if (order.status == "Cancelled" || order.status == "Completed") {
                    break
                }
                
                val nextIndex = statusSequence.indexOf(order.status) + 1
                if (nextIndex in statusSequence.indices) {
                    val nextStatus = statusSequence[nextIndex]
                    repository.updateOrder(order.copy(status = nextStatus))
                } else {
                    break
                }
            }
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.getOrders().first().find { it.orderId == orderId }?.let { order ->
                repository.updateOrder(order.copy(status = newStatus))
                if (newStatus == "Placed") {
                    startOrderTrackingSimulation(orderId)
                }
            }
        }
    }

    fun cancelOrder(orderId: Int, reason: String) {
        viewModelScope.launch {
            repository.getOrders().first().find { it.orderId == orderId }?.let { order ->
                repository.updateOrder(order.copy(status = "Cancelled", cancelledReason = reason))
            }
        }
    }

    fun completeOrder(orderId: Int, rating: Int, review: String) {
        viewModelScope.launch {
            repository.getOrders().first().find { it.orderId == orderId }?.let { order ->
                repository.updateOrder(order.copy(status = "Completed", rating = rating, review = review))
            }
        }
    }

    // Orders State
    val ordersState: StateFlow<List<OrderEntity>> = repository.getOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateUserAddress(newAddress: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.saveUser(user.copy(address = newAddress))
            }
        }
    }

    // Favorites State
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    fun loadFavorites(context: android.content.Context) {
        val prefs = context.getSharedPreferences("pasalhub_favorites", android.content.Context.MODE_PRIVATE)
        val favStrings = prefs.getStringSet("fav_set", emptySet()) ?: emptySet()
        _favoriteIds.value = favStrings.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleFavorite(context: android.content.Context, productId: Int) {
        val prefs = context.getSharedPreferences("pasalhub_favorites", android.content.Context.MODE_PRIVATE)
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _favoriteIds.value = current
        prefs.edit().putStringSet("fav_set", current.map { it.toString() }.toSet()).apply()
    }

    // Password State
    private val _userPassword = MutableStateFlow("password")
    val userPassword: StateFlow<String> = _userPassword.asStateFlow()

    fun loadPassword(context: android.content.Context, email: String) {
        val prefs = context.getSharedPreferences("pasalhub_passwords", android.content.Context.MODE_PRIVATE)
        _userPassword.value = prefs.getString("pwd_$email", "password") ?: "password"
    }

    fun updatePassword(context: android.content.Context, email: String, newPass: String) {
        val prefs = context.getSharedPreferences("pasalhub_passwords", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("pwd_$email", newPass).apply()
        _userPassword.value = newPass
    }

    fun placeDirectOrder(context: android.content.Context, product: ProductDto, finalTotal: Double, paymentMethod: String, appliedVoucher: String) {
        viewModelScope.launch {
            val userAddress = currentUser.value?.address ?: "Default Address, New York"
            val order = OrderEntity(
                totalAmount = finalTotal,
                itemsSummary = "${product.title} x1 (Direct Buy via $paymentMethod${if (appliedVoucher.isNotEmpty()) " [$appliedVoucher]" else ""})",
                status = "Placing",
                quantity = 1,
                price = product.price,
                address = userAddress,
                seller = "${product.category.replaceFirstChar { it.uppercase() }} Luxury Direct",
                date = System.currentTimeMillis()
            )
            repository.placeOrder(order)
            
            // Rewards: 10% of total amount as member points, minimum 50
            val pointsReward = (finalTotal / 10).toInt().coerceAtLeast(50)
            addPoints(context, pointsReward)
        }
    }
}
