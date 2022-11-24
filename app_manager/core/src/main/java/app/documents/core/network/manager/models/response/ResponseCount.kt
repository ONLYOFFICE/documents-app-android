package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseCount(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: Int = 0
) : BaseResponse()