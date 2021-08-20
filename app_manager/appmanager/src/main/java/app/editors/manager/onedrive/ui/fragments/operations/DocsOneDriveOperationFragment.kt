package app.editors.manager.onedrive.ui.fragments.operations

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.onedrive.mvp.presenters.DocsOneDrivePresenter
import app.editors.manager.onedrive.mvp.views.DocsOneDriveView
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.fragments.main.*
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import moxy.presenter.InjectPresenter
import java.lang.ClassCastException
import java.lang.RuntimeException

class DocsOneDriveOperationFragment: DocsBaseFragment(), OperationActivity.OnActionClickListener, DocsOneDriveView {


    companion object {

        val TAG = DocsCloudOperationFragment::class.java.simpleName

        fun newInstance(): DocsOneDriveOperationFragment {
            return DocsOneDriveOperationFragment()
        }
    }


    private var mOperationActivity: OperationActivity? = null
    private var mOperationType: OperationsState.OperationType? = null

    @InjectPresenter
    lateinit var presenter: DocsOneDrivePresenter

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mOperationActivity = context as OperationActivity
            mOperationActivity!!.setOnActionClickListener(this)
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

    override fun onDocsRefresh(list: MutableList<Entity>?) {
        super.onDocsRefresh(list?.filter { it is CloudFolder })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onItemClick(view: View?, position: Int) {
        super.onItemClick(view, position)
        mOperationActivity?.setEnabledActionButton(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mOperationActivity!!.setOnActionClickListener(null)
        mOperationActivity!!.setEnabledActionButton(false)
    }

    override fun onItemLongClick(view: View?, position: Int) {
        // Not actions
    }

    override fun isWebDav(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPresenter(): DocsBasePresenter<out DocsBaseView> {
        return presenter
    }

    override fun onError(message: String?) {
        super.onError(message)
        mOperationActivity?.setEnabledActionButton(false)
    }

    override fun onDocsGet(list: List<Entity?>?) {
        super.onDocsGet(list?.filter { it is CloudFolder})
        mOperationActivity?.setEnabledActionButton(true)
    }

    override fun onDocsBatchOperation() {
        super.onDocsBatchOperation()
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        setActionBarTitle(getString(R.string.operation_title))
        mSwipeRefresh.isRefreshing = true
        presenter.getProvider()
    }

    override fun onRemoveItemFromFavorites() {
        //stub
    }

    override fun onActionClick() {
        when (mOperationType) {
            OperationsState.OperationType.COPY -> presenter.copy()
            OperationsState.OperationType.MOVE -> presenter.move()
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
        }
        getArgs(savedInstanceState)
        initViews()
        presenter.checkBackStack()
    }

    private fun getArgs(savedInstanceState: Bundle?) {
        val intent = requireActivity().intent
        mOperationType =
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
        mOperationActivity?.setEnabledActionButton(false)
        mExplorerAdapter.isFoldersMode = true
        mRecyclerView.setPadding(0, 0, 0, 0)
    }
}