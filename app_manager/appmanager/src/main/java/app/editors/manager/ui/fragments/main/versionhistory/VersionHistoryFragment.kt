package app.editors.manager.ui.fragments.main.versionhistory

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.viewModels.main.VersionHistoryViewModel
import app.editors.manager.viewModels.main.VersionViewer
import kotlinx.serialization.Serializable
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.putArgs
import lib.toolkit.base.ui.activities.base.BaseActivity

interface RefreshListener{
    fun refresh()
}

class VersionHistoryFragment : ComposeDialogFragment(), RefreshListener {

    private var viewer: VersionViewer? = null
    private var refreshCallback: (() -> Unit)? = null

    sealed interface Screens {
        @Serializable
        data class Main(val fileId: String) : Screens
        @Serializable
        data class EditComment(
            val fileId: String,
            val version: Int,
            val comment: String
        ) : Screens
    }

    companion object {
        private val TAG: String = VersionHistoryFragment::class.java.simpleName
        private const val TAG_FRAGMENT_RESULT = "VersionHistoryFragment Result"
        private const val KEY_DOC_ID = "KEY_DOC_ID"

         fun newInstance(docId: String, versionViewer: VersionViewer): VersionHistoryFragment {
            return VersionHistoryFragment().apply { this.viewer = versionViewer }.putArgs(KEY_DOC_ID to docId)
        }

        fun show(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            docId: String,
            versionViewer: VersionViewer,
            onResult: () -> Unit
        ): RefreshListener {
            fragmentManager
                .setFragmentResultListener(
                    TAG_FRAGMENT_RESULT,
                    lifecycleOwner
                ){ _, _ -> onResult()}

            return newInstance(docId, versionViewer).apply {
                show(fragmentManager, TAG)
            }
        }
    }

    override fun refresh(){
        refreshCallback?.invoke()
    }

    override fun dismiss() {
        refreshParentFragment()
        super.dismiss()
    }

    private fun refreshParentFragment() {
        parentFragmentManager.setFragmentResult(
            TAG_FRAGMENT_RESULT,
            Bundle.EMPTY
        )
    }

    @Composable
    override fun Content() {
        val fileId = remember { arguments?.getString(KEY_DOC_ID) }.orEmpty()
        val navController = rememberNavController()
        val viewModel = viewModel<VersionHistoryViewModel>(
            factory = VersionHistoryViewModel.Factory(fileId, viewer)
        )
        refreshCallback = { viewModel.onRefresh(2000) }

        ManagerTheme {
            NavHost(
                navController = navController,
                startDestination = Screens.Main(fileId),
                modifier = Modifier.fillMaxSize()
            ) {
                composable<Screens.Main>{
                    VersionHistoryScreen(
                        viewModel = viewModel,
                        showDownloadFolderActivity = { uri ->
                            ActivitiesUtils.showDownloadViewer(
                                this@VersionHistoryFragment,
                                BaseActivity.REQUEST_ACTIVITY_DOWNLOAD_VIEWER,
                                uri
                            ) },
                        goToEditComment = { file ->
                            navController.navigate(
                                Screens.EditComment(file.fileId, file.version, file.comment)
                            ) },
                        onBack = ::dismiss
                    )
                }

                composable<Screens.EditComment>{
                    EditCommentScreen(
                        onSuccess = viewModel::onRefresh,
                        onBack = navController::popBackStackWhenResumed
                    )
                }
            }
        }
    }
}
