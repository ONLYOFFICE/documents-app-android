/*
 * Created by Michael Efremov on 02.10.20 17:02
 */
package app.documents.core.network.manager.models.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestDownload(
    @SerializedName("fileIds")
    @Expose
    var filesIds: List<String>? = null,

    @SerializedName("folderIds")
    @Expose
    var foldersIds: List<String>? = null
)