package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.user.Module
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseModules(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: List<Module>? = null
) : BaseResponse()