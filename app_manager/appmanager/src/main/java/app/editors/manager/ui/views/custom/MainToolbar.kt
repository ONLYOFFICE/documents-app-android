package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.GlideUtils
import com.bumptech.glide.Glide
import lib.toolkit.base.managers.utils.AccountUtils


class MainToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Toolbar(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.include_toolbar, this)
    }

    val toolbar: Toolbar = findViewById(R.id.toolbar)

    private val accountContainer by lazy { findViewById<ConstraintLayout>(R.id.accountContainer) }
    private val arrowIcon by lazy { findViewById<ImageView>(R.id.toolbarArrowIcon) }
    private val toolbarIcon by lazy { findViewById<ImageView>(R.id.toolbarIcon) }
    private val title by lazy { findViewById<AppCompatTextView>(R.id.toolbarTitle) }
    private val subtitle by lazy { findViewById<AppCompatTextView>(R.id.toolbarSubTitle) }


    var account: CloudAccount? = context.accountOnline

    var accountListener: ((view: View) -> Unit)? = null
        set(value) {
            field = value
            accountContainer.setOnClickListener { value?.invoke(it) }
        }

    fun showAccount(isShow: Boolean) {
        accountContainer.isVisible = isShow
        arrowIcon.isVisible = isShow
    }

    fun bind() {
        account?.let { cloudAccount ->
            title.text = cloudAccount.name
            subtitle.text = cloudAccount.portal.url
            if (cloudAccount.isWebDav) {
                setWebDavAvatar(cloudAccount.portal.provider)
            } else if (cloudAccount.isOneDrive) {
                setOneDriveAvatar()
            } else if(cloudAccount.isDropbox) {
                if(cloudAccount.avatarUrl.isEmpty()) {
                    setDropboxAvatar()
                } else {
                    loadAvatar(cloudAccount)
                }
            } else {
                loadAvatar(cloudAccount)
            }
        } ?: run {
            showAccount(false)
        }
    }

    private fun loadAvatar(cloudAccount: CloudAccount) {
        AccountUtils.getToken(
            context,
            account?.accountName.orEmpty()
        )?.let {
            val url = if (
                cloudAccount.avatarUrl.contains(ApiContract.SCHEME_HTTP) ||
                cloudAccount.avatarUrl.contains(ApiContract.SCHEME_HTTPS) ||
                cloudAccount.isDropbox || cloudAccount.isGoogleDrive
            ) {
                cloudAccount.avatarUrl
            } else {
                cloudAccount.portal.urlWithScheme + cloudAccount.avatarUrl
            }
            Glide.with(context)
                .load(GlideUtils.getCorrectLoad(url, it))
                .apply(GlideUtils.avatarOptions)
                .into(toolbarIcon)
        } ?: run {
            Glide.with(context).load(R.drawable.ic_account_placeholder)
                .into(toolbarIcon)
        }
    }

    private fun setOneDriveAvatar() {
        toolbarIcon.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_storage_onedrive
            )
        )
    }

    private fun setDropboxAvatar() {
        toolbarIcon.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_storage_dropbox
            )
        )
    }

    private fun setWebDavAvatar(provider: PortalProvider) {
        when (WebdavProvider.valueOf(provider)) {
            is WebdavProvider.NextCloud -> toolbarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_storage_nextcloud
                )
            )
            WebdavProvider.Yandex -> toolbarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_storage_yandex
                )
            )
            WebdavProvider.OwnCloud -> toolbarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_storage_owncloud
                )
            )
            WebdavProvider.KDrive -> toolbarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_storage_kdrive
                )
            )

            else -> {
                toolbarIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_storage_webdav
                    )
                )
            }
        }
    }

}