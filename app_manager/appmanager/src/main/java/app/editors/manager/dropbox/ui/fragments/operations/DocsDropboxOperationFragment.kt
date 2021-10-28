package app.editors.manager.dropbox.ui.fragments.operations

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.dropbox.mvp.presenters.DocsDropboxPresenter
import app.editors.manager.dropbox.mvp.views.DocsDropboxView
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.onedrive.ui.fragments.operations.DocsOneDriveOperationFragment
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import moxy.presenter.InjectPresenter

class DocsDropboxOperationFragment : DocsBaseFragment(), OperationActivity.OnActionClickListener,
    DocsDropboxView {

    companion object {

        val TAG = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsDropboxOperationFragment = DocsDropboxOperationFragment()
    }


    private var operationActivity: OperationActivity? = null
    private var operationType: OperationsState.OperationType? = null

    @InjectPresenter
    override lateinit var presenter: DocsDropboxPresenter

    override val isWebDav: Boolean
    get() = false

    init {
        App.getApp().appComponent.inject(this)
    }

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

    override fun onRemoveItemFromFavorites() {
        //stub
    }

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
            if (explorer != null) {
                presenter.setOperationExplorer(explorer)
            } else {
                requireActivity().finish()
            }
        }
    }

    private fun initViews() {
        operationActivity?.setEnabledActionButton(false)
        explorerAdapter?.isFoldersMode = true
        recyclerView?.setPadding(0, 0, 0, 0)
    }
}