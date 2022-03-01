package app.editors.manager.storages.base.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import app.editors.manager.R
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.storages.base.presenter.BaseStorageDocsPresenter
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.storages.dropbox.ui.fragments.operations.DocsDropboxOperationFragment
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.fragments.main.DocsBaseFragment

abstract class BaseStorageOperationsFragment: DocsBaseFragment(), OperationActivity.OnActionClickListener,
    BaseStorageDocsView {


    var operationActivity: OperationActivity? = null
    var operationType: OperationsState.OperationType? = null

    override val presenter: BaseStorageDocsPresenter<out BaseStorageDocsView>
        get() = getOperationsPresenter()
    override val isWebDav: Boolean?
        get() = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            operationActivity = context as OperationActivity
            operationActivity?.setOnActionClickListener(this)
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsDropboxOperationFragment::class.java.simpleName + " - must implement - " +
                        OperationActivity::class.java.simpleName
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onDocsRefresh(list: List<Entity>?) {
        super.onDocsRefresh(list?.filterIsInstance<CloudFolder>())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onItemClick(view: View, position: Int) {
        super.onItemClick(view, position)
        operationActivity?.setEnabledActionButton(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        operationActivity?.apply {
            setOnActionClickListener(null)
            setEnabledActionButton(false)
        }

    }

    override fun onFileWebView(file: CloudFile) {
        TODO("Not yet implemented")
    }

    override fun onChooseDownloadFolder() {
        TODO("Not yet implemented")
    }

    override fun onError(message: String?) {
        super.onError(message)
        operationActivity?.setEnabledActionButton(false)
    }

    override fun onDocsGet(list: List<Entity>?) {
        super.onDocsGet(list?.filterIsInstance<CloudFolder>())
        operationActivity?.setEnabledActionButton(true)
    }

    override fun onDocsBatchOperation() {
        super.onDocsBatchOperation()
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        setActionBarTitle(getString(R.string.operation_title))
        swipeRefreshLayout?.isRefreshing = true
        presenter.getProvider()
    }

    override fun onUpdateItemFavorites() { }

    override fun onActionClick() {
        when (operationType) {
            OperationsState.OperationType.COPY -> presenter.copy()
            OperationsState.OperationType.MOVE -> presenter.move()
            else -> { }
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        getArgs(savedInstanceState)
        initViews()
        presenter.checkBackStack()
    }

    private fun getArgs(savedInstanceState: Bundle?) {
        val intent = requireActivity().intent
        operationType =
            intent.getSerializableExtra(OperationActivity.TAG_OPERATION_TYPE) as OperationsState.OperationType?
        if (savedInstanceState == null) {
            val explorer =
                intent.getSerializableExtra(OperationActivity.TAG_OPERATION_EXPLORER) as Explorer?

            explorer?.let {
                presenter.setOperationExplorer(it)
            } ?: requireActivity().finish()

        }
    }

    private fun initViews() {
        operationActivity?.setEnabledActionButton(false)
        explorerAdapter?.isFoldersMode = true
        recyclerView?.setPadding(0, 0, 0, 0)
    }

    abstract fun getOperationsPresenter(): BaseStorageDocsPresenter<out BaseStorageDocsView>
}