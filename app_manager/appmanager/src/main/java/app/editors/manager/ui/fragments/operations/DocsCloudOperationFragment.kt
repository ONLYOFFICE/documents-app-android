package app.editors.manager.ui.fragments.operations

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.contracts.ApiContract.Access
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.activities.main.OperationActivity.OnActionClickListener
import app.editors.manager.ui.dialogs.fragments.AddRoomDialog
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import app.editors.manager.viewModels.main.CopyItems
import lib.toolkit.base.managers.utils.getSerializable
import lib.toolkit.base.managers.utils.putArgs

class DocsCloudOperationFragment : DocsCloudFragment(), OnActionClickListener {

    private var operationActivity: OperationActivity? = null
    private var operationType: OperationType? = null
    private var sectionType = 0
    private var showFolderAfterFinish: Boolean = false

    private val isRoomsRoot: Boolean
        get() = sectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM && presenter.isRoot

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
        super.onDocsGet(list?.filterNotFillFormRooms())
        setEnabledOperationButtons()
        setCreateFolderClickListener()
    }

    override fun onDocsRefresh(list: List<Entity>?) {
        super.onDocsRefresh(list?.filterNotFillFormRooms())
    }

    override fun onDocsNext(list: List<Entity>?) {
        super.onDocsNext(list?.filterNotFillFormRooms())
    }

    private fun setCreateFolderClickListener() {
        if (isRoomsRoot) {
            operationActivity?.setCreateFolderClickListener {
                AddRoomDialog.show(
                    activity = requireActivity(),
                    type = ApiContract.RoomType.FILL_FORMS_ROOM,
                    copyItems = CopyItems(fileIds = listOf())
                ) { bundle ->
                    bundle.getString("id")?.let { folderId ->
                        presenter.setDestFolder(folderId)
                        presenter.openFolder(folderId, 0)
                    }
                }
            }
        } else {
            operationActivity?.setCreateFolderClickListener(null)
        }
    }

    private fun setEnabledOperationButtons() {
        val current = presenter.currentFolder
        if (current != null) {
            val security = current.security
            if (security != null) {
                setEnabledActionButton(security.editAccess || security.editRoom)
                operationActivity?.setEnabledCreateFolderButton(security.create, isRoomsRoot)
            } else {
                val editable = current.access in arrayOf(Access.ReadWrite.type, Access.RoomAdmin.type)
                setEnabledActionButton(editable)
                operationActivity?.setEnabledCreateFolderButton(editable, isRoomsRoot)
            }
        }
    }

    private fun List<Entity>.filterNotFillFormRooms(): List<Entity> {
        if (operationType != OperationType.COPY_TO_FILL_FORM_ROOM) return this
        return filter { item ->
            if (item is CloudFolder) {
                if (item.isRoom) {
                    item.roomType == ApiContract.RoomType.FILL_FORMS_ROOM
                } else {
                    true
                }
            } else {
                true
            }
        }
    }

    override fun onDocsBatchOperation() {
        super.onDocsBatchOperation()
        if (showFolderAfterFinish) {
            requireActivity().setResult(
                RESULT_OPEN_FOLDER,
                Intent().putExtra(RESULT_KEY_OPEN_FOLDER, presenter.destFolderId)
            )
        } else {
            requireActivity().setResult(Activity.RESULT_OK)
        }
        requireActivity().finish()
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        if (presenter.pickerMode == PickerMode.Folders &&
            operationType != OperationType.COPY_TO_FILL_FORM_ROOM
        ) {
            setActionBarTitle(getString(R.string.operation_title))
        }
        getDocs()
    }

    override fun onActionClick() {
        when (operationType) {
            OperationType.COPY_TO_FILL_FORM_ROOM,
            OperationType.COPY -> cloudPresenter.copy()
            OperationType.MOVE -> cloudPresenter.tryMove()
            OperationType.RESTORE -> cloudPresenter.tryMove()
            OperationType.PICK_PDF_FORM -> cloudPresenter.copyFilesToCurrent()
            else -> {}
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        getArgs(savedInstanceState)
        initViews()
        cloudPresenter.checkBackStack()
        showFolderAfterFinish = operationType == OperationType.COPY_TO_FILL_FORM_ROOM
    }

    private fun getArgs(savedInstanceState: Bundle?) {
        arguments?.let {
            sectionType = it.getInt(TAG_OPERATION_SECTION_TYPE)
            operationType = requireActivity().intent
                .getSerializable(OperationActivity.TAG_OPERATION_TYPE, OperationType::class.java)
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
        if (operationType == OperationType.PICK_PDF_FORM) {
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