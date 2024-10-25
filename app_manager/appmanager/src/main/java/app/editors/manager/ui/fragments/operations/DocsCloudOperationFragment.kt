package app.editors.manager.ui.fragments.operations

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.contracts.ApiContract.Access
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import app.editors.manager.ui.fragments.main.AddRoomFragment
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import app.editors.manager.viewModels.main.CopyItems
import lib.toolkit.base.managers.utils.getIntExt
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs

open class DocsCloudOperationFragment : DocsCloudFragment(),
    OperationDialogFragment.OnActionClickListener {

    companion object {

        val TAG: String = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(
            sectionType: Int,
            destFolderId: String = "",
            operationType: OperationType,
            explorer: Explorer? = null
        ): DocsCloudOperationFragment = DocsCloudOperationFragment().putArgs(
            OperationDialogFragment.TAG_OPERATION_TYPE to operationType,
            OperationDialogFragment.TAG_SECTION_TYPE to sectionType,
            OperationDialogFragment.TAG_DEST_FOLDER_ID to destFolderId,
            OperationDialogFragment.TAG_OPERATION_EXPLORER to explorer
        )
    }

    protected val operationDialogFragment by lazy { parentFragment as? OperationDialogFragment }

    protected val operationType: OperationType? by lazy {
        arguments?.getSerializableExt(OperationDialogFragment.TAG_OPERATION_TYPE)
    }

    protected val destFolderId: String by lazy {
        arguments?.getString(OperationDialogFragment.TAG_DEST_FOLDER_ID).orEmpty()
    }

    protected val explorer: Explorer? by lazy {
        arguments?.getSerializableExt(OperationDialogFragment.TAG_OPERATION_EXPLORER)
    }

    private val sectionType: Int by lazy { arguments?.getIntExt(OperationDialogFragment.TAG_SECTION_TYPE) ?: 0 }

    private val showFolderAfterFinish: Boolean
        get() = operationType == OperationType.COPY_TO_FILL_FORM_ROOM

    private val isRoomsRoot: Boolean
        get() = sectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM && presenter.isRoot

    override fun onAttach(context: Context) {
        super.onAttach(context)
        operationDialogFragment?.setOnActionClickListener(this)
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
        operationDialogFragment?.setOnActionClickListener(null)
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
        setEnabledOperationButtons()
        setCreateFolderClickListener()
    }

    private fun setCreateFolderClickListener() {
        if (isRoomsRoot) {
            operationDialogFragment?.setCreateFolderClickListener {
                AddRoomFragment.show(
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
            operationDialogFragment?.setCreateFolderClickListener(null)
        }
    }

    open fun setEnabledOperationButtons() {
        val current = presenter.currentFolder
        if (current != null) {
            val security = current.security
            if (security != null) {
                setEnabledActionButton(security.editAccess || security.editRoom)
                operationDialogFragment?.setEnabledCreateFolderButton(security.create, isRoomsRoot)
            } else {
                val editable = current.access in arrayOf(Access.ReadWrite.type, Access.RoomAdmin.type)
                setEnabledActionButton(editable)
                operationDialogFragment?.setEnabledCreateFolderButton(editable, isRoomsRoot)
            }
        }
        operationDialogFragment?.setCreateFolderVisible(true)
    }

    override fun onDocsBatchOperation() {
        super.onDocsBatchOperation()
        requireActivity().supportFragmentManager.setFragmentResult(
            OperationDialogFragment.KEY_OPERATION_REQUEST,
            if (showFolderAfterFinish) {
                bundleOf(OperationDialogFragment.KEY_OPERATION_RESULT_OPEN_FOLDER to presenter.destFolderId)
            } else {
                bundleOf(OperationDialogFragment.KEY_OPERATION_RESULT_COMPLETE to true)
            }
        )
        operationDialogFragment?.dismiss()
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
            OperationType.COPY -> presenter.copy()
            OperationType.MOVE -> cloudPresenter.tryMove()
            OperationType.RESTORE -> cloudPresenter.tryMove()
            OperationType.PICK_PDF_FORM -> cloudPresenter.copyFilesToCurrent()
            else -> {}
        }
    }

    override fun setActionBarTitle(title: String?) {
        operationDialogFragment?.setToolbarTitle(title.orEmpty())
    }

    override fun onBackClick(): Boolean {
        return presenter.getBackStack()
    }

    private fun init(savedInstanceState: Bundle?) {
        initViews()
        if (savedInstanceState == null) explorer?.let(presenter::setOperationExplorer)
        presenter.checkBackStack()

        if (operationType == OperationType.COPY_TO_FILL_FORM_ROOM) {
            cloudPresenter.setFilterByRoom(ApiContract.RoomType.FILL_FORMS_ROOM)
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
            ApiContract.SectionType.CLOUD_USER -> presenter.getItemsById(ApiContract.SectionPath.MY)
            ApiContract.SectionType.CLOUD_SHARE -> presenter.getItemsById(ApiContract.SectionPath.SHARED)
            ApiContract.SectionType.CLOUD_COMMON -> presenter.getItemsById(ApiContract.SectionPath.COMMON)
            ApiContract.SectionType.CLOUD_PROJECTS -> presenter.getItemsById(ApiContract.SectionPath.PROJECTS)
            ApiContract.SectionType.CLOUD_VIRTUAL_ROOM -> presenter.getItemsById(ApiContract.SectionPath.ROOMS)
        }
    }

    private fun setPickerMode() {
        if (operationType == OperationType.PICK_PDF_FORM) {
            presenter.isSelectionMode = true
            presenter.pickerMode = PickerMode.Files
                .PDFForm(arguments?.getString(OperationDialogFragment.TAG_DEST_FOLDER_ID).orEmpty())
        } else {
            presenter.pickerMode = PickerMode.Folders
        }
        explorerAdapter?.pickerMode = presenter.pickerMode
    }

    private fun setEnabledActionButton(enabled: Boolean) {
        if (presenter.pickerMode is PickerMode.Files) {
            val mode = presenter.pickerMode as PickerMode.Files
            operationDialogFragment?.setEnabledActionButton(mode.selectedIds.isNotEmpty())
        } else {
            operationDialogFragment?.setEnabledActionButton(enabled)
        }
    }
}