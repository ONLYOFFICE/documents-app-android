package app.documents.core.network.manager.models.base

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Error {
    @SerializedName(KEY_MESSAGE)
    @Expose
    var message = ""

    @SerializedName(KEY_HRESULT)
    @Expose
    var hresult = ""

    @SerializedName(KEY_STACK)
    @Expose
    var stack = ""

    @SerializedName(KEY_DATA)
    @Expose
    var data = ""

    companion object {
        const val KEY_MESSAGE = "message"
        const val KEY_HRESULT = "hresult"
        const val KEY_DATA = "data"
        const val KEY_STACK = "stack"
    }
}