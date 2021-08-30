package app.editors.manager.managers.utils

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.AccountUtils
import okhttp3.Credentials

object GlideUtils {
    private fun getWebDavLoad(url: String, account: CloudAccount, password: String): Any {
        return GlideUrl(
            url, LazyHeaders.Builder()
                .addHeader(
                    WebDavApi.HEADER_AUTHORIZATION,
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

    /**
     * Load avatar if use RxJava
     * */
    fun loadAvatar(avatar: String): Drawable {
        val context = App.getApp().appComponent.context
        val placeholderDrawable = R.drawable.drawable_list_share_image_item_user_placeholder
        var drawable = ResourcesProvider(context).getDrawable(placeholderDrawable)
            ?: throw RuntimeException("Invalid drawable")
        context.accountOnline?.let { account ->
            val token = checkNotNull(AccountUtils.getToken(context, account.getAccountName()))
            val url = getCorrectLoad(account.scheme + account.portal + avatar, token)
            drawable = Glide.with(context)
                .asDrawable()
                .load(url)
                .submit().get()
            return drawable
        } ?: return drawable
    }

    /**
     * Load avatar into ImageView
     * */
    fun ImageView.loadAvatar(avatar: String) {
        val placeholderDrawable = R.drawable.drawable_list_share_image_item_user_placeholder
        context.accountOnline?.let { account ->
            val token = checkNotNull(AccountUtils.getToken(context, account.getAccountName()))
            val url = getCorrectLoad(account.scheme + account.portal + avatar, token)
            Glide.with(context)
                .load(url)
                .apply(RequestOptions().apply {
                    skipMemoryCache(true)
                    diskCacheStrategy(DiskCacheStrategy.NONE)
                    timeout(30 * 1000)
                    circleCrop()
                    error(placeholderDrawable)
                    placeholder(placeholderDrawable)
                })
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
            .apply(RequestOptions().apply {
                skipMemoryCache(true)
                diskCacheStrategy(DiskCacheStrategy.NONE)
                timeout(30 * 1000)
                circleCrop()
            })
            .into(this)
    }
}