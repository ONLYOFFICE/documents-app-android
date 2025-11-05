package app.editors.manager.ui.fragments.share.link

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.ui.compose.share.ShareDocSpaceScreen
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.tools.FileExtensions
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs


class ShareSettingsFragment : ComposeDialogFragment() {

    companion object {

        private const val KEY_FILE_ID = "KEY_FILE_ID"
        private const val KEY_FILE_EXTENSION = "KEY_FILE_EXTENSION"
        private val TAG = ShareSettingsFragment::class.simpleName

        private fun newInstance(): ShareSettingsFragment = ShareSettingsFragment()

        fun show(activity: FragmentActivity, fileId: String?, extension: String?) {
            newInstance()
                .putArgs(KEY_FILE_ID to fileId)
                .putArgs(KEY_FILE_EXTENSION to extension)
                .show(activity.supportFragmentManager, TAG)
        }
    }

    private val extension: FileExtensions? by lazy {
        arguments?.getString(KEY_FILE_EXTENSION)?.let { extension ->
            FileExtensions.fromExtension(extension)
        }
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            ShareDocSpaceScreen(
                roomProvider = requireContext().roomProvider,
                itemId = arguments?.getString(KEY_FILE_ID).orEmpty(),
                fileExtension = extension,
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