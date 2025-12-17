package app.editors.manager.ui.fragments.share.link

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
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
        private const val TAG_FRAGMENT_RESULT = "ShareSettingsFragment Result"
        const val KEY_RESULT_SHARED = "KEY_RESULT_SHARED"

        private fun newInstance(): ShareSettingsFragment = ShareSettingsFragment()

        fun show(fragmentManager: FragmentManager, item: Item, roomType: Int) {
            newInstance()
                .putArgs(KEY_SHARE_DATA to ShareData.from(item, roomType))
                .show(fragmentManager, TAG)
        }

        fun show(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            item: Item,
            roomType: Int,
            onResult: (Bundle) -> Unit
        ) {
            fragmentManager.setFragmentResultListener(
                TAG_FRAGMENT_RESULT, lifecycleOwner
            ) { _, bundle -> onResult(bundle) }

            show(fragmentManager,item, roomType)
        }
    }

    private val shareData: ShareData by lazy {
        arguments?.getSerializableExt(KEY_SHARE_DATA) ?: ShareData()
    }

    private fun dismissWithResult(shared: Boolean?) {
        if (shared != null) {
            parentFragmentManager.setFragmentResult(
                TAG_FRAGMENT_RESULT,
                bundleOf(KEY_RESULT_SHARED to shared)
            )
        }
        dismiss()
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            ShareDocSpaceScreen(
                roomProvider = requireContext().roomProvider,
                shareData = shareData,
                useTabletPadding = false,
                onClose = ::dismissWithResult,
                onSendLink = { link ->
                    requireContext().openSendTextActivity(
                        getString(R.string.toolbar_menu_main_share),
                        link
                    )
                },
                onShowSnackbar = ::showSnackbar
            )
        }
    }
}