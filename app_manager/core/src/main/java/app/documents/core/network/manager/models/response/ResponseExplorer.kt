package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.Explorer
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseExplorer(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: Explorer? = null
) : BaseResponse()