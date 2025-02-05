package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.SerializedName

data class Quota(

    @SerializedName("roomsQuota")
    val roomsQuota: QuotaData = QuotaData(),

    @SerializedName("usersQuota")
    val usersQuota: QuotaData = QuotaData(),

    @SerializedName("tenantCustomQuota")
    val tenantCustomQuota: QuotaData = QuotaData()
)