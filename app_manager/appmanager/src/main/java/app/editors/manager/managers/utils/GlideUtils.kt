package app.editors.manager.managers.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.webdav.WebDavService
import app.editors.manager.R
import lib.toolkit.base.R as R2
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.ManagerUiUtils.getFileThumbnail
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.Credentials
import java.net.URI
import java.security.MessageDigest


object GlideUtils {
    private fun getWebDavLoad(url: String, account: CloudAccount, password: String): Any {
        return GlideUrl(
            url, LazyHeaders.Builder()
                .addHeader(
                    WebDavService.HEADER_AUTHORIZATION,
                    Credentials.basic(account.login, password)
                )
                .build()
        )
    }

    fun getCorrectLoad(url: String?, token: String, portal: String? = null): Any {
        if (url.isNullOrEmpty()) {
            return Any()
        }

        val urlWithPortal = if (url.startsWith("http")) url else portal.orEmpty() + url

        return GlideUrl(
            URI(urlWithPortal).normalize().toString(), // remove duplicated slashes
            LazyHeaders.Builder()
                .addHeader(ApiContract.HEADER_AUTHORIZATION, token)
                .build()
        )
    }

    fun getWebDavUrl(webUrl: String, account: CloudAccount, password: String): Any {
        return getWebDavLoad(
            account.portal.scheme.value + account.portal.url + webUrl,
            account,
            password
        )
    }

    val avatarOptions: RequestOptions
        get() = RequestOptions()
            .timeout(30 * 1000)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.ic_account_placeholder)
            .placeholder(R.drawable.ic_account_placeholder)
            .circleCrop()

    suspend fun setAvatarFromUrl(context: Context, avatarUrl: String): Drawable? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                context.accountOnline?.let { account ->
                    val token = checkNotNull(AccountUtils.getToken(context, account.accountName))
                    val url = when {
                        avatarUrl.contains("http") -> avatarUrl
                        avatarUrl.contains("storage") -> "${account.portal.scheme}${account.portal.url}$avatarUrl"
                        else -> "${account.portal.scheme}${account.portal.url}/static/$avatarUrl"
                    }
                    Glide.with(context)
                        .asDrawable()
                        .load(getCorrectLoad(url, token))
                        .apply(avatarOptions)
                        .submit()
                        .get()
                }
            } catch (e: Exception) {
                null
            }
        }


    /**
     * Set Drawable avatar into ImageView
     * */
    @SuppressLint("CheckResult")
    fun ImageView.setAvatar(avatar: Drawable?) {
        Glide.with(context)
            .load(avatar)
            .apply(RequestOptions().circleCrop())
            .into(this)
    }

    fun ImageView.setRoomLogo(
        logo: String,
        isGrid: Boolean,
        isTemplate: Boolean,
        onLoadError: () -> Unit
    ) {
        context.accountOnline?.let { account ->
            val token = checkNotNull(AccountUtils.getToken(context, account.accountName))
            val url = getCorrectLoad(account.portal.scheme.value + account.portal.url + logo, token)

            val cornerRadiusRes = when {
                isGrid && isTemplate -> R2.dimen.default_corner_radius_medium
                isGrid -> R2.dimen.grid_card_view_corner_radius
                else -> R2.dimen.default_corner_radius_medium
            }

            Glide.with(context)
                .load(url)
                .apply(RequestOptions().timeout(30 * 1000))
                .transform(RoundedCorners(context.resources.getDimension(cornerRadiusRes).toInt()))
                .addListener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        onLoadError.invoke()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean = false
                })
                .into(this)
        }
    }

    /**
     * Load file thumbnail into ImageView
     * */
    fun ImageView.setFileThumbnailFromUrl(thumbnailUrl: String, ext: String) {
        val placeholderDrawable = getFileThumbnail(ext, isGrid = true)
        context.accountOnline?.let { account ->
            val token = checkNotNull(AccountUtils.getToken(context, account.accountName))
            val url = getCorrectLoad(thumbnailUrl, token)
            val resources = context.resources
            val cornerRadius = resources.getDimension(R2.dimen.default_corner_radius_small).toInt()
            Glide.with(context)
                .load(url)
                .apply(RequestOptions().timeout(30 * 1000))
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(
                    MultiTransformation(
                        CenterCrop(),
                        RoundedCorners(cornerRadius),
                        BorderTransformation(
                            cRadius = cornerRadius,
                            bWidth = resources.getDimension(R2.dimen.line_separator_height).toInt(),
                            bColor = resources.getColor(R2.color.colorFileThumbnailBorder, context.theme)
                        )
                    )
                )
                .error(placeholderDrawable)
                .into(this)
        }
    }

    private class BorderTransformation(
        private val cRadius: Int,
        private val bWidth: Int,
        private val bColor: Int
    ) : BitmapTransformation() {

        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap {
            val bitmap = pool.get(toTransform.width, toTransform.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(toTransform, 0f, 0f, null)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = bColor
                style = Paint.Style.STROKE
                strokeWidth = bWidth.toFloat()
            }

            val halfWidth = bWidth / 2f
            canvas.drawRoundRect(
                halfWidth,
                halfWidth,
                toTransform.width.toFloat() - halfWidth,
                toTransform.height.toFloat() - halfWidth,
                cRadius.toFloat(),
                cRadius.toFloat(),
                paint
            )
            return bitmap
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(("border_transformation_$cRadius${bWidth}$bColor").toByteArray())
        }
    }
}

/**
 * Load avatar into ImageView
 * */
fun ImageView.setAvatarFromUrl(avatar: String) {
    val placeholderDrawable = R.drawable.drawable_list_share_image_item_user_placeholder
    context.accountOnline?.let { account ->
        val token = checkNotNull(AccountUtils.getToken(context, account.accountName))
        val url = GlideUtils.getCorrectLoad(
            account.portal.scheme.value + account.portal.url + avatar,
            token
        )
        Glide.with(context)
            .load(url)
            .apply(
                RequestOptions().skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .timeout(30 * 1000)
                    .circleCrop()
                    .error(placeholderDrawable)
                    .placeholder(placeholderDrawable)
            )
            .into(this)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideAvatarImage(modifier: Modifier = Modifier, url: String) {
    val context = LocalContext.current
    val token: String?
    val account: CloudAccount?

    if (!LocalView.current.isInEditMode) {
        account = context.accountOnline
        token = account?.accountName
            ?.takeIf(String::isNotEmpty)
            ?.let { AccountUtils.getToken(context, it) }
    } else {
        account = null
        token = null
    }

    GlideImage(
        modifier = modifier,
        model = GlideUtils.getCorrectLoad(
            url = if (StringUtils.hasScheme(url)) url else "${account?.portal?.urlWithScheme}$url",
            token = token.orEmpty()
        ),
        contentScale = ContentScale.FillBounds,
        contentDescription = null,
        loading = placeholder(R.drawable.ic_account_placeholder),
        failure = placeholder(R.drawable.ic_account_placeholder),
    )
}
