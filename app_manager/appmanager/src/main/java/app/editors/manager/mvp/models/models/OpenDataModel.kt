package app.editors.manager.mvp.models.models

import android.os.Parcel
import android.os.Parcelable
import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

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
}

@Serializable
data class OpenFileModel(
    val id: Int? = null,
    val title: String? = null,
    val extension: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
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
    val id: Int? = null,
    val parentId: Int? = null,
    val rootFolderType: Int? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(parentId)
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

// TODO need string?
object JsonAsStringSerializer: JsonTransformingSerializer<String>(tSerializer = String.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonPrimitive(value = element.toString())
    }
}