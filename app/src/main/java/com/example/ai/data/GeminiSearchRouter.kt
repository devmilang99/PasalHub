package com.example.ai.data

import android.util.Log
import com.example.ai.domain.model.SearchIntent
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiSearchRouter(private val moshi: Moshi) {
    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-3.1-flash-lite",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )
    }

    private val newPromptBase ="You are the backend search parsing engine for 'Pasal Hub', an e-commerce application. Your sole task is to parse a user's natural language search query into a structured, minified JSON object matching the schema below.\\n\\n### Database Constraints\\n- Allowed Categories: [\\\"electronics\\\", \\\"clothing\\\", \\\"footwear\\\", \\\"home_appliances\\\", \\\"accessories\\\", \\\"sports_fitness\\\", \\\"fashion\\\", \\\"jewelery\\\", \\\"home\\\"]\\n- Allowed Sort Values: \\\"rating_desc\\\" | \\\"price_asc\\\" | \\\"price_desc\\\" | \\\"relevance\\\"\\n\\n### Parsing Instructions\\n1. **is_valid (boolean)**: Set `true` if the query relates to shopping, products, or deals (even if vague like \\\"cheap things\\\"). Set `false` only if completely unrelated (e.g., greeting, weather).\\n2. **keywords (string|null)**: Extract core product nouns (e.g., \\\"laptop\\\", \\\"shoes\\\", \\\"shirt\\\").\\n3. **category (string|null)**: Map to the closest allowed category, inferring if necessary (e.g., \\\"headphones\\\" -> \\\"electronics\\\").\\n4. **brand/color/size (string|null)**: Extract explicit attributes if present.\\n5. **price_max (number|null)**: Extract numerical maximum price limit.\\n6. **min_rating (number|null)**: If user implies quality (\\\"best\\\", \\\"top rated\\\"), set to `4.0`. Otherwise `null`.\\n7. **sort_by**: Map \\\"best/top\\\" to `rating_desc`, \\\"cheap/low price\\\" to `price_asc`, \\\"expensive/high price\\\" to `price_desc`. Default to `relevance`.\\n8. **display_message (string|null)**: A friendly, conversational confirmation of the search parameters. `null` if `is_valid` is false.\\n9. **product_summary (string|null)**: Enthusiastic value proposition of the product type matching the user's intent. Must be under 20 words. `null` if `is_valid` is false.\\n10. **fallback_message (string|null)**: Only populate with a polite rejection if `is_valid` is false. Otherwise `null`.\\n\\n### Output JSON Schema\\nReturn exactly this JSON structure. Do not include markdown code blocks, explanations, or extra text.\\n\\n{\\n  \\\"is_valid\\\": boolean,\\n  \\\"fields\\\": {\\n    \\\"keywords\\\": string or null,\\n    \\\"category\\\": string or null,\\n    \\\"brand\\\": string or null,\\n    \\\"color\\\": string or null,\\n    \\\"size\\\": string or null,\\n    \\\"price_max\\\": number or null,\\n    \\\"min_rating\\\": number or null\\n  },\\n  \\\"sort_by\\\": string,\\n  \\\"display_message\\\": string or null,\\n  \\\"product_summary\\\": string or null,\\n  \\\"fallback_message\\\": string or null\\n}"




    suspend fun routeSearch(query: String): SearchIntent? = withContext(Dispatchers.IO) {
        try {
            // Safety check for query length
            if (query.length > 5000) {
                Log.e("GeminiRouter", "Search query too long")
                return@withContext null
            }

            val fullPrompt = "$newPromptBase\n\n### Input\nUser: \"$query\"\nJSON:"
            val response = generativeModel.generateContent(fullPrompt)

            val responseText = response.text ?: return@withContext null

            // Critical: Don't trim or copy if it's already massive
            if (responseText.length > 500_000) {
                Log.e("GeminiRouter", "Response text too large: ${responseText.length}")
                return@withContext null
            }

            val rawResponse = responseText.trim()

            // Truncate logging to prevent OOM if response is large
            val logResponse = if (rawResponse.length > 500) {
                rawResponse.take(500) + "... [truncated, total length: ${rawResponse.length}]"
            } else {
                rawResponse
            }
            Log.d("GeminiRouter", "Raw Response: $logResponse")

            // More robust JSON extraction: Find the first '{' and last '}'
            val startIndex = rawResponse.indexOf('{')
            val endIndex = rawResponse.lastIndexOf('}')

            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
                Log.e("GeminiRouter", "No valid JSON object found in response")
                return@withContext null
            }

            val cleanedJson = rawResponse.substring(startIndex, endIndex + 1)

            val logJson = if (cleanedJson.length > 500) {
                cleanedJson.take(500) + "... [truncated]"
            } else {
                cleanedJson
            }
            Log.d("GeminiRouter", "Extracted JSON: $logJson")

            moshi.adapter(SearchIntent::class.java).fromJson(cleanedJson)
        } catch (t: Throwable) {
            // Catching Throwable to handle OutOfMemoryError and other critical failures
            Log.e("GeminiRouter", "Error routing search: ${t.javaClass.simpleName}", t)
            null
        }
    }
}