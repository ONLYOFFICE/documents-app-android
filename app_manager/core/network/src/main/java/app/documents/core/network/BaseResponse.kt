package app.documents.core.network

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(val result: T)