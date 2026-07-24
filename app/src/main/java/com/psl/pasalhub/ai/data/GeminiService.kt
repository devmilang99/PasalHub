package com.psl.pasalhub.ai.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val client: HttpClient,
    private val apiKey: String
) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"
    private val prioritizedModels = listOf(
        "gemini-3.6-flash",
        "gemini-3.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-flash",
        "gemini-1.5-pro"
    )

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        if (apiKey.isBlank()) {
            Log.e("GeminiService", "Gemini API Key is missing or empty!")
        } else if (apiKey.length < 10) {
            Log.w("GeminiService", "Gemini API Key seems suspiciously short.")
        } else {
            Log.d("GeminiService", "Gemini API Key initialized (Length: ${apiKey.length})")
        }
    }

    suspend fun generateContent(request: GeminiRequest): GeminiResponse {
        var lastException: Exception? = null

        for (modelName in prioritizedModels) {
            try {
                val response = client.post("$baseUrl/$modelName:generateContent") {
                    header("x-goog-api-key", apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                val rawJson = response.bodyAsText()
                Log.d("GeminiService", "RAW RESPONSE from $modelName: $rawJson")

                return json.decodeFromString<GeminiResponse>(rawJson).also {
                    Log.d("GeminiService", "Content generation successful with model: $modelName")
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed attempt with model $modelName: ${e.message}")
                lastException = e
                // Continue to next model on failure
            }
        }
        throw lastException ?: Exception("All Gemini models failed to generate content")
    }

    suspend fun listModels(): GeminiModelList {
        return client.get("https://generativelanguage.googleapis.com/v1beta/models") {
            header("x-goog-api-key", apiKey)
        }.body()
    }
}
