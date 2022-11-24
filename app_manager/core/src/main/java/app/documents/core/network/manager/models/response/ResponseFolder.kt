package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.CloudFolder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseFolder(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: CloudFolder? = null
) : BaseResponse()