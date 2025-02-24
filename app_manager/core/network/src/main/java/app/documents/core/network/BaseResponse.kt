package app.documents.core.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int

@Serializable
data class BaseResponse<T>(val response: T, @Serializable(with = IntOrStringAsStringSerializer::class) val status: String? = null)

object IntOrStringAsStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntOrStringAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        return when (val jsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> {
                if (jsonElement.isString) {
                    jsonElement.content
                } else {
                    jsonElement.int.toString()
                }
            }
            else -> throw SerializationException("Unexpected JSON token: ${jsonElement::class}")
        }
    }
}

