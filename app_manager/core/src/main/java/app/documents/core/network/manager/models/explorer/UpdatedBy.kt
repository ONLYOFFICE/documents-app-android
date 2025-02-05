package app.documents.core.network.manager.models.explorer

import android.text.Html
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class UpdatedBy : Cloneable, Serializable {
    @SerializedName("id")
    @Expose
    var id = ""

    @SerializedName("displayName")
    @Expose
    var displayName = ""

    @SerializedName("avatarSmall")
    @Expose
    var avatarSmall = ""

    @SerializedName("profileUrl")
    @Expose
    var profileUrl = ""

    @SerializedName("hasAvatar")
    @Expose
    var hasAvatar = false

    @SerializedName("isAnonim")
    @Expose
    var isAnonim = false

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): UpdatedBy {
        return super.clone() as UpdatedBy
    }

    val displayNameFromHtml: String
        get() = Html.fromHtml(displayName, Html.FROM_HTML_MODE_LEGACY).toString()
}