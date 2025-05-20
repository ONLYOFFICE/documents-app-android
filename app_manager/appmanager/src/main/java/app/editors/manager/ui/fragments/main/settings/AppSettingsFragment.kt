package app.editors.manager.ui.fragments.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.viewModels.main.AppSettingsEffect
import app.editors.manager.viewModels.main.AppSettingsViewModel
import app.editors.manager.viewModels.main.AppSettingsViewModelFactory
import app.editors.manager.viewModels.main.PasscodeViewModel
import app.editors.manager.viewModels.main.PasscodeViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.FontPicker
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.suspendLaunchAfterResume
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.fragments.base.BaseFragment
import javax.inject.Inject
import kotlin.properties.Delegates

class AppSettingsFragment : BaseFragment() {

    companion object {
        val TAG: String = AppSettingsFragment::class.java.simpleName
        private const val DIALOG_CANCEL_TAG = "DIALOG_CANCEL_TAG"

        fun newInstance(): AppSettingsFragment = AppSettingsFragment()
    }

    @Inject
    lateinit var preferenceTool: PreferenceTool

    private var addMenuItem: MenuItem? = null
    private var clearMenuItem: MenuItem? = null

    private var navController: NavHostController by Delegates.notNull()

    @Inject
    lateinit var injectFactory: AppSettingsViewModelFactory

    private val viewModel by viewModels<AppSettingsViewModel> { injectFactory }

    private val passcodeViewModel by viewModels<PasscodeViewModel> {
        PasscodeViewModelFactory(preferenceTool = preferenceTool)
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_settings_fonts, menu)
        clearMenuItem = menu.findItem(R.id.clear_fonts)
        addMenuItem = menu.findItem(R.id.add_font)
        clearMenuItem?.let {
            UiUtils.setMenuItemTint(
                requireContext(),
                it,
                lib.toolkit.base.R.color.colorPrimary
            )
        }
        addMenuItem?.let {
            UiUtils.setMenuItemTint(
                requireContext(),
                it,
                lib.toolkit.base.R.color.colorPrimary
            )
        }

        suspendLaunchAfterResume {
            navController.currentBackStackEntryFlow.collect { entry ->
                if (entry.destination.route == AppSettingsScreen.Fonts.route) {
                    clearMenuItem?.isVisible = viewModel.settingsState.value.fonts.isNotEmpty()
                    addMenuItem?.isVisible = true
                } else {
                    clearMenuItem?.isVisible = false
                    addMenuItem?.isVisible = false
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear_fonts -> {
                UiUtils.showQuestionDialog(
                    context = requireContext(),
                    title = getString(R.string.dialogs_question_delete_all_fonts),
                    description = resources.getQuantityString(
                        R.plurals.dialogs_question_message_delete,
                        2
                    ),
                    acceptListener = viewModel::clearFonts
                )
            }

            R.id.add_font -> {
                FontPicker(
                    activityResultRegistry = requireActivity().activityResultRegistry,
                    callback = viewModel::addFont
                ).pickFonts()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext())
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        when (tag) {
            DIALOG_CANCEL_TAG -> {
                viewModel.cancelJob()
                hideDialog()
            }

            else -> super.onCancelClick(dialogs, tag)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setContent {
            ManagerTheme {
                navController = rememberNavController()

                LaunchedEffect(Unit) {
                    viewModel.effect.collect {
                        when (it) {
                            is AppSettingsEffect.Error -> showSnackBar(it.message)
                            is AppSettingsEffect.Progress -> updateProgressDialog(100, it.value)
                            AppSettingsEffect.HideDialog -> hideDialog()
                            AppSettingsEffect.ShowDialog -> {
                                showProgressDialog(
                                    title = getString(R.string.dialogs_wait_title),
                                    isHideButton = false,
                                    cancelTitle = getString(R.string.dialogs_common_cancel_button),
                                    tag = DIALOG_CANCEL_TAG
                                )
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.message.collectLatest { message ->
                        showSnackBar(message)
                    }
                }

                LaunchedEffect(Unit) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        initToolbar(destination.route)
                    }
                }

                AppSettingsScreenHost(
                    viewModel = viewModel,
                    passcodeViewModel = passcodeViewModel,
                    navController = navController,
                    onShowClearMenuItem = { show -> clearMenuItem?.isVisible = show }
                )
            }
        }
    }

    private fun initToolbar(screen: String?) {
        (activity as? IMainActivity)?.apply {
            val title = when (screen) {
                AppSettingsScreen.Theme.route -> R.string.app_settings_color_theme
                AppSettingsScreen.Passcode.route -> R.string.app_settings_passcode
                AppSettingsScreen.LocalePicker.route -> R.string.settings_language
                AppSettingsScreen.About.route -> R.string.about_title
                AppSettingsScreen.Fonts.route -> lib.toolkit.base.R.string.settings_fonts_title
                else -> R.string.settings_item_title
            }

            setAppBarStates(false)
            showNavigationButton(screen != AppSettingsScreen.Main.route)
            showActionButton(false)
            setActionBarTitle(getString(title))
        }
    }
}
