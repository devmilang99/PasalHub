package com.example.initial.di

import android.content.Context
import androidx.room.Room
import com.example.ai.data.GeminiSearchRouter
import com.example.core.database.data.AppDatabase
import com.example.core.application.domain.AppPreferencesRepository
import com.example.core.application.data.AppPreferencesRepositoryImpl
import com.example.core.application.utils.NotificationHelper
import com.example.core.networking.network.AuthInterceptor
import com.example.core.networking.network.TokenAuthenticator
import com.example.core.networking.network.TokenManager
import com.example.core.networking.remote.FakeStoreApi
import com.example.dashboard.products.repository.ProductRepository
import com.example.auth.login.domain.LoginRepository
import com.example.auth.login.data.LoginRepositoryImpl
import com.example.auth.register.domain.RegisterRepository
import com.example.auth.register.data.RegisterRepositoryImpl
import com.example.auth.forgotpassword.domain.ForgotPasswordRepository
import com.example.auth.forgotpassword.data.ForgotPasswordRepositoryImpl
import com.example.dashboard.home.domain.HomeRepository
import com.example.dashboard.home.data.HomeRepositoryImpl
import com.example.dashboard.order.domain.OrderRepository
import com.example.dashboard.order.data.OrderRepositoryImpl
import com.example.dashboard.cart.domain.CartRepository
import com.example.dashboard.cart.data.CartRepositoryImpl
import com.example.dashboard.profile.domain.ProfileRepository
import com.example.dashboard.profile.data.ProfileRepositoryImpl
import com.example.initial.domain.InitialRepository
import com.example.initial.data.InitialRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val productRepository: ProductRepository
    val geminiSearchRouter: GeminiSearchRouter
    val tokenManager: TokenManager

    val loginRepository: LoginRepository
    val registerRepository: RegisterRepository
    val forgotPasswordRepository: ForgotPasswordRepository
    val homeRepository: HomeRepository
    val orderRepository: OrderRepository
    val cartRepository: CartRepository
    val profileRepository: ProfileRepository
    val initialRepository: InitialRepository
    val appPreferencesRepository: AppPreferencesRepository
    val notificationHelper: NotificationHelper
}

class AppContainerImpl(private val context: Context) : AppContainer {

    override val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(tokenManager))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val api: FakeStoreApi by lazy {
        retrofit.create(FakeStoreApi::class.java)
    }

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pasalhub_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepository(
            api = api,
            userDao = db.userDao(),
            cartDao = db.cartDao(),
            orderDao = db.orderDao()
        )
    }

    override val geminiSearchRouter: GeminiSearchRouter by lazy {
        GeminiSearchRouter(moshi)
    }

    override val loginRepository: LoginRepository by lazy {
        LoginRepositoryImpl(db.userDao(), context, appPreferencesRepository)
    }

    override val registerRepository: RegisterRepository by lazy {
        RegisterRepositoryImpl(db.userDao(), context)
    }

    override val forgotPasswordRepository: ForgotPasswordRepository by lazy {
        ForgotPasswordRepositoryImpl()
    }

    override val homeRepository: HomeRepository by lazy {
        HomeRepositoryImpl(productRepository, db.userDao(), context, appPreferencesRepository)
    }

    override val orderRepository: OrderRepository by lazy {
        OrderRepositoryImpl(db.orderDao(), context, appPreferencesRepository)
    }

    override val cartRepository: CartRepository by lazy {
        CartRepositoryImpl(productRepository, db.userDao(), db.cartDao(), db.orderDao(), context)
    }

    override val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(productRepository, db.userDao(), context, appPreferencesRepository)
    }

    override val initialRepository: InitialRepository by lazy {
        InitialRepositoryImpl(context, db.userDao(), appPreferencesRepository)
    }

    override val appPreferencesRepository: AppPreferencesRepository by lazy {
        AppPreferencesRepositoryImpl(context)
    }

    override val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(context)
    }
}
