package app.documents.core.network.manager.models.response

import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.user.Thirdparty
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseThirdparty(
    @SerializedName(KEY_RESPONSE)
    @Expose
    var response: List<Thirdparty>
) : BaseResponse()