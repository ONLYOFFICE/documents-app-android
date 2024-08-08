package app.editors.manager.managers.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.webdav.WebDavService
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import okhttp3.Credentials
import java.net.URI


object GlideUtils {
    private fun getWebDavLoad(url: String, account: CloudAccount, password: String): Any {
        return GlideUrl(
            url, LazyHeaders.Builder()
                .addHeader(
                    WebDavService.HEADER_AUTHORIZATION,
                    Credentials.basic(account.login!!, password)
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

    fun ImageView.setRoomLogo(logo: String, isGrid: Boolean, onLoadError: () -> Unit) {
        context.accountOnline?.let { account ->
            val token = checkNotNull(AccountUtils.getToken(context, account.accountName))
            val url = getCorrectLoad(account.portal.scheme.value + account.portal.url + logo, token)

            val cornerRadius = if (isGrid) {
                context.resources.getDimension(lib.toolkit.base.R.dimen.grid_card_view_corner_radius)
            } else {
                context.resources.getDimension(lib.toolkit.base.R.dimen.default_corner_radius_medium)
            }

            Glide.with(context)
                .load(url)
                .apply(RequestOptions().timeout(30 * 1000))
                .transform(RoundedCorners(cornerRadius.toInt()))
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