package app.documents.core.network.share.models.response

import app.documents.core.network.manager.models.response.BaseListResponse
import app.documents.core.network.share.models.Share
import kotlinx.serialization.Serializable

@Serializable
class ResponseShare : BaseListResponse<Share>()