package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CartItem
import com.example.data.local.OrderEntity
import com.example.data.local.UserEntity
import com.example.data.remote.ProductDto
import com.example.data.repository.Resource
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ShopRepository
) : ViewModel() {

    val notificationEvent = MutableSharedFlow<String>()

    // Theme state (false = Light, true = Dark)
    private val _isDarkTheme = MutableStateFlow(true) // Default to dark for luxury look
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme(context: android.content.Context) {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", newValue).apply()
    }

    fun setTheme(context: android.content.Context, dark: Boolean) {
        _isDarkTheme.value = dark
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", dark).putBoolean("theme_set", true).apply()
    }

    fun isThemeSet(context: android.content.Context): Boolean {
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("theme_set", false)
    }

    // Permission States
    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _cameraPermissionGranted = MutableStateFlow(false)
    val cameraPermissionGranted: StateFlow<Boolean> = _cameraPermissionGranted.asStateFlow()

    private val _storagePermissionGranted = MutableStateFlow(false)
    val storagePermissionGranted: StateFlow<Boolean> = _storagePermissionGranted.asStateFlow()

    private val _notificationPermissionGranted = MutableStateFlow(false)
    val notificationPermissionGranted: StateFlow<Boolean> = _notificationPermissionGranted.asStateFlow()

    fun loadSettings(context: android.content.Context) {
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("dark_theme", true)
        _onboardingCompleted.value = prefs.getBoolean("onboarding_done", false)
        _locationPermissionGranted.value = prefs.getBoolean("perm_location", false)
        _cameraPermissionGranted.value = prefs.getBoolean("perm_camera", false)
        _storagePermissionGranted.value = prefs.getBoolean("perm_storage", false)
        _notificationPermissionGranted.value = prefs.getBoolean("perm_notification", false)
    }

    val allPermissionsGranted: Flow<Boolean> = combine(
        _locationPermissionGranted,
        _cameraPermissionGranted,
        _storagePermissionGranted,
        _notificationPermissionGranted
    ) { loc, cam, store, notif -> loc && cam && store && notif }

    fun setLocationPermission(context: android.content.Context, granted: Boolean) {
        _locationPermissionGranted.value = granted
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("perm_location", granted).apply()
    }

    fun setCameraPermission(context: android.content.Context, granted: Boolean) {
        _cameraPermissionGranted.value = granted
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("perm_camera", granted).apply()
    }

    fun setStoragePermission(context: android.content.Context, granted: Boolean) {
        _storagePermissionGranted.value = granted
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("perm_storage", granted).apply()
    }

    fun setNotificationPermission(context: android.content.Context, granted: Boolean) {
        _notificationPermissionGranted.value = granted
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("perm_notification", granted).apply()
    }

    // Biometric State
    private val _biometricAuthenticated = MutableStateFlow(false)
    val biometricAuthenticated: StateFlow<Boolean> = _biometricAuthenticated.asStateFlow()

    fun setBiometricAuthenticated(authenticated: Boolean) {
        _biometricAuthenticated.value = authenticated
    }

    // Onboarding State
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun completeOnboarding(context: android.content.Context) {
        _onboardingCompleted.value = true
        val prefs = context.getSharedPreferences("pasalhub_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_done", true).apply()
    }

    // User State from DB
    val currentUser: StateFlow<UserEntity?> = repository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun registerUser(name: String, email: String, dateOfBirth: String, address: String, rememberMe: Boolean) {
        viewModelScope.launch {
            val user = UserEntity(
                email = email,
                name = name,
                dateOfBirth = dateOfBirth,
                address = address,
                isRemembered = rememberMe
            )
            repository.saveUser(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearUser()
            repository.clearCart()
            _biometricAuthenticated.value = false
        }
    }

    // Products State
    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)

    val productsState: StateFlow<Resource<List<ProductDto>>> = combine(
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

    fun refreshProducts() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

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
            notificationEvent.emit("Successfully added ${product.title} to your cart!")
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
                notificationEvent.emit("Your order #ORD-${1000 + order.orderId} has been placed successfully!")
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
