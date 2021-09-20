package lib.toolkit.base.managers.tools

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import java.io.File
import javax.inject.Inject

class GlideTool @Inject constructor(private val context: Context) {

    enum class Scale {
        NONE, CIRCLE, CENTER_CROP
    }

    fun load(into: ImageView, url: String, isSkipCache: Boolean, placeholder: Drawable?, error: Drawable?, listener: RequestListener<Bitmap>?) {
        load(into, url, getOptions(Scale.NONE, isSkipCache, placeholder, error, null), Bitmap::class.java, listener)
    }

    fun load(into: ImageView, bitmap: Bitmap, isSkipCache: Boolean) {
        load(into, bitmap, getOptions(Scale.NONE, isSkipCache, null, null, null), Bitmap::class.java, null)
    }

    fun load(into: ImageView, url: Any, isSkipCache: Boolean, size: Point?, listener: RequestListener<Bitmap>?) {
        load(into, url, getOptions(Scale.NONE, isSkipCache, null, null, size), Bitmap::class.java, listener)
    }

    fun loadCrop(into: ImageView, url: Any, isSkipCache: Boolean, @DrawableRes placeholder: Int, @DrawableRes error: Int) {
        load(into, url, getOptions(Scale.CIRCLE, isSkipCache, placeholder, error, null), Bitmap::class.java, null)
    }

    fun setCrop(into: ImageView, @DrawableRes resId: Int) {
        load(into, resId, getOptions(Scale.CIRCLE, true, null, null, null), Bitmap::class.java, null)
    }

    fun loadCrop(into: CustomTarget<Bitmap>, url: Any, isSkipCache: Boolean) {
        load(into, url, getOptions(Scale.CIRCLE, isSkipCache, null, null, null), Bitmap::class.java, null)
    }

    fun load(into: CustomTarget<Bitmap>, url: Any, isSkipCache: Boolean, listener: RequestListener<Bitmap>?) {
        load(into, url, getOptions(Scale.NONE, isSkipCache, null, null, null), Bitmap::class.java, listener)
    }

    fun loadGif(into: CustomTarget<GifDrawable>, url: Any, isSkipCache: Boolean, listener: RequestListener<GifDrawable>?) {
        load(into, url, getOptions(Scale.NONE, isSkipCache, null, null, null), GifDrawable::class.java, listener)
    }

    fun loadFile(into: CustomTarget<File>, url: String, isSkipCache: Boolean, listener: RequestListener<File>?) {
        load(into, url, getOptions(Scale.NONE, isSkipCache, null, null, null), File::class.java, listener)
    }

    fun loadDrawable(into: CustomTarget<Bitmap>, @DrawableRes resId: Int, isSkipCache: Boolean) {
        load(into, resId, getOptions(Scale.NONE, isSkipCache, null, null, null), Bitmap::class.java, null)
    }

    fun loadDrawable(into: ImageView, @DrawableRes resId: Int, isSkipCache: Boolean, listener: RequestListener<Bitmap>?) {
        load(into, resId, getOptions(Scale.NONE, isSkipCache, null, null, null), Bitmap::class.java, listener)
    }

    fun loadAsset(into: CustomTarget<Bitmap>, assetPath: String, isSkipCache: Boolean) {
        val uri = Uri.parse("file:///android_asset${assetPath}")
        load(into, uri, getOptions(Scale.NONE, isSkipCache, null, null, null), Bitmap::class.java, null)
    }

    fun loadAsset(into: ImageView, assetPath: String, isSkipCache: Boolean, listener: RequestListener<Bitmap>?, scaleType: Scale = Scale.NONE) {
        val uri = Uri.parse("file:///android_asset${assetPath}")
        load(into, uri, getOptions(scaleType, isSkipCache, null, null, null), Bitmap::class.java, listener)
    }

    fun clear(into: Any): Boolean {
        return try {
            if (into is Target<*>) {
                Glide.with(context).clear(into)
            } else {
                Glide.with(context).clear(into as ImageView)
            }
            true
        } catch (e: RuntimeException) {
            false
        }
    }

    /*
    * Base methods
    * */
    private fun <T> load(into: Any, load: Any, options: RequestOptions, tClass: Class<T>, requestListener: RequestListener<T>?) {
        Glide.with(context).`as`(tClass).apply {
            load(load)
            apply(options)
            listener(requestListener)
            if (into is Target<*>) {
                into(into as Target<T>)
            } else {
                into(into as ImageView)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun getOptions(scaleType: Scale, isSkipCache: Boolean,
                           placeholder: Drawable?, error: Drawable?,
                           size: Point?): RequestOptions {
        return RequestOptions().apply {
            timeout(30 * 1000)
            placeholder(placeholder)
            error(error)

            if (isSkipCache) {
                skipMemoryCache(true)
                diskCacheStrategy(DiskCacheStrategy.NONE)
            } else {
                skipMemoryCache(false)
                diskCacheStrategy(DiskCacheStrategy.ALL)
            }
            when(scaleType) {
                Scale.CIRCLE -> circleCrop()
                Scale.CENTER_CROP -> centerCrop()
                else -> centerCrop()
            }

            if (size != null) {
                override(size.x, size.y)
            }
        }
    }

    private fun getOptions(scaleType: Scale, isSkipCache: Boolean, @DrawableRes placeholder: Int,
                           @DrawableRes error: Int, size: Point?): RequestOptions {
        return getOptions(scaleType, isSkipCache, getDrawable(placeholder), getDrawable(error), size)
    }

    private fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

}
