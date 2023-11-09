package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse

open class BaseListResponse<T> : BaseResponse() {

    val response: List<T> = listOf()
}