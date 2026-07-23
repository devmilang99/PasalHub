package com.psl.pasalhub.ai.data

import com.psl.pasalhub.ai.domain.model.SearchIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiSearchRouter @Inject constructor(
    private val geminiService: GeminiService
) {

    private val searchProductsTool = FunctionDeclaration(
        name = "search_products",
        description = "Search for products in the Pasal Hub catalog based on various filters. Use this when the user is looking for something to buy.",
        parameters = Schema(
            type = "object",
            properties = mapOf(
                "keywords" to Schema(
                    type = "string",
                    description = "Core product keywords or nouns (e.g., 'laptop', 'shoes')."
                ),
                "category" to Schema(
                    type = "string",
                    description = "Product category. Allowed: electronics, clothing, footwear, home_appliances, accessories, sports_fitness, fashion, jewelery, home."
                ),
                "brand" to Schema(
                    type = "string",
                    description = "Specific brand name if mentioned."
                ),
                "color" to Schema(type = "string", description = "Preferred color."),
                "price_max" to Schema(type = "number", description = "Maximum price limit."),
                "sort_by" to Schema(
                    type = "string",
                    description = "Sorting preference. Allowed: price_asc, price_desc, rating_desc, relevance."
                )
            )
        )
    )

    private val getProductDetailsTool = FunctionDeclaration(
        name = "get_product_details",
        description = "Get detailed information about a specific product by its ID. Use this when the user asks specifically about one product.",
        parameters = Schema(
            type = "object",
            properties = mapOf(
                "product_id" to Schema(
                    type = "integer",
                    description = "The unique ID of the product."
                )
            ),
            required = listOf("product_id")
        )
    )

    private val applyDiscountTool = FunctionDeclaration(
        name = "apply_discount",
        description = "Check for available discounts or apply a coupon code.",
        parameters = Schema(
            type = "object",
            properties = mapOf(
                "coupon_code" to Schema(
                    type = "string",
                    description = "The discount coupon code (optional)."
                ),
                "product_id" to Schema(
                    type = "integer",
                    description = "Specific product ID to check discount for (optional)."
                )
            )
        )
    )

    private val tools = listOf(
        Tool(
            functionDeclarations = listOf(
                searchProductsTool,
                getProductDetailsTool,
                applyDiscountTool
            )
        )
    )

    private val generationConfig = GenerationConfig(
        temperature = 0.2f
    )

    private val systemInstruction = Content(
        role = "system",
        parts = listOf(
            Part(
                text = """
                You are the AI Assistant for PasalHub, a premium e-commerce app. 
                Your primary goal is to help users find products and provide short, sweet shopping advice.
                
                CRITICAL RULES:
                1. Always use 'search_products' tool when a user mentions a category, wants to explore products, or uses phrases like "Latest", "Best", "Deals", "Show me".
                2. Do not answer that you cannot find products without first calling the 'search_products' tool.
                3. If the user clicks a "Quick Explore" action (like 'Latest electronics', 'Summer fashion', etc.), you MUST call 'search_products' with the appropriate category.
                4. For 'Latest tech' or 'Latest electronics', use category='electronics'. Avoid using restrictive keywords unless the user was specific.
                5. For 'Summer fashion', use category='clothing' or 'fashion'.
                6. For 'Gaming gear', use category='electronics' or 'sports_fitness' with 'gaming' keywords.
                7. For 'Home decor', use category='home'.
                8. ALWAYS provide a natural language response after tool calls.
                9. Keep responses EXTREMELY concise (max 2 short sentences).
                10. DO NOT list product names, prices, or descriptions in your text response, as they are already shown in the result cards.
                11. If 'search_products' returns results, just say something like "Here are some top picks for you!" or "I found these matches in our catalog."
                12. If 'search_products' returns 0 results, suggest broader categories or check for deals.
                13. Tone: Helpful, premium, and very BRIEF.
                """.trimIndent()
            )
        )
    )

    suspend fun routeSearch(query: String, history: List<Content> = emptyList()): GeminiResponse {
        val contents = history.toMutableList().apply {
            add(Content(role = "user", parts = listOf(Part(text = query))))
        }
        val request = GeminiRequest(
            contents = contents,
            tools = tools,
            generationConfig = generationConfig,
            systemInstruction = systemInstruction
        )
        return geminiService.generateContent(request)
    }

    suspend fun routeSearchWithContent(contents: List<Content>): GeminiResponse {
        val request = GeminiRequest(
            contents = contents,
            tools = tools,
            generationConfig = generationConfig,
            systemInstruction = systemInstruction
        )

        return geminiService.generateContent(request)
    }

    suspend fun listAvailableModels() = geminiService.listModels()

    // Backward compatibility for the legacy parsing flow if still needed
    suspend fun routeSearchLegacy(query: String): SearchIntent? = withContext(Dispatchers.IO) {
        null
    }
}
