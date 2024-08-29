package app.documents.core.network.manager.models.base

import app.documents.core.model.login.User
import app.documents.core.network.manager.models.explorer.CloudFile
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FillResult(
    @SerializedName("completedForm")
    @Expose
    val completedForm: CloudFile,
    @SerializedName("manager")
    @Expose
    val manager: User,
    @SerializedName("originalForm")
    @Expose
    val originalForm: CloudFile,
    @SerializedName("formNumber")
    @Expose
    val formNumber: Int,
    @SerializedName("roomId")
    @Expose
    val roomId: Int,
    @SerializedName("isRoomMember")
    @Expose
    val isRoomMember: Boolean
)