package app.editors.manager.managers.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.webdav.WebDavService
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import okhttp3.Credentials

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

    fun getCorrectLoad(url: String, token: String): Any {
        return GlideUrl(
            url, LazyHeaders.Builder()
                .addHeader(ApiContract.HEADER_AUTHORIZATION, token)
                .build()
        )
    }

    fun getWebDavUrl(webUrl: String, account: CloudAccount, password: String): Any {
        return getWebDavLoad(account.scheme + account.portal + webUrl, account, password)
    }

    val avatarOptions: RequestOptions
        get() = RequestOptions()
            .timeout(30 * 1000)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.ic_account_placeholder)
            .placeholder(R.drawable.ic_account_placeholder)
            .circleCrop()

    suspend fun getAvatarFromUrl(context: Context, avatarUrl: String): Drawable? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                context.accountOnline?.let { account ->
                    val token = checkNotNull(AccountUtils.getToken(context, account.getAccountName()))
                    val url = when {
                        avatarUrl.contains("http") -> avatarUrl
                        avatarUrl.contains("storage") -> "${account.scheme}${account.portal}$avatarUrl"
                        else -> "${account.scheme}${account.portal}/static/$avatarUrl"
                    }
                    Glide.with(context)
                        .asDrawable()
                        .load(getCorrectLoad(url, token))
                        .submit()
                        .get()
                }
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Load avatar into ImageView
     * */
    fun ImageView.getAvatarFromUrl(avatar: String) {
        val placeholderDrawable = R.drawable.drawable_list_share_image_item_user_placeholder
        context.accountOnline?.let { account ->
            val token = checkNotNull(AccountUtils.getToken(context, account.getAccountName()))
            val url = getCorrectLoad(account.scheme + account.portal + avatar, token)
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

    /**
     * Set Drawable avatar into ImageView
     * */
    @SuppressLint("CheckResult")
    fun ImageView.setAvatar(avatar: Drawable?) {
        Glide.with(context)
            .load(avatar)
            .apply(
                RequestOptions()
                    .circleCrop()
            )
            .into(this)
    }
}