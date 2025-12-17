package app.editors.manager.mvp.models.models

import android.os.Parcel
import android.os.Parcelable
import androidx.core.net.toUri
import app.documents.core.network.IntOrStringAsStringSerializer
import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.Serializable

@Serializable
data class OpenDataModel(
    val portal: String? = null,
    val email: String? = null,
    val file: OpenFileModel? = null,
    val folder: OpenFolderModel? = null,
    val originalUrl: String? = null,
    val errorMsg: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(OpenFileModel::class.java.classLoader),
        parcel.readParcelable(OpenFolderModel::class.java.classLoader),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(portal)
        parcel.writeString(email)
        parcel.writeParcelable(file, flags)
        parcel.writeParcelable(folder, flags)
        parcel.writeString(originalUrl)
        parcel.writeString(errorMsg)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OpenDataModel> {
        override fun createFromParcel(parcel: Parcel): OpenDataModel {
            return OpenDataModel(parcel)
        }

        override fun newArray(size: Int): Array<OpenDataModel?> {
            return arrayOfNulls(size)
        }
    }

    fun getPortalWithoutScheme(): String? {
        if (portal == null) return null
        if (portal.contains(ApiContract.SCHEME_HTTPS)) return portal.replace(ApiContract.SCHEME_HTTPS, "")
        if (portal.contains(ApiContract.SCHEME_HTTP)) return portal.replace(ApiContract.SCHEME_HTTP, "")
        return portal
    }

    fun getCorrectPortal(): String? {
        if (portal.isNullOrEmpty()) return null

        var result = portal

        if (!result.startsWith("http://", true) && !result.startsWith("https://", true)) {
            result = "https://$result"
        }

        if (!result.endsWith("/")) {
            result += "/"
        }

        return result
    }

    val share: String
        get() = runCatching {
            checkNotNull(originalUrl)
                .toUri()
                .getQueryParameter("share")
        }.getOrNull().orEmpty()

    val action: String
        get() = runCatching {
            checkNotNull(originalUrl)
                .toUri()
                .getQueryParameter("action")
        }.getOrNull().orEmpty()
}

@Serializable
data class OpenFileModel(
    @Serializable(with = IntOrStringAsStringSerializer::class) val id: String? = null,
    val title: String? = null,
    val extension: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(extension)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OpenFileModel> {
        override fun createFromParcel(parcel: Parcel): OpenFileModel {
            return OpenFileModel(parcel)
        }

        override fun newArray(size: Int): Array<OpenFileModel?> {
            return arrayOfNulls(size)
        }
    }

}

@Serializable
data class OpenFolderModel(
    @Serializable(with = IntOrStringAsStringSerializer::class) val id: String? = null,
    @Serializable(with = IntOrStringAsStringSerializer::class) val parentId: String? = null,
    val rootFolderType: Int? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(parentId)
        parcel.writeValue(rootFolderType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OpenFolderModel> {
        override fun createFromParcel(parcel: Parcel): OpenFolderModel {
            return OpenFolderModel(parcel)
        }

        override fun newArray(size: Int): Array<OpenFolderModel?> {
            return arrayOfNulls(size)
        }
    }

}
