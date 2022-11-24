package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.CloudFile
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseFiles(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: List<CloudFile>
) : BaseResponse()