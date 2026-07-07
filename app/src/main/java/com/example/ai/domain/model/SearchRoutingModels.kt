package com.example.ai.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchIntent(
    val is_valid: Boolean,
    val fields: SearchFields?,
    val sort_by: String?,
    val fallback_message: String?,
    val display_message: String? = null,
    val product_summary: String? = null
)

@JsonClass(generateAdapter = true)
data class SearchFields(
    val keywords: String?,
    val category: String?,
    val brand: String?,
    val color: String?,
    val size: String?,
    val price_max: Double?,
    val min_rating: Double?
)
