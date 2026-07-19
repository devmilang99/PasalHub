package com.psl.pasalhub.ai.data

import android.util.Log
import com.psl.pasalhub.BuildConfig
import com.psl.pasalhub.ai.domain.model.SearchFields
import com.psl.pasalhub.ai.domain.model.SearchIntent
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiSearchRouter {

    private val searchProductsTool = defineFunction(
        name = "search_products",
        description = "Search for products in the Pasal Hub catalog based on various filters. Use this when the user is looking for something to buy.",
        parameters = listOf(
            Schema.str("keywords", "Core product keywords or nouns (e.g., 'laptop', 'shoes')."),
            Schema.str(
                "category",
                "Product category. Allowed: electronics, clothing, footwear, home_appliances, accessories, sports_fitness, fashion, jewelery, home."
            ),
            Schema.str("brand", "Specific brand name if mentioned."),
            Schema.str("color", "Preferred color."),
            Schema.double("price_max", "Maximum price limit."),
            Schema.str(
                "sort_by",
                "Sorting preference. Allowed: price_asc, price_desc, rating_desc, relevance."
            )
        )
    )

    private val getProductDetailsTool = defineFunction(
        name = "get_product_details",
        description = "Get detailed information about a specific product by its ID. Use this when the user asks specifically about one product.",
        parameters = listOf(
            Schema.int("product_id", "The unique ID of the product.")
        ),
        requiredParameters = listOf("product_id")
    )

    private val applyDiscountTool = defineFunction(
        name = "apply_discount",
        description = "Check for available discounts or apply a coupon code.",
        parameters = listOf(
            Schema.str("coupon_code", "The discount coupon code (optional)."),
            Schema.int("product_id", "Specific product ID to check discount for (optional).")
        )
    )

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-3.1-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.2f
            },
            tools = listOf(
                Tool(
                    listOf(
                        searchProductsTool,
                        getProductDetailsTool,
                        applyDiscountTool
                    )
                )
            )
        )
    }

    fun startChat() = generativeModel.startChat()

    // Backward compatibility for the legacy parsing flow if still needed
    // In the new flow, we will use the chat interface directly in the ViewModel
    suspend fun routeSearchLegacy(query: String): SearchIntent? = withContext(Dispatchers.IO) {
        // This is kept for reference but we will migrate to function calling in the ViewModel
        null
    }
}
