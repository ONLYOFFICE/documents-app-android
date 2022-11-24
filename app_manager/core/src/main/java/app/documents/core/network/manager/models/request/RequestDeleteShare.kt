package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestDeleteShare(
    @SerializedName("folderIds")
    @Expose
    var folderIds: List<String> = ArrayList(),

    @SerializedName("fileIds")
    @Expose
    var fileIds: List<String> = ArrayList()
)