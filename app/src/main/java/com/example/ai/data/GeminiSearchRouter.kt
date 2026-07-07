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

    private val promptBase = """
        You are a smart search assistant for 'Pasal Hub', an e-commerce app. 
        Your goal is to parse user queries into a structured JSON format.

        ### Database Schema & Allowed Categories
        - category: ["electronics", "clothing", "footwear", "home_appliances", "accessories", "sports_fitness", "fashion", "jewelery", "home"]
        - keywords: Main product terms (e.g., "laptop", "shoes", "shirt")
        - price_max: Maximum price if specified
        - min_rating: Minimum rating if the user asks for "best", "top rated", etc. (default to 4.0 for these terms)
        - color: Preferred color (e.g., "red", "blue")
        - size: Preferred size (e.g., "M", "L", "42")

        ### Instructions
        1. 'is_valid' should be true if the query is related to shopping or products.
        2. If the user query is vague but related to shopping (e.g., "cheap things", "best deals"), set 'is_valid' to true and use appropriate keywords/sort_by.
        3. Only set 'is_valid' to false if the query is completely unrelated to products or e-commerce (e.g., "what is the weather").
        4. In the 'fields' object, map the user's intent to the schema. 
        5. If a category isn't explicitly mentioned but can be inferred (e.g., "headphones" -> "electronics"), set the category.
        6. 'display_message' should be a friendly confirmation of what you are searching for (e.g., "Looking for white sneakers under $100 for you...").
        7. 'product_summary' should be a very brief, friendly, and enthusiastic summary of why these types of products are great for the user's specific request (e.g., "White sneakers are a timeless classic that go with almost anything, especially for a clean summer look!"). Keep it under 20 words.

        ### Output JSON Format (Strictly Raw JSON)
        {
          "is_valid": boolean,
          "fields": {
            "keywords": "string or null",
            "category": "string or null",
            "brand": "string or null",
            "color": "string or null",
            "size": "string or null",
            "price_max": number or null,
            "min_rating": number or null
          },
          "sort_by": "rating_desc | price_asc | price_desc | relevance",
          "display_message": "string or null",
          "product_summary": "string or null",
          "fallback_message": "string or null (only if is_valid is false)"
        }
        
        ### Examples
        User: "best wireless headphones"
        JSON: {"is_valid":true,"fields":{"keywords":"headphones","category":"electronics","brand":null,"price_max":null,"min_rating":4.0},"sort_by":"rating_desc","display_message":"Searching for the top-rated wireless headphones...","product_summary":"Wireless headphones offer total freedom and amazing sound quality for your music and calls!","fallback_message":null}
        
        User: "white sneakers under 100"
        JSON: {"is_valid":true,"fields":{"keywords":"sneakers","category":"footwear","brand":null,"price_max":100,"min_rating":null,"color":"white"},"sort_by":"relevance","display_message":"Finding white sneakers under $100 for you...","product_summary":"White sneakers are super versatile and perfect for keeping your outfit looking fresh and crisp!","fallback_message":null}
    """.trimIndent()


    private val newPromptBase ="You are the backend search parsing engine for 'Pasal Hub', an e-commerce application. Your sole task is to parse a user's natural language search query into a structured, minified JSON object matching the schema below.\\n\\n### Database Constraints\\n- Allowed Categories: [\\\"electronics\\\", \\\"clothing\\\", \\\"footwear\\\", \\\"home_appliances\\\", \\\"accessories\\\", \\\"sports_fitness\\\", \\\"fashion\\\", \\\"jewelery\\\", \\\"home\\\"]\\n- Allowed Sort Values: \\\"rating_desc\\\" | \\\"price_asc\\\" | \\\"price_desc\\\" | \\\"relevance\\\"\\n\\n### Parsing Instructions\\n1. **is_valid (boolean)**: Set `true` if the query relates to shopping, products, or deals (even if vague like \\\"cheap things\\\"). Set `false` only if completely unrelated (e.g., greeting, weather).\\n2. **keywords (string|null)**: Extract core product nouns (e.g., \\\"laptop\\\", \\\"shoes\\\", \\\"shirt\\\").\\n3. **category (string|null)**: Map to the closest allowed category, inferring if necessary (e.g., \\\"headphones\\\" -> \\\"electronics\\\").\\n4. **brand/color/size (string|null)**: Extract explicit attributes if present.\\n5. **price_max (number|null)**: Extract numerical maximum price limit.\\n6. **min_rating (number|null)**: If user implies quality (\\\"best\\\", \\\"top rated\\\"), set to `4.0`. Otherwise `null`.\\n7. **sort_by**: Map \\\"best/top\\\" to `rating_desc`, \\\"cheap/low price\\\" to `price_asc`, \\\"expensive/high price\\\" to `price_desc`. Default to `relevance`.\\n8. **display_message (string|null)**: A friendly, conversational confirmation of the search parameters. `null` if `is_valid` is false.\\n9. **product_summary (string|null)**: Enthusiastic value proposition of the product type matching the user's intent. Must be under 20 words. `null` if `is_valid` is false.\\n10. **fallback_message (string|null)**: Only populate with a polite rejection if `is_valid` is false. Otherwise `null`.\\n\\n### Output JSON Schema\\nReturn exactly this JSON structure. Do not include markdown code blocks, explanations, or extra text.\\n\\n{\\n  \\\"is_valid\\\": boolean,\\n  \\\"fields\\\": {\\n    \\\"keywords\\\": string or null,\\n    \\\"category\\\": string or null,\\n    \\\"brand\\\": string or null,\\n    \\\"color\\\": string or null,\\n    \\\"size\\\": string or null,\\n    \\\"price_max\\\": number or null,\\n    \\\"min_rating\\\": number or null\\n  },\\n  \\\"sort_by\\\": string,\\n  \\\"display_message\\\": string or null,\\n  \\\"product_summary\\\": string or null,\\n  \\\"fallback_message\\\": string or null\\n}"



    suspend fun routeSearch(query: String): SearchIntent? = withContext(Dispatchers.IO) {
        try {
            val fullPrompt = "$newPromptBase\n\n### Input\nUser: \"$query\"\nJSON:"
            val response = generativeModel.generateContent(fullPrompt)
            
            val rawResponse = response.text?.trim() ?: return@withContext null
            Log.d("GeminiRouter", "Raw Response: $rawResponse")
            
            // More robust JSON extraction: Find the first '{' and last '}'
            val startIndex = rawResponse.indexOf('{')
            val endIndex = rawResponse.lastIndexOf('}')
            
            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
                Log.e("GeminiRouter", "No valid JSON object found in response")
                return@withContext null
            }
            
            val cleanedJson = rawResponse.substring(startIndex, endIndex + 1)
            Log.d("GeminiRouter", "Extracted JSON: $cleanedJson")

            moshi.adapter(SearchIntent::class.java).fromJson(cleanedJson)
        } catch (e: Exception) {
            Log.e("GeminiRouter", "Error routing search", e)
            null
        }
    }
}
