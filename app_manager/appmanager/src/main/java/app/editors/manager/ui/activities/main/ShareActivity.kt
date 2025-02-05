@file:Suppress("FunctionName")

package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.model.cloud.isDocSpace
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.share.ShareDocSpaceScreen
import app.editors.manager.ui.compose.share.ShareScreen
import app.editors.manager.viewModels.link.ShareSettingsViewModel
import lib.compose.ui.theme.BaseAppTheme
import lib.compose.ui.theme.LocalUseTabletPadding
import lib.toolkit.base.managers.utils.openSendTextActivity

class ShareActivity : BaseAppActivity() {

    companion object {
        private const val KEY_SHARE_ITEM_ID: String = "KEY_SHARE_ITEM_ID"
        private const val KEY_SHARE_ITEM_EXTENSION: String = "KEY_SHARE_ITEM_EXTENSION"
        private const val KEY_SHARE_IS_FOLDER: String = "KEY_SHARE_IS_FOLDER"
        private const val KEY_EDITOR_COLOR: String = "KEY_EDITOR_COLOR"

        @JvmStatic
        fun show(fragment: Fragment, itemId: String, isFolder: Boolean, extension: String) {
            fragment.startActivityForResult(
                Intent(fragment.context, ShareActivity::class.java).apply {
                    putExtra(KEY_SHARE_ITEM_ID, itemId)
                    putExtra(KEY_SHARE_IS_FOLDER, isFolder)
                    putExtra(KEY_SHARE_ITEM_EXTENSION, extension)
                },
                REQUEST_ACTIVITY_SHARE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val color = intent?.getIntExtra(KEY_EDITOR_COLOR, -1)?.let { Color(it) }
                ?: Color(getColor(lib.toolkit.base.R.color.colorPrimary))

            CompositionLocalProvider(LocalUseTabletPadding provides true) {
                BaseAppTheme(primaryColor = color) {
                    if (accountOnline.isDocSpace) {
                        ShareDocSpaceScreen(
                            viewModel = viewModel {
                                ShareSettingsViewModel(
                                    roomProvider = roomProvider,
                                    fileId = intent.getStringExtra(KEY_SHARE_ITEM_ID).orEmpty(),
                                )
                            },
                            fileExtension = intent.getStringExtra(KEY_SHARE_ITEM_EXTENSION).orEmpty(),
                            onSendLink = { link ->
                                openSendTextActivity(
                                    title = getString(R.string.toolbar_menu_main_share),
                                    text = link
                                )
                            },
                            useTabletPadding = true,
                            onClose = ::finish
                        )
                    } else {
                        ShareScreen(
                            isFolder = remember { intent.getBooleanExtra(KEY_SHARE_IS_FOLDER, false) },
                            itemId = remember { intent.getStringExtra(KEY_SHARE_ITEM_ID).orEmpty() },
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