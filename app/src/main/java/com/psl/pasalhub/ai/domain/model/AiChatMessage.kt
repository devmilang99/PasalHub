package com.psl.pasalhub.ai.domain.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String? = null,
    val image: Bitmap? = null,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
