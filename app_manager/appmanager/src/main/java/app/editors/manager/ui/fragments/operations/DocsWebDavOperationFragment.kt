package app.editors.manager.ui.fragments.operations

import android.view.View
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.mvp.presenters.main.DocsWebDavPresenter
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.mvp.views.main.DocsWebDavView
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs
import moxy.presenter.InjectPresenter

class DocsWebDavOperationFragment : DocsCloudOperationFragment(), DocsWebDavView {

    @InjectPresenter
    lateinit var webdavPresenter: DocsWebDavPresenter

    companion object {

        fun newInstance(
            operationType: OperationType,
            explorer: Explorer? = null
        ): DocsWebDavOperationFragment = DocsWebDavOperationFragment().putArgs(
            OperationDialogFragment.TAG_OPERATION_TYPE to operationType,
            OperationDialogFragment.TAG_OPERATION_EXPLORER to explorer
        )
    }

    override fun onStateEmptyBackStack() {
        webdavPresenter.pickerMode = PickerMode.Folders
        explorerAdapter?.pickerMode = PickerMode.Folders
        webdavPresenter.getItems()
        arguments?.getSerializableExt<Explorer>(OperationDialogFragment.TAG_OPERATION_EXPLORER)?.let { explorer ->
            webdavPresenter.setOperationExplorer(explorer)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        val item = explorerAdapter?.getItem(position) as Item
        if (item is CloudFile && webdavPresenter.pickerMode == PickerMode.Folders) return

        webdavPresenter.onItemClick(item, position)
    }

    override fun setEnabledOperationButtons() {
        operationDialogFragment?.setEnabledCreateFolderButton(isEnabled = true, isRoom = false)
        operationDialogFragment?.setEnabledActionButton(isEnabled = true)
    }

    override fun onBackClick(): Boolean {
        return webdavPresenter.getBackStack()
    }

    override fun onActionClick() {
        when (operationType) {
            OperationType.COPY -> webdavPresenter.copy()
            else -> super.onActionClick()
        }
    }

    override fun onActionDialog() {}
}