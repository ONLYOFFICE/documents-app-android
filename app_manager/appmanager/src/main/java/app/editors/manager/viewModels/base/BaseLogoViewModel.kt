package app.editors.manager.viewModels.base

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.runtime.Immutable
import androidx.core.graphics.decodeBitmap
import androidx.lifecycle.ViewModel
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class RoomSettingsLogoState(
    val logoWebUrl: String? = null,
    val logoUri: Uri? = null,
    val logoPreview: Bitmap? = null,
)

data class UploadedRoomLogo(
    val tmpFile: String,
    val size: Size
)

abstract class BaseLogoViewModel(
    private val contentResolver: ContentResolver,
    private val roomProvider: RoomProvider
) : ViewModel() {

    private val _logoState: MutableStateFlow<RoomSettingsLogoState> =
        MutableStateFlow(RoomSettingsLogoState())
    val logoState: StateFlow<RoomSettingsLogoState> = _logoState.asStateFlow()

    open fun setLogoUri(uri: Uri?) {
        _logoState.update { it.copy(logoUri = uri, logoPreview = uri?.let(::getBitmapFromUri)) }
    }

    fun updateLogoState(block: (RoomSettingsLogoState) -> RoomSettingsLogoState) {
        _logoState.update(block)
    }

    @Suppress("DEPRECATION")
    protected fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.createSource(contentResolver, uri).decodeBitmap { _, _ -> }
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    suspend fun getUploadedRoomLogo(
        onError: (Exception) -> Unit,
    ): UploadedRoomLogo? {
        try {
            with(logoState.value) {
                if (logoUri != null && logoPreview != null) {
                    return UploadedRoomLogo(
                        tmpFile = roomProvider.uploadImage(logoPreview),
                        size = Size(logoPreview.width, logoPreview.height)
                    )
                }
            }
        } catch (e: Exception) {
            onError(e)
        }
        return null
    }
}