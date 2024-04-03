package app.documents.core.network

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(val response: T, val status: String? = null)