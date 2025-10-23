package lib.toolkit.base.managers.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Spanned
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.StringUtils
import java.io.File
import javax.inject.Inject

class ResourcesProvider @Inject constructor(val context: Context) {

    fun getString(@StringRes res: Int) = context.getString(res)

    fun getString(@StringRes res: Int, vararg args: String) = context.getString(res, *args)

    fun getStringArray(@ArrayRes res: Int): Array<String> = context.resources.getStringArray(res)

    fun getDrawable(@DrawableRes res: Int) = AppCompatResources.getDrawable(context, res)

    fun getColor(@ColorRes res: Int) = ContextCompat.getColor(context, res)

    fun getDimen(@DimenRes res: Int) = context.resources.getDimension(res)

    fun getCacheDir(isExternal: Boolean): File? {
        return if (isExternal) {
            context.externalCacheDir
        } else {
            context.cacheDir
        }
    }

    fun getLocale(): String? {
        return context.resources.configuration.locales[0].country
    }

    fun getFileNameByUri(uri: Uri): String = ContentResolverUtils.getName(context, uri)

    fun getMimeTypeByUri(uri: Uri): String? = ContentResolverUtils.getMimeType(context, uri)

    fun getHtmlSpanned(@StringRes resource: Int, vararg args: String): Spanned {
        return StringUtils.getHtmlSpanned(
            context.getString(
                resource,
                *args
            )
        )
    }

    suspend fun getBitmapByUri(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream).also {
            inputStream?.close()
        }
    }

    suspend fun getBitmapByUrl(url: String): Bitmap = withContext(Dispatchers.IO) {
        val imageBytes = Glide.with(context)
            .`as`(ByteArray::class.java)
            .load(url)
            .submit()
            .get()
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
