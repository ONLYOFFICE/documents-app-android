package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestStorage(
    @SerializedName("url")
    @Expose
    var url: String? = null,

    @SerializedName("login")
    @Expose
    var login: String? = null,

    @SerializedName("password")
    @Expose
    var password: String? = null,

    @SerializedName("token")
    @Expose
    var token: String? = null,

    @SerializedName("isCorporate")
    @Expose
    var corporate: Boolean = false,

    @SerializedName("customerTitle")
    @Expose
    var customerTitle: String? = null,

    @SerializedName("providerKey")
    @Expose
    var providerKey: String? = null,

    @SerializedName("providerId")
    @Expose
    var providerId: String? = null,

    @SerializedName("isRoomStorage")
    @Expose
    var isRoomStorage: Boolean = false,
)