package app.documents.core.network.manager.models.user

import app.documents.core.network.manager.models.base.ItemProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Portal(

    @SerializedName("tenantId")
    @Expose
    var tenantId: Int? = null,

    @SerializedName("tenantAlias")
    @Expose
    var tenantAlias: String? = null,

    @SerializedName("mappedDomain")
    @Expose
    var mappedDomain: Any? = null,

    @SerializedName("version")
    @Expose
    var version: Int? = null,

    @SerializedName("versionChanged")
    @Expose
    var versionChanged: String? = null,

    @SerializedName("tenantDomain")
    @Expose
    var tenantDomain: String? = null,

    @SerializedName("hostedRegion")
    @Expose
    var hostedRegion: Any? = null,

    @SerializedName("name")
    @Expose
    var name: String? = null,

    @SerializedName("language")
    @Expose
    var language: String? = null,

    @SerializedName("trustedDomains")
    @Expose
    var trustedDomains: List<Any>? = null,

    @SerializedName("trustedDomainsType")
    @Expose
    var trustedDomainsType: Int? = null,

    @SerializedName("ownerId")
    @Expose
    var ownerId: String? = null,

    @SerializedName("createdDateTime")
    @Expose
    var createdDateTime: String? = null,

    @SerializedName("lastModified")
    @Expose
    var lastModified: String? = null,

    @SerializedName("status")
    @Expose
    private val status: Int? = null,

    @SerializedName("statusChangeDate")
    @Expose
    var statusChangeDate: String? = null,

    @SerializedName("partnerId")
    @Expose
    var partnerId: Any? = null,

    @SerializedName("affiliateId")
    @Expose
    var affiliateId: Any? = null,

    @SerializedName("paymentId")
    @Expose
    var paymentId: Any? = null,

    @SerializedName("industry")
    @Expose
    var industry: Int? = null,

    @SerializedName("spam")
    @Expose
    var spam: Boolean? = null,

    @SerializedName("calls")
    @Expose
    var calls: Boolean? = null

) : ItemProperties()