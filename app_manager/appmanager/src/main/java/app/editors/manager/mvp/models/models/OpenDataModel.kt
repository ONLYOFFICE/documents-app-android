package app.editors.manager.mvp.models.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int

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

    val share: String
        get() {
            if (originalUrl == null) return ""
            return try {
                Uri.parse(originalUrl).getQueryParameter("share").toString()
            } catch (error: Exception) {
                ""
            }
        }
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

object IntOrStringAsStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntOrStringAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        return when (val jsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> {
                if (jsonElement.isString) {
                    jsonElement.content
                } else {
                    jsonElement.int.toString()
                }
            }
            else -> throw SerializationException("Unexpected JSON token: ${jsonElement::class}")
        }
    }
}
