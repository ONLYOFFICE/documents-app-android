package app.editors.manager.ui.fragments.share.link

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.ui.compose.share.ShareDocSpaceScreen
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.viewModels.link.ShareSettingsViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs

sealed class Route(val name: String) {

    data object SettingsScreen : Route("settings")
    data object LinkSettingsScreen : Route("link_settings")
}

class ShareSettingsFragment : ComposeDialogFragment() {

    companion object {

        private const val KEY_FILE_ID = "KEY_FILE_ID"
        private const val KEY_FILE_EXTENSION = "KEY_FILE_EXTENSION"
        private val TAG = ShareSettingsFragment::class.simpleName

        private fun newInstance(): ShareSettingsFragment = ShareSettingsFragment()

        fun show(activity: FragmentActivity, fileId: String?, extension: String) {
            newInstance()
                .putArgs(KEY_FILE_ID to fileId)
                .putArgs(KEY_FILE_EXTENSION to extension)
                .show(activity.supportFragmentManager, TAG)
        }
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            ShareDocSpaceScreen(
                viewModel = viewModel {
                    ShareSettingsViewModel(
                        roomProvider = requireContext().roomProvider,
                        fileId = arguments?.getString(KEY_FILE_ID).orEmpty(),
                    )
                },
                fileExtension = arguments?.getString(KEY_FILE_EXTENSION).orEmpty(),
                onSendLink = { link ->
                    requireContext().openSendTextActivity(
                        getString(R.string.toolbar_menu_main_share),
                        link
                    )
                },
                useTabletPadding = false,
                onClose = ::dismiss
            )
        }
    }
}