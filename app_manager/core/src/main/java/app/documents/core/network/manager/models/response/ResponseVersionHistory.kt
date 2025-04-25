package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.CloudFile
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseVersionHistory(
    @SerializedName(KEY_RESPONSE)
    @Expose
    val response: List<CloudFile>? = null
) : BaseResponse()
