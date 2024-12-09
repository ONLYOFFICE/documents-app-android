package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Watermark {
    @SerializedName("additions")
    @Expose
    var additions = 0

    @SerializedName("rotate")
    @Expose
    var rotate = 0

    @SerializedName("imageScale")
    @Expose
    var imageScale = 0

    @SerializedName("imageHeight")
    @Expose
    var imageHeight = 0

    @SerializedName("imageWidth")
    @Expose
    var imageWidth = 0

    @SerializedName("imageUrl")
    @Expose
    var imageUrl = ""
}