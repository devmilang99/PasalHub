package com.psl.pasalhub.core.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.psl.pasalhub.core.sync.TimestampSerializer
import kotlinx.serialization.SerialName
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
    @SerialName("updated_at")
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Long = System.currentTimeMillis()
)
