package app.editors.manager.ui.fragments.storage

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import app.editors.manager.R
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.presenters.storage.ConnectPresenter
import app.editors.manager.mvp.views.storage.ConnectView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.fragments.base.WebDavBaseFragment
import app.editors.manager.ui.interfaces.WebDavInterface
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter

class WebDavStorageFragment : WebDavBaseFragment(), ConnectView {

    @InjectPresenter
    lateinit var connectPresenter: ConnectPresenter

    private var providerKey: String? = null
    private var url: String? = null
    private var title: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = try {
            context as WebDavInterface
        } catch (e: ClassCastException) {
            throw RuntimeException(
                WebDavStorageFragment::class.java.simpleName + " - must implement - " +
                        StorageActivity::class.java.simpleName
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        connectPresenter.cancelRequest()
    }

    private fun onSaveClick() {
        viewBinding?.let {
            showWaitingDialog(getString(R.string.dialogs_wait_title_storage))
            connectPresenter.connectWebDav(
                providerKey,
                it.storageWebDavServerEdit.text.toString(),
                it.storageWebDavLoginEdit.text.toString(),
                it.storageWebDavPasswordEdit.text.toString(),
                it.storageWebDavTitleEdit.text.toString(),
                parentActivity?.isMySection == false
            )
        }
    }

    override fun onUnauthorized(message: String?) {
        requireActivity().finish()
        MainActivity.show(requireContext())
    }

    override fun onConnect(folder: CloudFolder) {
        hideDialog()
        parentActivity?.finishWithResult(folder)
    }

    override fun onBackPressed(): Boolean {
        parentFragmentManager.popBackStack()
        hideKeyboard()
        return true
    }

    private fun init() {
        getArgs()
        setActionBarTitle(title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        requireActivity().window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun getArgs() {
        arguments?.let {
            url = it.getString(TAG_URL)
            title = it.getString(TAG_TITLE)
            providerKey = it.getString(TAG_PROVIDER_KEY)
        }
    }

    override fun initViews(isNextCloud: Boolean) {
        parentActivity?.setOnConnectButtonClickListener { onSaveClick() }
        viewBinding?.let { binding ->
            binding.storageWebDavServerEdit.setText(url)
            binding.storageWebDavTitleEdit.setText(title)
            binding.storageWebDavTitleEdit.setActionDoneListener(this::onSaveClick)
            hideUrlLayout()
        }
        super.initViews(isNextCloud)
    }

    private fun hideUrlLayout() {
        if (url?.isNotEmpty() == true) {
            viewBinding?.storageWebDavServerEdit?.isVisible = false
            viewBinding?.storageWebDavServerLayout?.isVisible = false
        }
    }

    companion object {
        val TAG = WebDavStorageFragment::class.java.simpleName
        const val TAG_URL = "TAG_MEDIA"
        const val TAG_TITLE = "TAG_TITLE"
        const val TAG_PROVIDER_KEY = "TAG_PROVIDER_KEY"

        fun newInstance(providerKey: String?, url: String?, title: String?): WebDavStorageFragment =
            WebDavStorageFragment().apply {
                arguments = Bundle(3).apply {
                    putString(TAG_URL, url)
                    putString(TAG_TITLE, title)
                    putString(TAG_PROVIDER_KEY, providerKey)
                }
            }
    }
}
