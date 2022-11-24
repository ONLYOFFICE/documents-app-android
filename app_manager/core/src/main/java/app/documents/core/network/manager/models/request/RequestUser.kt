package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestUser(
    @SerializedName("firstName")
    @Expose
    var firstName: String? = null,

    @SerializedName("lastName")
    @Expose
    var lastName: String? = null,

    @SerializedName("sex")
    @Expose
    var sex: String? = null,

    @SerializedName("files")
    @Expose
    var avatar: String? = null
) {
    enum class Sex {
        MALE, FEMALE
    }

    companion object {
        fun getSex(sex: Sex?): String? {
            return when (sex) {
                Sex.MALE -> return "male"
                Sex.FEMALE -> return "female"
                else -> null
            }
        }
    }
}