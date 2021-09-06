package app.editors.manager.ui.fragments.storage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentStorageWebDavBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.presenters.storage.ConnectPresenter
import app.editors.manager.mvp.views.storage.ConnectView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class WebDavFragment : BaseAppFragment(), ConnectView {

    @Inject
    lateinit var mPreferenceTool: PreferenceTool

    @InjectPresenter
    lateinit var mConnectPresenter: ConnectPresenter

    private var storageActivity: StorageActivity? = null
    private var fieldsWatcher: FieldsWatcher? = null
    private var providerKey: String? = null
    private var url: String? = null
    private var title: String? = null
    private var viewBinding: FragmentStorageWebDavBinding? = null

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
        viewBinding = FragmentStorageWebDavBinding.inflate(layoutInflater, container, false)
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
        mConnectPresenter.cancelRequest()
    }

    private fun onSaveClick() {
        viewBinding?.let {
            showWaitingDialog(getString(R.string.dialogs_wait_title_storage))
            mConnectPresenter.connectWebDav(
                providerKey,
                it.storageWebDavUrlEdit.text.toString(),
                it.storageWebDavLoginEdit.text.toString(),
                it.storageWebDavPasswordEdit.text.toString(),
                it.storageWebDavTitleEdit.text.toString(),
                storageActivity?.isMySection == false
            )
        }
    }

    override fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(it) }
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
        getArgs()
        setActionBarTitle(title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        requireActivity().window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        initViews()
    }

    private fun getArgs() {
        arguments?.let {
            url = it.getString(TAG_URL)
            title = it.getString(TAG_TITLE)
            providerKey = it.getString(TAG_PROVIDER_KEY)
        }
    }

    private fun initViews() {
        viewBinding?.let {
            fieldsWatcher = FieldsWatcher()
            it.storageWebDavUrlEdit.addTextChangedListener(fieldsWatcher)
            it.storageWebDavLoginEdit.addTextChangedListener(fieldsWatcher)
            it.storageWebDavPasswordEdit.addTextChangedListener(fieldsWatcher)
            it.storageWebDavUrlEdit.setText(url)
            it.storageWebDavTitleEdit.setText(title)
            hideUrlLayout()
            it.storageWebDavTitleEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSaveClick()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        }
    }

    private fun hideUrlLayout() {
        if (url?.isNotEmpty() == true) {
            viewBinding?.storageWebDavUrlEdit?.isVisible = false
            viewBinding?.storageWebDavUrlLayout?.isVisible = false
        }
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.let {
                it.storageWebDavSaveButton.isEnabled =
                    it.storageWebDavUrlEdit.text?.isNotEmpty() == true
                            && it.storageWebDavLoginEdit.text?.isNotEmpty() == true
                            && it.storageWebDavPasswordEdit.text?.isNotEmpty() == true
                            && it.storageWebDavTitleEdit.text?.isNotEmpty() == true
            }
        }
    }

    companion object {
        val TAG = WebDavFragment::class.java.simpleName
        const val TAG_URL = "TAG_MEDIA"
        const val TAG_TITLE = "TAG_TITLE"
        const val TAG_PROVIDER_KEY = "TAG_PROVIDER_KEY"

        fun newInstance(providerKey: String?, url: String?, title: String?): WebDavFragment =
            WebDavFragment().apply {
                arguments = Bundle(3).apply {
                    putString(TAG_URL, url)
                    putString(TAG_TITLE, title)
                    putString(TAG_PROVIDER_KEY, providerKey)
                }
            }
    }
}