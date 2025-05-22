package app.editors.manager.ui.fragments.main.template.settings

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.viewModels.main.TemplateSettingsMode
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.putArgs

class TemplateAccessSettingsFragment : ComposeDialogFragment() {

    private val templateId: String
        get() = arguments?.getString(KEY_TEMPLATE_ID).orEmpty()

    @Composable
    override fun Content() {
        ManagerTheme {
            AccessSettingsDestination(
                templateId = templateId,
                modeId = TemplateSettingsMode.MODE_EDIT_TEMPLATE,
                initSettings = null,
                showSnackbar = {
                    UiUtils.getSnackBar(requireView()).setText(it).show()
                },
                onSavedSuccessfully = {
                    setResultMsg()
                    dismiss()
                },
                onClose = ::dismiss
            )
        }
    }

    private fun setResultMsg() {
        parentFragmentManager.setFragmentResult(
            TAG_FRAGMENT_RESULT,
            bundleOf()
        )
    }

    companion object {
        private val TAG: String = TemplateAccessSettingsFragment::class.java.simpleName
        private const val KEY_TEMPLATE_ID = "key_template_id"
        private const val TAG_FRAGMENT_RESULT = "TemplateAccessSettingsFragmentResult"

        fun show(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            templateId: String,
            onResult: (Bundle) -> Unit
        ) {
            fragmentManager.setFragmentResultListener(
                TAG_FRAGMENT_RESULT,
                lifecycleOwner
            ) { _, bundle -> onResult(bundle) }

            newInstance(templateId).show(fragmentManager, TAG)
        }

        private fun newInstance(templateId: String) =
            TemplateAccessSettingsFragment().putArgs(KEY_TEMPLATE_ID to templateId)
    }
}