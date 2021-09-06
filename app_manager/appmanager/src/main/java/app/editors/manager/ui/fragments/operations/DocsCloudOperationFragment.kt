package app.editors.manager.ui.fragments.operations

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.activities.main.OperationActivity.OnActionClickListener
import app.editors.manager.ui.fragments.main.*

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
        operationActivity?.setEnabledActionButton(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        operationActivity?.setOnActionClickListener(null)
        operationActivity?.setEnabledActionButton(false)
    }


    override fun onItemLongClick(view: View, position: Int) {
        // Not actions
    }

    override fun onError(message: String?) {
        super.onError(message)
        operationActivity?.setEnabledActionButton(false)
    }

    override fun onDocsGet(list: List<Entity>?) {
        super.onDocsGet(list)
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
        getDocs()
    }

    override fun onRemoveItemFromFavorites() {
        //stub
    }

    override fun onActionClick() {
        when (operationType) {
            OperationsState.OperationType.COPY -> cloudPresenter.copy()
            OperationsState.OperationType.MOVE -> cloudPresenter.move()
            else -> { }
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
                    .getSerializableExtra(OperationActivity.TAG_OPERATION_TYPE) as
                    OperationsState.OperationType
            savedInstanceState ?: {
                requireActivity().intent
                    .getSerializableExtra(OperationActivity.TAG_OPERATION_EXPLORER)?.let { explorer ->
                        cloudPresenter.setOperationExplorer(explorer as Explorer)
                    } ?: run { requireActivity().finish() }
            }
        }
    }

    private fun initViews() {
        operationActivity?.setEnabledActionButton(false)
        explorerAdapter?.isFoldersMode = true
        recyclerView?.setPadding(0, 0, 0, 0)
    }

    private fun getDocs() {
        cloudPresenter.setFoldersMode(true)
        when (sectionType) {
            ApiContract.SectionType.CLOUD_USER -> cloudPresenter.getItemsById(DocsMyFragment.ID)
            ApiContract.SectionType.CLOUD_SHARE -> cloudPresenter.getItemsById(DocsShareFragment.ID)
            ApiContract.SectionType.CLOUD_COMMON ->
                cloudPresenter.getItemsById(DocsCommonFragment.ID)
            ApiContract.SectionType.CLOUD_PROJECTS ->
                cloudPresenter.getItemsById(DocsProjectsFragment.ID)
        }
    }

    override val section: Int
        get() = ApiContract.SectionType.UNKNOWN

    companion object {
        val TAG = DocsCloudOperationFragment::class.java.simpleName
        private const val TAG_OPERATION_SECTION_TYPE = "TAG_OPERATION_SECTION_TYPE"

        fun newInstance(sectionType: Int): DocsCloudOperationFragment =
            DocsCloudOperationFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(TAG_OPERATION_SECTION_TYPE, sectionType)
                }
            }

    }
}