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
        description = "Search PasalHub catalog. Use when user wants to buy or explore products.",
        parameters = Schema(
            type = "object",
            properties = mapOf(
                "keywords" to Schema(type = "string", description = "Product nouns (e.g. laptop)."),
                "category" to Schema(
                    type = "string",
                    description = "Allowed: electronics, clothing, footwear, home_appliances, accessories, sports_fitness, fashion, jewelery, home."
                ),
                "brand" to Schema(type = "string", description = "Brand name."),
                "color" to Schema(type = "string", description = "Color."),
                "price_max" to Schema(type = "number", description = "Max price."),
                "sort_by" to Schema(
                    type = "string",
                    description = "Allowed: price_asc, price_desc, rating_desc, relevance."
                )
            )
        )
    )

    private val getProductDetailsTool = FunctionDeclaration(
        name = "get_product_details",
        description = "Get detailed info for a specific product ID.",
        parameters = Schema(
            type = "object",
            properties = mapOf(
                "product_id" to Schema(type = "integer", description = "Unique product ID.")
            ),
            required = listOf("product_id")
        )
    )

    private val applyDiscountTool = FunctionDeclaration(
        name = "apply_discount",
        description = "Check/apply coupon codes.",
        parameters = Schema(
            type = "object",
            properties = mapOf(
                "coupon_code" to Schema(type = "string", description = "Coupon code."),
                "product_id" to Schema(type = "integer", description = "Product ID (optional).")
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
                Role: PasalHub AI Assistant.
                Goal: Help find products with brief, premium advice.
                Rules:
                - Use 'search_products' for any product exploration, category mentions, or "Latest/Best/Deals" queries.
                - Call tools before claiming no results.
                - Map "tech/electronics" -> 'electronics', "fashion" -> 'clothing', "decor" -> 'home'.
                - Post-tool: Give a concise natural language response (max 2 sentences). 
                - Do NOT list products/prices (already in UI cards).
                - Tone: Brief & Helpful.
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
