package app.editors.manager.ui.fragments.share.link

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.ui.compose.share.ShareDocSpaceScreen
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs


class ShareSettingsFragment : ComposeDialogFragment() {

    companion object {

        private const val KEY_SHARE_DATA = "KEY_SHARE_DATA"
        private val TAG = ShareSettingsFragment::class.simpleName

        private fun newInstance(): ShareSettingsFragment = ShareSettingsFragment()

        fun show(activity: FragmentActivity, item: Item) {
            newInstance()
                .putArgs(KEY_SHARE_DATA to ShareData.from(item))
                .show(activity.supportFragmentManager, TAG)
        }
    }

    private val shareData: ShareData by lazy {
        arguments?.getSerializableExt(KEY_SHARE_DATA) ?: ShareData()
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            ShareDocSpaceScreen(
                roomProvider = requireContext().roomProvider,
                shareData = shareData,
                useTabletPadding = false,
                onClose = ::dismiss,
                onSendLink = { link ->
                    requireContext().openSendTextActivity(
                        getString(R.string.toolbar_menu_main_share),
                        link
                    )
                }
            )
        }
    }
}