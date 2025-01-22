package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.SerializedName

data class QuotaData(

    @SerializedName("enableQuota")
    val enabled: Boolean = false,

    @SerializedName("defaultQuota")
    val value: Long = 0
)