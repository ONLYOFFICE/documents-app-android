package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import app.documents.core.account.CloudAccount
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
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


    var account: CloudAccount? = null

    var accountListener: ((view: View) -> Unit)? = null
        set(value) {
            field = value
            accountContainer.setOnClickListener { value?.invoke(it) }
        }

    fun showAccount(isShow: Boolean) {
        accountContainer.visibility = if (isShow) View.VISIBLE else View.GONE
        arrowIcon.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    fun bind(cloudAccount: CloudAccount?) {
        account = cloudAccount
        cloudAccount?.let {
            title.text = cloudAccount.name
            subtitle.text = cloudAccount.portal
            if (cloudAccount.isWebDav) {
                setWebDavAvatar(cloudAccount.webDavProvider ?: "")
            } else if (cloudAccount.isOneDrive) {
                setOneDriveAvatar()
            } else {
                loadAvatar(it)
            }
        } ?: run {
            showAccount(false)
        }
    }

    private fun loadAvatar(cloudAccount: CloudAccount) {
        AccountUtils.getToken(
            context,
            account?.getAccountName() ?: ""
        )?.let {
            val url = if (cloudAccount.avatarUrl?.contains("static") == true || cloudAccount.avatarUrl?.contains("default") == true || cloudAccount.isDropbox) {
                cloudAccount.avatarUrl
            } else {
                cloudAccount.scheme + cloudAccount.portal + cloudAccount.avatarUrl
            }
            Glide.with(context)
                .load(GlideUtils.getCorrectLoad(url ?: "", it))
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

    private fun setWebDavAvatar(provider: String) {
        when (WebDavApi.Providers.valueOf(provider)) {
            WebDavApi.Providers.Yandex -> toolbarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_storage_yandex
                )
            )
            WebDavApi.Providers.NextCloud -> toolbarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_storage_nextcloud
                )
            )
            WebDavApi.Providers.OwnCloud -> toolbarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_storage_owncloud
                )
            )
            WebDavApi.Providers.KDrive -> toolbarIcon.setImageDrawable(
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