package app.editors.manager.ui.fragments.operations

import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.storages.BaseStorageDocsPresenter
import app.editors.manager.mvp.views.base.BaseStorageDocsView
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import lib.toolkit.base.managers.utils.getSerializableExt

abstract class BaseStorageOperationsFragment : DocsCloudOperationFragment(),
    OperationDialogFragment.OnActionClickListener,
    BaseStorageDocsView {

    override fun onDocsGet(list: List<Entity>?) {
        super.onDocsGet(list)
        setEnabledOperationButtons()
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        setActionBarTitle(getString(R.string.operation_title))
        (presenter as BaseStorageDocsPresenter<*>).getProvider()
        arguments?.getSerializableExt<Explorer>(OperationDialogFragment.TAG_OPERATION_EXPLORER)?.let { explorer ->
            presenter.setOperationExplorer(explorer)
        }
    }

    override fun setEnabledOperationButtons() {
        operationDialogFragment?.setEnabledCreateFolderButton(isEnabled = true, isRoom = false)
        operationDialogFragment?.setEnabledActionButton(isEnabled = true)
    }

    override fun onActionClick() {
        when (operationType) {
            OperationsState.OperationType.COPY -> presenter.copy()
            OperationsState.OperationType.MOVE -> presenter.move()
            else -> Unit
        }
    }
}