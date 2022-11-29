package app.documents.core.network.common.models

import android.os.Parcelable
import android.os.Parcel

class Storage(var name: String? = null, var clientId: String? = null, var redirectUrl: String? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(clientId)
        parcel.writeString(redirectUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Storage> {
        override fun createFromParcel(parcel: Parcel): Storage {
            return Storage(parcel)
        }

        override fun newArray(size: Int): Array<Storage?> {
            return arrayOfNulls(size)
        }
    }

}