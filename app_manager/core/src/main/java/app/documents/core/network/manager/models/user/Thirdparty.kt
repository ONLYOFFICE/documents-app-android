package app.documents.core.network.manager.models.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Thirdparty : Serializable {

    @SerializedName("corporate")
    @Expose
    var isCorporate = false

    @SerializedName("customer_title")
    @Expose
    var title: String? = null

    @SerializedName("provider_id")
    @Expose
    var id = 0

    @SerializedName("provider_key")
    @Expose
    var providerKey: String? = null
}