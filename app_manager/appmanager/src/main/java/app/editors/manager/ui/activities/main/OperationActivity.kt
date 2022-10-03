package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import app.documents.core.account.AccountDao
import app.documents.core.network.ApiContract
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.ActivityOperationBinding
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.storages.dropbox.ui.fragments.operations.DocsDropboxOperationFragment
import app.editors.manager.storages.googledrive.ui.fragments.operations.DocsGoogleDriveOperationFragment
import app.editors.manager.storages.onedrive.ui.fragments.operations.DocsOneDriveOperationFragment
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.operations.DocsCloudOperationFragment
import app.editors.manager.ui.fragments.operations.DocsOperationSectionFragment
import app.editors.manager.ui.fragments.operations.DocsWebDavOperationFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

        @JvmStatic
        fun showCopy(fragment: Fragment, explorer: Explorer) {
            val intent = Intent(fragment.context, OperationActivity::class.java)
            intent.putExtra(TAG_OPERATION_TYPE, OperationType.COPY)
            intent.putExtra(TAG_OPERATION_EXPLORER, explorer)
            fragment.startActivityForResult(intent, REQUEST_ACTIVITY_OPERATION)
        }

        @JvmStatic
        fun showMove(fragment: Fragment, explorer: Explorer) {
            val intent = Intent(fragment.context, OperationActivity::class.java)
            intent.putExtra(TAG_OPERATION_TYPE, OperationType.MOVE)
            intent.putExtra(TAG_OPERATION_EXPLORER, explorer)
            fragment.startActivityForResult(intent, REQUEST_ACTIVITY_OPERATION)
        }

        @JvmStatic
        fun showRestore(fragment: Fragment, explorer: Explorer) {
            val intent = Intent(fragment.context, OperationActivity::class.java)
            intent.putExtra(TAG_OPERATION_TYPE, OperationType.RESTORE)
            intent.putExtra(TAG_OPERATION_EXPLORER, explorer)
            fragment.startActivityForResult(intent, REQUEST_ACTIVITY_OPERATION)
        }
    }

    @Inject
    lateinit var accountDao: AccountDao

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
        operationType = intent.getSerializableExtra(TAG_OPERATION_TYPE) as OperationType?
        initButton(operationType)
        if (savedInstanceState == null) {
            initState()
        }
    }

    private fun initState() {
       lifecycleScope.launch {
            accountDao.getAccountOnline()?.let { account ->
                withContext(Dispatchers.Main) {
                    if (account.isWebDav) {
                        showFragment(
                            DocsWebDavOperationFragment.newInstance(
                                WebDavApi.Providers.valueOf(
                                    account.webDavProvider ?: ""
                                )
                            ), null
                        )
                    } else if(account.isOneDrive) {
                        showFragment(DocsOneDriveOperationFragment.newInstance(), null)
                    } else if(account.isDropbox) {
                        showFragment(DocsDropboxOperationFragment.newInstance(), null)
                    } else if(account.isGoogleDrive) {
                        showFragment(DocsGoogleDriveOperationFragment.newInstance(), null)
                    } else {
                        if (account.portal?.contains(ApiContract.PERSONAL_HOST) == true) {
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