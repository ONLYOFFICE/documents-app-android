package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseExternal(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: String? = null
) : BaseResponse()