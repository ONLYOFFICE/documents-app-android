package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.ActivityOperationBinding
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import app.editors.manager.ui.fragments.operations.DocsDropboxOperationFragment
import app.editors.manager.ui.fragments.operations.DocsGoogleDriveOperationFragment
import app.editors.manager.ui.fragments.operations.DocsOneDriveOperationFragment
import app.editors.manager.ui.fragments.operations.DocsOperationSectionFragment
import app.editors.manager.ui.fragments.operations.DocsWebDavOperationFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.getSerializable
import javax.inject.Inject

class OperationActivity : BaseAppActivity(){

    interface OnActionClickListener {
        fun onActionClick()
    }

    companion object {
        val TAG: String = OperationActivity::class.java.simpleName

        const val TAG_OPERATION_TYPE = "TAG_OPERATION_OPERATION_TYPE"
        const val TAG_OPERATION_EXPLORER = "TAG_OPERATION_EXPLORER"
        const val TAG_IS_WEB_DAV = "TAG_IS_WEB_DAV"

        fun getIntent(context: Context, operation: OperationType, explorer: Explorer) = Intent(context, OperationActivity::class.java).apply {
            putExtra(TAG_OPERATION_TYPE, operation)
            putExtra(TAG_OPERATION_EXPLORER, explorer)
        }
    }

    @Inject
    lateinit var cloudDataSource: CloudDataSource

    private var operationType: OperationType? = null
    private var actionClickListener: OnActionClickListener? = null
    private var viewBinding: ActivityOperationBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getApp().appComponent.inject(this)
        viewBinding = ActivityOperationBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
        initListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    private fun init(savedInstanceState: Bundle?) {
        setFinishOnTouchOutside(true)
        setSupportActionBar(viewBinding?.appBarToolbar)
        operationType = intent.getSerializable(TAG_OPERATION_TYPE, OperationType::class.java)
        initButton(operationType)
        if (savedInstanceState == null) {
            initState()
        }
    }

    private fun initState() {
       lifecycleScope.launch {
            accountOnline?.let { account ->
                withContext(Dispatchers.Main) {
                    if (account.isWebDav) {
                        showFragment(
                            DocsWebDavOperationFragment.newInstance(
                                WebdavProvider.valueOf(account.portal.provider)
                            ), null
                        )
                    } else if(account.isOneDrive) {
                        showFragment(DocsOneDriveOperationFragment.newInstance(), null)
                    } else if(account.isDropbox) {
                        showFragment(DocsDropboxOperationFragment.newInstance(), null)
                    } else if(account.isGoogleDrive) {
                        showFragment(DocsGoogleDriveOperationFragment.newInstance(), null)
                    } else {
                        if (account.isPersonal()) {
                            showFragment(DocsCloudOperationFragment.newInstance(ApiContract.SectionType.CLOUD_USER), null)
                        } else {
                            showFragment(DocsOperationSectionFragment.newInstance(Json.encodeToString(account)), null)
                        }
                    }
                }
            }
        }
    }

    private fun initListeners() {
        viewBinding?.operationPanel?.operationActionButton?.setOnClickListener {
            actionClickListener?.onActionClick()
        }
        viewBinding?.operationPanel?.operationCancelButton?.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun initButton(actionOperationType: OperationType?) {
        when (actionOperationType) {
            OperationType.COPY -> viewBinding?.operationPanel?.operationActionButton?.setText(R.string.operation_panel_copy_button)
            OperationType.MOVE -> viewBinding?.operationPanel?.operationActionButton?.setText(R.string.operation_panel_move_button)
            OperationType.RESTORE -> viewBinding?.operationPanel?.operationActionButton?.setText(R.string.operation_panel_restore_button)
            else -> {
            }
        }
    }

    fun setEnabledActionButton(isEnabled: Boolean) {
        viewBinding?.operationPanel?.operationActionButton?.isEnabled = isEnabled
    }

    fun setOnActionClickListener(onActionClickListener: OnActionClickListener?) {
        actionClickListener = onActionClickListener
    }

}