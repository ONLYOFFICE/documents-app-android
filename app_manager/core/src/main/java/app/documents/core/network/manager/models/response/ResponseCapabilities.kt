package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.login.models.Capabilities
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseCapabilities(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: Capabilities = Capabilities()
) : BaseResponse()