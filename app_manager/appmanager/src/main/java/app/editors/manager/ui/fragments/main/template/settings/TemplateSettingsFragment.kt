package app.editors.manager.ui.fragments.main.template.settings

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.viewModels.main.TemplateSettingsMode
import app.editors.manager.viewModels.main.TemplateSettingsViewModel
import kotlinx.serialization.Serializable
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.putArgs

class TemplateSettingsFragment : ComposeDialogFragment() {

    private val templateId: String
        get() = arguments?.getString(KEY_TEMPLATE_ID).orEmpty()

    private val settingsMode: Int
        get() = arguments?.getInt(KEY_SETTINGS_MODE) ?: TemplateSettingsMode.MODE_CREATE_TEMPLATE

    @Composable
    override fun Content() {
        val navController = rememberNavController()
        val viewModel = viewModel<TemplateSettingsViewModel>(
            factory = TemplateSettingsViewModel.factory(
                templateId = templateId,
                modeId = settingsMode
            )
        )

        ManagerTheme {
            NavHost(
                navController = navController,
                startDestination = Screens.MainSettings
            ) {
                composable<Screens.MainSettings> {
                    TemplateSettingsScreen(
                        viewModel = viewModel,
                        showSnackbar = ::showSnackbar,
                        navigateToAccessSettings = {
                            navController.navigate(Screens.AccessSettingsDestination)
                        },
                        navigateToCreated = { id, type, title ->
                            setResult(id, title, type)
                            dismiss()
                        },
                        onBack = ::dismiss
                    )
                }

                composable<Screens.AccessSettingsDestination> {
                    AccessSettingsDestination(
                        templateId = templateId,
                        modeId = settingsMode,
                        initSettings = viewModel.uiState.value.accessSettings,
                        showSnackbar = ::showSnackbar,
                        onSaveClick = { settings ->
                            viewModel.updateAccessSettings(settings)
                            navController.popBackStackWhenResumed()
                        },
                        onClose = navController::popBackStackWhenResumed
                    )
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        UiUtils.getSnackBar(requireView()).setText(message).show()
    }

    private fun setResult(id: String?, title: String?, roomType: Int?) {
        parentFragmentManager.setFragmentResult(
            TAG_FRAGMENT_RESULT,
            bundleOf(
                KEY_SAVED_ID to id,
                KEY_SAVED_TITLE to title,
                KEY_SAVED_ROOM_TYPE to roomType
            )
        )
    }

    sealed interface Screens {
        @Serializable
        data object MainSettings : Screens

        @Serializable
        data object AccessSettingsDestination : Screens
    }

    companion object {
        private val TAG: String = TemplateSettingsFragment::class.java.simpleName
        private const val TAG_FRAGMENT_RESULT = "TemplateSettingsFragmentResult"
        private const val KEY_SETTINGS_MODE = "key_settings_mode"
        private const val KEY_TEMPLATE_ID = "key_template_id"
        const val KEY_SAVED_ID = "key_saved_id"
        const val KEY_SAVED_TITLE = "key_saved_title"
        const val KEY_SAVED_ROOM_TYPE = "key_saved_room_type"

        fun show(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            templateId: String,
            settingsMode: Int,
            onResult: (Bundle) -> Unit
        ) {
            fragmentManager.setFragmentResultListener(
                TAG_FRAGMENT_RESULT,
                lifecycleOwner
            ) { _, bundle -> onResult(bundle) }

            newInstance(templateId, settingsMode).show(fragmentManager, TAG)
        }

        private fun newInstance(templateId: String, settingsMode: Int) =
            TemplateSettingsFragment().putArgs(
                KEY_TEMPLATE_ID to templateId,
                KEY_SETTINGS_MODE to settingsMode
            )
    }
}

