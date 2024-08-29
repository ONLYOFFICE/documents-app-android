package app.editors.manager.ui.fragments.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import app.editors.manager.app.api
import app.editors.manager.app.shareApi
import app.editors.manager.ui.compose.share.ShareScreen
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.putArgs

class ShareFragment : ComposeDialogFragment() {

    companion object {
        private const val KEY_SHARE_ITEM_ID: String = "KEY_SHARE_ITEM_ID"
        private const val KEY_SHARE_IS_FOLDER: String = "KEY_SHARE_IS_FOLDER"

        private fun newInstance(itemId: String, isFolder: Boolean): ShareFragment {
            return ShareFragment().putArgs(
                KEY_SHARE_ITEM_ID to itemId,
                KEY_SHARE_IS_FOLDER to isFolder
            )
        }

        fun show(activity: FragmentActivity, itemId: String, isFolder: Boolean) {
            newInstance(itemId, isFolder).show(activity.supportFragmentManager, null)
        }
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            ShareScreen(
                itemId = remember { arguments?.getString(KEY_SHARE_ITEM_ID) }.orEmpty(),
                isFolder = remember { arguments?.getBoolean(KEY_SHARE_IS_FOLDER) } ?: false,
                shareApi = requireContext().shareApi,
                managerService = requireContext().api,
                useTabletPaddings = false,
                onClose = ::dismiss
            )
        }
    }
}