package app.editors.manager.ui.fragments.share

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.app.api
import app.editors.manager.app.shareApi
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.ui.compose.share.ShareScreen
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs

class ShareFragment : ComposeDialogFragment() {

    companion object {
        private const val KEY_SHARE_DATA: String = "KEY_SHARE_DATA"

        private fun newInstance(item: Item, roomType: Int): ShareFragment {
            return ShareFragment().putArgs(
                KEY_SHARE_DATA to ShareData.from(item, roomType),
            )
        }

        fun show(activity: FragmentActivity, item: Item, roomType: Int) {
            newInstance(item, roomType).show(activity.supportFragmentManager, null)
        }
    }

    private val shareData: ShareData by lazy {
        arguments?.getSerializableExt(KEY_SHARE_DATA) ?: ShareData()
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            ShareScreen(
                shareData = shareData,
                shareApi = requireContext().shareApi,
                managerService = requireContext().api,
                useTabletPaddings = false,
                onClose = ::dismiss
            )
        }
    }
}