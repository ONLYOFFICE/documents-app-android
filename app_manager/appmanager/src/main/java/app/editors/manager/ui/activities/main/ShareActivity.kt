@file:Suppress("FunctionName")

package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import app.documents.core.model.cloud.isDocSpace
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.share.ShareDocSpaceScreen
import app.editors.manager.ui.compose.share.ShareScreen
import lib.compose.ui.theme.BaseAppTheme
import lib.compose.ui.theme.LocalUseTabletPadding
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.openSendTextActivity

class ShareActivity : BaseAppActivity() {

    companion object {
        private const val KEY_SHARE_DATA = "key_share_data"

        @JvmStatic
        fun show(fragment: Fragment, shareData: ShareData) {
            fragment.startActivityForResult(
                Intent(fragment.context, ShareActivity::class.java).apply {
                    putExtra(EditorsContract.EXTRA_ITEM_ID, shareData.itemId) // check editors
                    putExtra(KEY_SHARE_DATA, shareData)
                },
                REQUEST_ACTIVITY_SHARE
            )
        }
    }

    private val shareData: ShareData by lazy {
        intent.getSerializableExt<ShareData>(KEY_SHARE_DATA) ?: ShareData(
            itemId = intent.getStringExtra(EditorsContract.EXTRA_ITEM_ID).orEmpty()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val color = intent?.getIntExtra(EditorsContract.EXTRA_THEME_COLOR, -1)?.let { Color(it) }
                ?: Color(getColor(lib.toolkit.base.R.color.colorPrimary))

            CompositionLocalProvider(LocalUseTabletPadding provides true) {
                BaseAppTheme(primaryColor = color) {
                    if (accountOnline.isDocSpace) {
                        ShareDocSpaceScreen(
                            roomProvider = roomProvider,
                            shareData = shareData,
                            useTabletPadding = true,
                            onClose = ::finish,
                            onSendLink = { link ->
                                openSendTextActivity(
                                    title = getString(R.string.toolbar_menu_main_share),
                                    text = link
                                )
                            }
                        )
                    } else {
                        ShareScreen(
                            shareData = shareData,
                            useTabletPaddings = true,
                            shareApi = shareApi,
                            managerService = api,
                            onClose = ::finish
                        )
                    }
                }
            }
        }
    }
}