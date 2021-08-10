package app.editors.manager.onedrive.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.documents.core.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.main.DocsBasePresenter
import app.editors.manager.mvp.views.main.DocsBaseView
import app.editors.manager.onedrive.mvp.presenters.DocsOneDrivePresenter
import app.editors.manager.onedrive.mvp.views.DocsOneDriveView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.main.DocsOnDeviceFragment
import lib.toolkit.base.ui.activities.base.BaseActivity
import moxy.presenter.InjectPresenter

class DocsOneDriveFragment : DocsBaseFragment(), ActionButtonFragment, DocsOneDriveView {

    companion object {
        val TAG = DocsOneDriveFragment::class.java.simpleName

        const val KEY_ACCOUNT = "KEY_ACCOUNT"
        const val KEY_UPLOAD = "KEY_UPLOAD"
        const val KEY_UPDATE = "KEY_UPDATE"

        fun newInstance(account: String): DocsOneDriveFragment {
            return DocsOneDriveFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ACCOUNT, account)
                }
            }
        }
    }


    var account: CloudAccount? = null

    @InjectPresenter
    lateinit var presenter: DocsOneDrivePresenter
    private var activity: IMainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = context as IMainActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsOnDeviceFragment::class.java.simpleName + " - must implement - " +
                        IMainActivity::class.java.simpleName
            )
        }
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isActivePage && resultCode == Activity.RESULT_CANCELED) {
            onRefresh()
        } else if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_DOCS, REQUEST_SHEETS, REQUEST_PRESENTATION -> data?.data?.let {
                    presenter.upload(
                        it,
                        KEY_UPDATE)
                }
                BaseActivity.REQUEST_ACTIVITY_FILE_PICKER -> data?.data?.let {
                    presenter.upload(
                        it,
                        KEY_UPLOAD)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        presenter.checkBackStack()
    }

    override fun onRemoveItemFromFavorites() {

    }

    override fun isWebDav(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
    }

    override fun getPresenter(): DocsBasePresenter<out DocsBaseView> {
        return presenter
    }


    override fun onStateEmptyBackStack() {
        super.onStateEmptyBackStack()
        loadFiles()
        mSwipeRefresh.isRefreshing = true
    }

    private fun loadFiles() {
        presenter.getProvider()
    }

}