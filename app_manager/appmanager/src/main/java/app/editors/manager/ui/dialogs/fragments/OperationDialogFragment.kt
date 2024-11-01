package app.editors.manager.ui.dialogs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.FragmentDialogOperationBinding
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import app.editors.manager.ui.fragments.operations.DocsDropboxOperationFragment
import app.editors.manager.ui.fragments.operations.DocsGoogleDriveOperationFragment
import app.editors.manager.ui.fragments.operations.DocsOneDriveOperationFragment
import app.editors.manager.ui.fragments.operations.DocsOperationSectionFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs
import lib.toolkit.base.ui.activities.base.BaseActivity
import javax.inject.Inject

class OperationDialogFragment : BaseDialogFragment() {

    interface OnActionClickListener {
        fun onActionClick()
        fun onBackClick(): Boolean
    }

    companion object {
        val TAG: String = OperationDialogFragment::class.java.simpleName

        const val TAG_OPERATION_TYPE = "tag_operation_type"
        const val TAG_SECTION_TYPE = "tag_section_type"
        const val TAG_OPERATION_EXPLORER = "tag_operation_explorer"
        const val TAG_DEST_FOLDER_ID = "dest_folder_id"
        const val KEY_OPERATION_REQUEST = "key_operation_request"
        const val KEY_OPERATION_RESULT_OPEN_FOLDER = "key_operation_result_open_folder"
        const val KEY_OPERATION_RESULT_COMPLETE = "key_operation_result_complete"

        private fun newInstance(
            operation: OperationType,
            explorer: Explorer
        ): OperationDialogFragment {
            return OperationDialogFragment().putArgs(
                TAG_OPERATION_TYPE to operation,
                TAG_OPERATION_EXPLORER to explorer
            )
        }

        private fun newInstance(
            destFolderId: String,
            explorer: Explorer
        ): OperationDialogFragment {
            return OperationDialogFragment().putArgs(
                TAG_OPERATION_TYPE to OperationType.PICK_PDF_FORM,
                TAG_OPERATION_EXPLORER to explorer,
                TAG_DEST_FOLDER_ID to destFolderId
            )
        }

        private fun setFragmentResultListener(
            activity: FragmentActivity,
            onResult: (Bundle) -> Unit
        ) {
            activity.supportFragmentManager.setFragmentResultListener(
                KEY_OPERATION_REQUEST,
                activity
            ) { _, bundle -> onResult(bundle) }
        }

        fun show(
            activity: FragmentActivity,
            operation: OperationType,
            explorer: Explorer,
            onResult: (Bundle) -> Unit
        ) {
            setFragmentResultListener(activity, onResult)
            newInstance(operation, explorer).show(activity.supportFragmentManager, null)
        }

        fun show(
            activity: FragmentActivity,
            destFolderId: String,
            explorer: Explorer,
            onResult: (Bundle) -> Unit
        ) {
            setFragmentResultListener(activity, onResult)
            newInstance(destFolderId, explorer).show(activity.supportFragmentManager, null)
        }
    }

    @Inject
    lateinit var cloudDataSource: CloudDataSource

    private var actionClickListener: OnActionClickListener? = null
    private var binding: FragmentDialogOperationBinding? = null

    private val operationType: OperationType by lazy {
        requireNotNull(arguments?.getSerializableExt(TAG_OPERATION_TYPE))
    }

    private val destFolderId: String? by lazy { arguments?.getString(TAG_DEST_FOLDER_ID) }

    private val explorer: Explorer? by lazy { arguments?.getSerializableExt(TAG_OPERATION_EXPLORER) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UiUtils.isTablet(requireContext())) {
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialog
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogOperationBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.getApp().appComponent.inject(this)
        init(savedInstanceState)
        initListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun setToolbarTitle(title: String) {
        binding?.appBarToolbar?.title = title
    }

    override fun onBackPressed(): Boolean {
        return if (actionClickListener?.onBackClick() != true) {
            super.onBackPressed()
        } else {
            false
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        initActionButton(operationType)
        if (savedInstanceState == null) initState()
        binding?.appBarToolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initState() {
        lifecycleScope.launch {
            context?.accountOnline?.let { account ->
                withContext(Dispatchers.Main) {
                    val fragment = if (operationType == OperationType.COPY_TO_FILL_FORM_ROOM) {
                        DocsCloudOperationFragment.newInstance(
                            sectionType = ApiContract.SectionType.CLOUD_VIRTUAL_ROOM,
                            operationType = OperationType.COPY_TO_FILL_FORM_ROOM,
                            explorer = explorer
                        )
                    } else {
                        when (account.portal.provider) {
                            PortalProvider.Dropbox -> {
                                DocsDropboxOperationFragment.newInstance(
                                    operationType = operationType,
                                    explorer = explorer
                                )
                            }
                            PortalProvider.GoogleDrive -> {
                                DocsGoogleDriveOperationFragment.newInstance(
                                    operationType = operationType,
                                    explorer = explorer
                                )
                            }
                            PortalProvider.Onedrive -> {
                                DocsOneDriveOperationFragment.newInstance(
                                    operationType = operationType,
                                    explorer = explorer
                                )
                            }
                            is PortalProvider.Webdav -> {
                                DocsCloudOperationFragment.newInstance(
                                    sectionType = ApiContract.SectionType.CLOUD_VIRTUAL_ROOM,
                                    operationType = operationType,
                                    explorer = explorer
                                )
                            }
                            is PortalProvider.Cloud.Personal -> {
                                DocsCloudOperationFragment.newInstance(
                                    sectionType = ApiContract.SectionType.CLOUD_USER,
                                    operationType = OperationType.COPY_TO_FILL_FORM_ROOM,
                                    explorer = explorer
                                )
                            }
                            else -> {
                                DocsOperationSectionFragment.newInstance(
                                    destFolderId = destFolderId.orEmpty(),
                                    operationType = operationType,
                                    explorer = explorer
                                )
                            }
                        }
                    }
                    showFragment(fragment)
                }
            }
        }
    }

    private fun initListeners() {
        binding?.operationPanel?.operationActionButton?.setOnClickListener {
            actionClickListener?.onActionClick()
        }
        binding?.operationPanel?.operationCancelButton?.setOnClickListener { dismiss() }
    }

    private fun initActionButton(actionOperationType: OperationType?) {
        when (actionOperationType) {
            OperationType.COPY_TO_FILL_FORM_ROOM,
            OperationType.COPY -> binding?.operationPanel?.operationActionButton?.setText(R.string.operation_panel_copy_button)
            OperationType.MOVE -> binding?.operationPanel?.operationActionButton?.setText(R.string.operation_panel_move_button)
            OperationType.RESTORE -> binding?.operationPanel?.operationActionButton?.setText(R.string.operation_panel_restore_button)
            OperationType.PICK_PDF_FORM -> {
                binding?.operationPanel?.operationActionButton?.setText(lib.toolkit.base.R.string.common_ok)
                binding?.operationPanel?.operationCreateFolderButton?.isVisible = false
            }
            else -> Unit
        }
    }

    fun setEnabledActionButton(isEnabled: Boolean) {
        binding?.operationPanel?.operationActionButton?.isEnabled = isEnabled
    }

    fun setEnabledCreateFolderButton(isEnabled: Boolean, isRoom: Boolean) {
        binding?.operationPanel?.operationCreateFolderButton?.let { button ->
            button.isEnabled = isEnabled
            if (isRoom) {
                button.setText(R.string.dialog_create_room)
            } else {
                button.setText(R.string.dialogs_edit_create_folder)
            }
        }
    }

    fun setCreateFolderClickListener(onClick: (() -> Unit)? = null) {
        binding?.operationPanel?.operationCreateFolderButton?.setOnClickListener {
            onClick?.invoke() ?: (requireActivity() as? BaseActivity)?.showEditDialog(
                title = getString(R.string.dialogs_edit_create_folder),
                bottomTitle = null,
                value = getString(R.string.dialogs_edit_create_folder),
                editHint = getString(R.string.dialogs_edit_hint),
                acceptTitle = getString(R.string.dialogs_edit_accept_create),
                cancelTitle = getString(R.string.dialogs_common_cancel_button),
                error = null,
                tag = DocsBasePresenter.TAG_DIALOG_ACTION_FOLDER,
            )
        }
    }

    fun setCreateFolderVisible(visible: Boolean) {
        binding?.operationPanel?.operationCreateFolderButton?.isVisible = visible
    }

    fun setOnActionClickListener(onActionClickListener: OnActionClickListener?) {
        actionClickListener = onActionClickListener
    }

}