package app.editors.manager.managers.utils

import android.widget.ImageView
import androidx.core.content.ContextCompat
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R

object UiUtils {

    @JvmStatic
    fun setWebDavImage(providerName: String?, image: ImageView) {
        when (WebDavApi.Providers.valueOf(providerName ?: "")) {
            WebDavApi.Providers.NextCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_nextcloud
                )
            )
            WebDavApi.Providers.OwnCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_owncloud
                )
            )
            WebDavApi.Providers.Yandex -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_yandex
                )
            )
            WebDavApi.Providers.WebDav -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_webdav
                )
            )
        }
    }
}