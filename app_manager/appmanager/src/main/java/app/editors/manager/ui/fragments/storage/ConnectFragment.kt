package app.editors.manager.ui.fragments.storage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentStorageConnectBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.presenters.storage.ConnectPresenter
import app.editors.manager.mvp.views.storage.ConnectView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class ConnectFragment : BaseAppFragment(), ConnectView {

    @InjectPresenter
    lateinit var connectPresenter: ConnectPresenter

    @Inject
    lateinit var preferenceTool: PreferenceTool

    private var storageActivity: StorageActivity? = null
    private var storage: Storage? = null
    private var token: String? = null
    private var viewBinding: FragmentStorageConnectBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
        storageActivity = try {
            context as StorageActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                ConnectFragment::class.java.simpleName + " - must implement - " +
                        StorageActivity::class.java.getSimpleName()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentStorageConnectBinding.inflate(layoutInflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        connectPresenter.cancelRequest()
    }

    override fun onError(message: String?) {
        hideDialog()
        message?. let {
            showSnackBar(message)
        }
    }

    override fun onUnauthorized(message: String?) {
        requireActivity().finish()
        MainActivity.show(requireContext())
    }

    override fun onConnect(folder: CloudFolder) {
        hideDialog()
        storageActivity?.finishWithResult(folder)
    }

    private fun init() {
        setActionBarTitle(getString(R.string.storage_connect_title))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        getArgs()
        initViews()
        initListeners()
    }

    private fun getArgs() {
        arguments?.let { arguments ->
            token = arguments.getString(TAG_TOKEN)
            storage = arguments.getParcelable(TAG_STORAGE)
        }
    }

    private fun initViews() {
        viewBinding?.let {
            var title: String = getString(R.string.storage_connect_title) + " "
            when (storage!!.name) {
                ApiContract.Storage.BOXNET -> {
                    it.storageConnectTitleEdit.setText(R.string.storage_select_box)
                    title += getString(R.string.storage_select_box)
                }
                ApiContract.Storage.DROPBOX -> {
                    it.storageConnectTitleEdit.setText(R.string.storage_select_drop_box)
                    title += getString(R.string.storage_select_drop_box)
                }
                ApiContract.Storage.SHAREPOINT -> {
                    it.storageConnectTitleEdit.setText(R.string.storage_select_share_point)
                    title += getString(R.string.storage_select_share_point)
                }
                ApiContract.Storage.GOOGLEDRIVE -> {
                    it.storageConnectTitleEdit.setText(R.string.storage_select_google_drive)
                    title += getString(R.string.storage_select_google_drive)
                }
                ApiContract.Storage.ONEDRIVE -> {
                    it.storageConnectTitleEdit.setText(R.string.storage_select_one_drive)
                    title += getString(R.string.storage_select_one_drive)
                }
                ApiContract.Storage.YANDEX -> {
                    it.storageConnectTitleEdit.setText(R.string.storage_select_yandex)
                    title += getString(R.string.storage_select_yandex)
                }
                ApiContract.Storage.WEBDAV -> {
                    it.storageConnectTitleEdit.setText(R.string.storage_select_web_dav)
                    title += getString(R.string.storage_select_web_dav)
                }
            }
            setActionBarTitle(title)
        }
    }

    private fun onSaveClick() {
        val storageTitle = viewBinding?.storageConnectTitleEdit?.text.toString()
        if (storageTitle.isNotEmpty()) {
            showWaitingDialog(getString(R.string.dialogs_wait_title_storage))
            connectPresenter.connectService(token, storage?.name,
                storageTitle, !storageActivity?.isMySection!!)
        } else {
            showSnackBar(R.string.storage_connect_empty_title)
        }
    }

    private fun initListeners() {
        viewBinding?.let { binding ->
            binding.storageConnectTitleEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSaveClick()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            binding.storageConnectSave.setOnClickListener {
                onSaveClick()
            }
        }
    }

    companion object {
        @JvmField
        val TAG = ConnectFragment::class.java.simpleName
        const val TAG_TOKEN = "TAG_TOKEN"
        const val TAG_STORAGE = "TAG_MEDIA"

        @JvmStatic
        fun newInstance(token: String?, storage: Storage?): ConnectFragment =
            ConnectFragment().apply {
                arguments = Bundle(2).apply {
                    putString(TAG_TOKEN, token)
                    putParcelable(TAG_STORAGE, storage)
                }
            }
    }
}