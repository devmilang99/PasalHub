package com.psl.pasalhub.ai.di

import com.psl.pasalhub.BuildConfig
import com.psl.pasalhub.ai.data.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGeminiHttpClient(okHttpClient: OkHttpClient): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                preconfigured = okHttpClient
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { _, response ->
                    response.status.value in 500..599 || response.status == HttpStatusCode.TooManyRequests
                }
                exponentialDelay(base = 2.0, maxDelayMs = 5000)
            }
        }
    }

    @Provides
    @Singleton
    fun provideGeminiService(httpClient: HttpClient): GeminiService {
        return GeminiService(
            client = httpClient,
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
}
