package app.editors.manager.ui.fragments.operations

import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.mvp.presenters.storages.DocsOneDrivePresenter
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import lib.toolkit.base.managers.utils.putArgs
import moxy.presenter.InjectPresenter

class DocsOneDriveOperationFragment : BaseStorageOperationsFragment() {

    companion object {

        fun newInstance(
            operationType: OperationType,
            explorer: Explorer? = null
        ): DocsOneDriveOperationFragment = DocsOneDriveOperationFragment().putArgs(
            OperationDialogFragment.TAG_OPERATION_TYPE to operationType,
            OperationDialogFragment.TAG_OPERATION_EXPLORER to explorer
        )
    }

    @InjectPresenter
    override lateinit var presenter: DocsOneDrivePresenter

}