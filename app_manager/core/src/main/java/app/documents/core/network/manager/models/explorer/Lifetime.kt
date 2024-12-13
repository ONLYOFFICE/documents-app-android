package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Lifetime(
    @SerializedName("deletePermanently")
    @Expose
    val deletePermanently: Boolean = false,

    @SerializedName("period")
    @Expose
    val period: Int = PERIOD_DAYS,

    @SerializedName("value")
    @Expose
    val value: Int = 0,

    @SerializedName("enabled")
    @Expose
    val enabled: Boolean
) {

    companion object {

        const val DEFAULT_VALUE: Int = 12

        const val PERIOD_DAYS: Int = 0
        const val PERIOD_MONTHS: Int = 1
        const val PERIOD_YEARS: Int = 2
    }
}