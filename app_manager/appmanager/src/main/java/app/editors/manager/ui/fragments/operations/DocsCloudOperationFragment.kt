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
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.activities.main.OperationActivity.OnActionClickListener
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import lib.toolkit.base.managers.utils.getSerializable

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
        if (sectionType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM && presenter.isRoot) {
            operationActivity?.setEnabledActionButton(false)
        } else {
            operationActivity?.setEnabledActionButton(true)
        }
    }

    override fun onDocsBatchOperation() {
        super.onDocsBatchOperation()
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        setActionBarTitle(getString(R.string.operation_title))
        getDocs()
    }

    override fun onActionClick() {
        when (operationType) {
            OperationsState.OperationType.COPY -> cloudPresenter.copy()
            OperationsState.OperationType.MOVE -> cloudPresenter.tryMove()
            OperationsState.OperationType.RESTORE -> cloudPresenter.tryMove()
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
            savedInstanceState ?: run {
                requireActivity().intent
                    .getSerializable(OperationActivity.TAG_OPERATION_EXPLORER, Explorer::class.java).let { explorer ->
                        cloudPresenter.setOperationExplorer(explorer)
                    }
            }
        }
    }

    private fun initViews() {
        operationActivity?.setEnabledActionButton(false)
        explorerAdapter?.isFoldersMode = true
        recyclerView?.setPadding(0, 0, 0, 0)
    }

    private fun getDocs() {
        cloudPresenter.isFoldersMode = true
        when (sectionType) {
            ApiContract.SectionType.CLOUD_USER -> cloudPresenter.getItemsById(ApiContract.SectionPath.MY)
            ApiContract.SectionType.CLOUD_SHARE -> cloudPresenter.getItemsById(ApiContract.SectionPath.SHARED)
            ApiContract.SectionType.CLOUD_COMMON -> cloudPresenter.getItemsById(ApiContract.SectionPath.COMMON)
            ApiContract.SectionType.CLOUD_PROJECTS -> cloudPresenter.getItemsById(ApiContract.SectionPath.PROJECTS)
            ApiContract.SectionType.CLOUD_VIRTUAL_ROOM -> cloudPresenter.getItemsById(ApiContract.SectionPath.ROOMS)
        }
    }

    companion object {
        val TAG: String = DocsCloudOperationFragment::class.java.simpleName
        private const val TAG_OPERATION_SECTION_TYPE = "section"

        fun newInstance(sectionType: Int): DocsCloudOperationFragment =
            DocsCloudOperationFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(TAG_OPERATION_SECTION_TYPE, sectionType)
                }
            }

    }
}