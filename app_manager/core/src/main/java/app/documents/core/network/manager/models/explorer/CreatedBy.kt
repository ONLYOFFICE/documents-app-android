package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CreatedBy : UpdatedBy() {

    @SerializedName("title")
    @Expose
    var title = ""

    @Throws(CloneNotSupportedException::class)
    override fun clone(): CreatedBy {
        return super.clone() as CreatedBy
    }
}