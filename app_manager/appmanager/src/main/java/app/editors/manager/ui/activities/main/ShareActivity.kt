@file:Suppress("FunctionName")

package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import app.editors.manager.app.api
import app.editors.manager.app.shareApi
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.share.ShareScreen

class ShareActivity : BaseAppActivity() {

    companion object {
        private const val KEY_SHARE_ITEM_ID: String = "KEY_SHARE_ITEM_ID"
        private const val KEY_SHARE_IS_FOLDER: String = "KEY_SHARE_IS_FOLDER"

        @JvmStatic
        fun show(fragment: Fragment, itemId: String, isFolder: Boolean) {
            fragment.startActivityForResult(
                Intent(fragment.context, ShareActivity::class.java).apply {
                    putExtra(KEY_SHARE_ITEM_ID, itemId)
                    putExtra(KEY_SHARE_IS_FOLDER, isFolder)
                },
                REQUEST_ACTIVITY_SHARE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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