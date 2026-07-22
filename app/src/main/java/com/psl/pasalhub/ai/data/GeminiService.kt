package com.psl.pasalhub.ai.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val client: HttpClient,
    private val apiKey: String
) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"
    private val prioritizedModels = listOf("gemini-3.5-flash", "gemini-1.5-flash", "gemini-1.5-pro")

    suspend fun generateContent(request: GeminiRequest): GeminiResponse {
        var lastException: Exception? = null

        for (modelName in prioritizedModels) {
            try {
                return client.post("$baseUrl/$modelName:generateContent") {
                    header("x-goog-api-key", apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
            } catch (e: Exception) {
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
