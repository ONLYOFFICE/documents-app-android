package app.editors.manager.ui.fragments.operations

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.activities.main.OperationActivity.OnActionClickListener
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import lib.toolkit.base.managers.utils.getSerializable
import lib.toolkit.base.managers.utils.putArgs

class DocsCloudOperationFragment : DocsCloudFragment(), OnActionClickListener {

    private var operationActivity: OperationActivity? = null
    private var operationType: OperationsState.OperationType? = null
    private var sectionType = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            operationActivity = context as OperationActivity
            operationActivity?.setOnActionClickListener(this)
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsCloudOperationFragment::class.java.simpleName + " - must implement - " +
                        OperationActivity::class.java.simpleName
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onItemClick(view: View, position: Int) {
        super.onItemClick(view, position)
        setEnabledActionButton(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        operationActivity?.setOnActionClickListener(null)
        setEnabledActionButton(false)
    }


    override fun onItemLongClick(view: View, position: Int) {
        // Not actions
    }

    override fun onError(message: String?) {
        super.onError(message)
        setEnabledActionButton(false)
    }

    override fun onDocsGet(list: List<Entity>?) {
        super.onDocsGet(list)
        if (sectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM && presenter.isRoot) {
            setEnabledActionButton(false)
        } else {
            setEnabledActionButton(true)
        }
    }

    override fun onDocsBatchOperation() {
        super.onDocsBatchOperation()
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        if (presenter.pickerMode == PickerMode.Folders) {
            setActionBarTitle(getString(R.string.operation_title))
        }
        getDocs()
    }

    override fun onActionClick() {
        when (operationType) {
            OperationsState.OperationType.COPY -> cloudPresenter.copy()
            OperationsState.OperationType.MOVE -> cloudPresenter.tryMove()
            OperationsState.OperationType.RESTORE -> cloudPresenter.tryMove()
            OperationsState.OperationType.PICK_PDF_FORM -> cloudPresenter.copyFilesToCurrent()
            else -> {}
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        getArgs(savedInstanceState)
        initViews()
        cloudPresenter.checkBackStack()
    }

    private fun getArgs(savedInstanceState: Bundle?) {
        arguments?.let {
            sectionType = it.getInt(TAG_OPERATION_SECTION_TYPE)
            operationType = requireActivity().intent
                .getSerializable(OperationActivity.TAG_OPERATION_TYPE, OperationsState.OperationType::class.java)
            if (savedInstanceState == null) {
                requireActivity().intent
                    .getSerializable(OperationActivity.TAG_OPERATION_EXPLORER, Explorer::class.java).let { explorer ->
                        cloudPresenter.setOperationExplorer(explorer)
                    }
            }
        }
    }

    private fun initViews() {
        setPickerMode()
        setEnabledActionButton(false)
        recyclerView?.setPadding(0, 0, 0, 0)
    }

    private fun getDocs() {
        setPickerMode()
        when (sectionType) {
            ApiContract.SectionType.CLOUD_USER -> cloudPresenter.getItemsById(ApiContract.SectionPath.MY)
            ApiContract.SectionType.CLOUD_SHARE -> cloudPresenter.getItemsById(ApiContract.SectionPath.SHARED)
            ApiContract.SectionType.CLOUD_COMMON -> cloudPresenter.getItemsById(ApiContract.SectionPath.COMMON)
            ApiContract.SectionType.CLOUD_PROJECTS -> cloudPresenter.getItemsById(ApiContract.SectionPath.PROJECTS)
            ApiContract.SectionType.CLOUD_VIRTUAL_ROOM -> cloudPresenter.getItemsById(ApiContract.SectionPath.ROOMS)
        }
    }

    private fun setPickerMode() {
        if (operationType == OperationsState.OperationType.PICK_PDF_FORM) {
            cloudPresenter.isSelectionMode = true
            cloudPresenter.pickerMode =
                PickerMode.Files.PDFForm(destFolderId = arguments?.getString(TAG_DEST_FOLDER_ID).orEmpty())
        } else {
            cloudPresenter.pickerMode = PickerMode.Folders
        }
        explorerAdapter?.pickerMode = cloudPresenter.pickerMode
    }
    
    private fun setEnabledActionButton(enabled: Boolean) {
        if (presenter.pickerMode is PickerMode.Files) {
            val mode = presenter.pickerMode as PickerMode.Files
            operationActivity?.setEnabledActionButton(mode.selectedIds.isNotEmpty())
        } else {
            operationActivity?.setEnabledActionButton(enabled)
        }
    }

    companion object {
        val TAG: String = DocsCloudOperationFragment::class.java.simpleName
        private const val TAG_OPERATION_SECTION_TYPE = "section"
        private const val TAG_DEST_FOLDER_ID = "dest_folder_id"

        fun newInstance(sectionType: Int, destFolderId: String): DocsCloudOperationFragment =
            DocsCloudOperationFragment().putArgs(
                TAG_OPERATION_SECTION_TYPE to sectionType,
                TAG_DEST_FOLDER_ID to destFolderId
            )
    }
}