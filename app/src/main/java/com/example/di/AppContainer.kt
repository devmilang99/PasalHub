package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.ai.data.GeminiSearchRouter
import com.example.data.local.AppDatabase
import com.example.data.network.AuthInterceptor
import com.example.data.network.TokenAuthenticator
import com.example.data.network.TokenManager
import com.example.data.remote.FakeStoreApi
import com.example.data.repository.ShopRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val shopRepository: ShopRepository
    val geminiSearchRouter: GeminiSearchRouter
    val tokenManager: TokenManager
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

    override val shopRepository: ShopRepository by lazy {
        ShopRepository(
            api = api,
            userDao = db.userDao(),
            cartDao = db.cartDao(),
            orderDao = db.orderDao()
        )
    }

    override val geminiSearchRouter: GeminiSearchRouter by lazy {
        GeminiSearchRouter(moshi)
    }
}
