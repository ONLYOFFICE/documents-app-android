package app.documents.core.network.manager.models.explorer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

private const val MASK_USERNAME = 1
private const val MASK_EMAIL = 2
private const val MASK_IP_ADDRESS = 4
private const val MASK_CURRENT_DATE = 8
private const val MASK_ROOM_NAME = 16

private const val ROTATE_ANGLE_DIAGONAL = -45
private const val ROTATE_ANGLE_HORIZONTAL = 0

sealed class WatermarkInfo(val mask: Int) {

    data object Username : WatermarkInfo(MASK_USERNAME)
    data object Email : WatermarkInfo(MASK_EMAIL)
    data object IpAddress : WatermarkInfo(MASK_IP_ADDRESS)
    data object CurrentDate : WatermarkInfo(MASK_CURRENT_DATE)
    data object RoomName : WatermarkInfo(MASK_ROOM_NAME)

    companion object {

        fun values(): Array<WatermarkInfo> {
            return arrayOf(Username, Email, IpAddress, CurrentDate, RoomName)
        }
    }
}

@Serializable
sealed class WatermarkTextPosition(val angle: Int ) {

    data object Diagonal : WatermarkTextPosition(ROTATE_ANGLE_DIAGONAL)
    data object Horizontal : WatermarkTextPosition(ROTATE_ANGLE_HORIZONTAL)

    companion object {

        fun values(): Array<WatermarkTextPosition> {
            return arrayOf(Diagonal, Horizontal)
        }

        fun from(angle: Int): WatermarkTextPosition {
            return when (angle) {
                ROTATE_ANGLE_DIAGONAL -> Diagonal
                else -> Horizontal
            }
        }
    }
}

@Serializable
sealed class WatermarkType {

    data object Image : WatermarkType()
    data object ViewerInfo : WatermarkType()

    companion object {

        fun values(): List<WatermarkType> {
            return listOf(ViewerInfo, Image)
        }
    }
}

@Serializable
data class Watermark(
    @SerializedName("text")
    @Expose
    val text: String = "",

    @SerializedName("additions")
    @Expose
    val additions: Int = 0,

    @SerializedName("rotate")
    @Expose
    val rotate: Int = 0,

    @SerializedName("imageScale")
    @Expose
    val imageScale: Int = 0,

    @SerializedName("imageHeight")
    @Expose
    val imageHeight: Int = 0,

    @SerializedName("imageWidth")
    @Expose
    val imageWidth: Int = 0,

    @SerializedName("imageUrl")
    @Expose
    val imageUrl: String? = null,

    val type: WatermarkType = if (imageWidth == 0 || imageHeight == 0) {
        WatermarkType.ViewerInfo
    } else {
        WatermarkType.Image
    }
) {

    val textPosition: WatermarkTextPosition
        get() = WatermarkTextPosition.from(angle = rotate)

    val watermarkInfoList: List<WatermarkInfo>
        get() = WatermarkInfo.values()
            .mapNotNull { info -> info.takeIf { (additions and it.mask) != 0 } }
}