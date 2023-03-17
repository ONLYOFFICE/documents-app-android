package app.documents.core.network.manager.models.explorer

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import app.documents.core.network.manager.models.base.Entity
import java.util.*

class UploadFile() : Entity, Parcelable {
    var id: String? = null
    var folderId: String? = null
    var name: String? = null
    var uri: Uri? = null
    var size: String? = null
    var progress = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        folderId = parcel.readString()
        name = parcel.readString()
        uri = parcel.readParcelable(Uri::class.java.classLoader)
        size = parcel.readString()
        progress = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UploadFile
        return id == that.id &&
                folderId == that.folderId &&
                name == that.name &&
                uri == that.uri &&
                size == that.size
    }

    override fun hashCode(): Int {
        return Objects.hash(id, folderId, name, uri, size)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(folderId)
        parcel.writeString(name)
        parcel.writeParcelable(uri, flags)
        parcel.writeString(size)
        parcel.writeInt(progress)
    }

    companion object CREATOR : Parcelable.Creator<UploadFile> {
        override fun createFromParcel(parcel: Parcel): UploadFile {
            return UploadFile(parcel)
        }

        override fun newArray(size: Int): Array<UploadFile?> {
            return arrayOfNulls(size)
        }
    }

}