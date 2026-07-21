package com.psl.pasalhub.ai.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.generationConfig
import com.psl.pasalhub.ai.domain.model.SearchIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiSearchRouter {

    private val searchProductsTool = FunctionDeclaration(
        name = "search_products",
        description = "Search for products in the Pasal Hub catalog based on various filters. Use this when the user is looking for something to buy.",
        parameters = mapOf(
            "keywords" to Schema.string("Core product keywords or nouns (e.g., 'laptop', 'shoes')."),
            "category" to Schema.string(
                "Product category. Allowed: electronics, clothing, footwear, home_appliances, accessories, sports_fitness, fashion, jewelery, home."
            ),
            "brand" to Schema.string("Specific brand name if mentioned."),
            "color" to Schema.string("Preferred color."),
            "price_max" to Schema.double("Maximum price limit."),
            "sort_by" to Schema.string(
                "Sorting preference. Allowed: price_asc, price_desc, rating_desc, relevance."
            )
        )
    )

    private val getProductDetailsTool = FunctionDeclaration(
        name = "get_product_details",
        description = "Get detailed information about a specific product by its ID. Use this when the user asks specifically about one product.",
        parameters = mapOf(
            "product_id" to Schema.integer("The unique ID of the product.")
        )
    )

    private val applyDiscountTool = FunctionDeclaration(
        name = "apply_discount",
        description = "Check for available discounts or apply a coupon code.",
        parameters = mapOf(
            "coupon_code" to Schema.string("The discount coupon code (optional)."),
            "product_id" to Schema.integer("Specific product ID to check discount for (optional).")
        ),
        optionalParameters = listOf("coupon_code", "product_id")
    )

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel(
            modelName = "gemini-3.1-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.2f
            },
            tools = listOf(
                Tool.functionDeclarations(
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
        // This is kept for reference, but we will migrate to function calling in the ViewModel
        null
    }
}
