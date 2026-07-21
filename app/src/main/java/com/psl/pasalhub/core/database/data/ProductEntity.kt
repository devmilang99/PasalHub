package com.psl.pasalhub.core.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "products")
@Serializable
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val price: Double,
    val description: String?,
    val category: String?,
    val image: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
