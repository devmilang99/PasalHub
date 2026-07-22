package com.psl.pasalhub.core.sync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Serializer to convert between ISO 8601 strings (Supabase/Postgres) 
 * and Long millisecond timestamps (Room/Local).
 */
object TimestampSerializer : KSerializer<Long> {
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // Fallback formats for parsing if Supabase returns different ISO 8601 variations
    private val fallbackFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    )

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Long) {
        val date = Date(value)
        encoder.encodeString(format.format(date))
    }

    override fun deserialize(decoder: Decoder): Long {
        val string = decoder.decodeString()
        return try {
            format.parse(string)?.time ?: 0L
        } catch (e: Exception) {
            fallbackFormats.firstNotNullOfOrNull {
                try {
                    it.parse(string)?.time
                } catch (e: Exception) {
                    null
                }
            } ?: 0L
        }
    }
}
