package app.editors.manager.ui.fragments.operations

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import app.documents.core.network.webdav.WebDavService
import app.editors.manager.R
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.activities.main.OperationActivity.OnActionClickListener
import app.editors.manager.ui.fragments.main.DocsWebDavFragment
import lib.toolkit.base.managers.utils.getSerializable
import lib.toolkit.base.managers.utils.getSerializableExt

class DocsWebDavOperationFragment : DocsWebDavFragment(), OnActionClickListener {

    private var operationActivity: OperationActivity? = null
    private var operationType: OperationType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider = requireArguments().getSerializableExt(KEY_PROVIDER, WebDavApi.Providers::class.java)
        setHasOptionsMenu(false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            operationActivity = context as OperationActivity
            operationActivity?.setOnActionClickListener(this)
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsWebDavOperationFragment::class.java.simpleName + " - must implement - " +
                        OperationActivity::class.java.simpleName
            )
        }
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

    override fun onActionClick() {
        when (operationType) {
            OperationType.COPY -> webDavPresenter.copy()
            OperationType.MOVE -> webDavPresenter.move()
            else -> {}
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
        getArgs(savedInstanceState)
        initViews()
        webDavPresenter.checkBackStack()
    }

    private fun getArgs(savedInstanceState: Bundle?) {
        val bundle = arguments
        if (bundle != null) {
            val intent = requireActivity().intent
            operationType = intent.getSerializable(OperationActivity.TAG_OPERATION_TYPE, OperationType::class.java)
            if (savedInstanceState == null) {
                val explorer = intent.getSerializable(OperationActivity.TAG_OPERATION_EXPLORER, Explorer::class.java)
                webDavPresenter.setOperationExplorer(explorer)
            }
        }
    }

    private fun initViews() {
        operationActivity?.setEnabledActionButton(false)
        explorerAdapter?.isFoldersMode = true
        swipeRefreshLayout?.setPadding(0, 0, 0, 0)
    }

    private fun getDocs() {
        webDavPresenter.isFoldersMode = true
        webDavPresenter.getProvider()
    }

    companion object {
        val TAG: String = DocsWebDavOperationFragment::class.java.simpleName

        fun newInstance(provider: WebDavService.Providers?): DocsWebDavOperationFragment {
            return DocsWebDavOperationFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(KEY_PROVIDER, provider)
                }
            }
        }
    }
}