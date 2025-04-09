package lib.toolkit.base.managers.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImagePickerUtils(private val context: Context) {

    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        const val STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private var currentPhotoPath: String? = null

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("images")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun getCameraIntent(): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()

        val photoURI = FileProvider.getUriForFile(
            context,
            "${context.packageName}.asc.provider",
            photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        return intent
    }

    fun getImageFromCameraResult(): Bitmap? {
        return currentPhotoPath?.let { path ->
            BitmapFactory.decodeFile(path)
        }
    }

    fun getImageFromGallery(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

}