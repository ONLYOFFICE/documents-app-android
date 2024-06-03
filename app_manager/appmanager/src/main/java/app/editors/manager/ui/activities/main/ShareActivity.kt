package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.Fragment
import app.editors.manager.ui.activities.base.BaseAppActivity

class ShareActivity : BaseAppActivity() {

    companion object {
        private const val KEY_SHARE_ITEM_ID: String = "KEY_SHARE_ITEM_ID"

        @JvmStatic
        fun show(fragment: Fragment, itemId: String) {
            fragment.startActivityForResult(
                Intent(fragment.context, ShareActivity::class.java).apply { putExtra(KEY_SHARE_ITEM_ID, itemId) },
                REQUEST_ACTIVITY_SHARE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }
}