package app.editors.manager.mvp.models.ui

import android.os.Parcel
import android.os.Parcelable

data class SectionInfo(
    val type: Int,
    val id: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        type = parcel.readInt(),
        id = parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeString(id)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SectionInfo> {
        override fun createFromParcel(parcel: Parcel): SectionInfo = SectionInfo(parcel)
        override fun newArray(size: Int): Array<SectionInfo?> = arrayOfNulls(size)
    }
}