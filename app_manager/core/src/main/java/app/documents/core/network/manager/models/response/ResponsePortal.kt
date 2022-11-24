package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.user.Portal
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponsePortal(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: Portal? = null
) : BaseResponse()