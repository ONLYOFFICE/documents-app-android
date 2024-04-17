package app.editors.manager.ui.fragments.storage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import app.documents.core.network.common.models.Storage
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentStorageSelectBinding
import app.editors.manager.mvp.presenters.storage.SelectPresenter
import app.editors.manager.mvp.views.storage.SelectView
import app.editors.manager.ui.adapters.StorageAdapter
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.putArgs
import lib.toolkit.base.ui.adapters.BaseAdapter
import moxy.presenter.InjectPresenter

class SelectFragment : BaseAppFragment(), BaseAdapter.OnItemClickListener, SelectView {

    @InjectPresenter
    lateinit var presenter: SelectPresenter

    private var storageAdapter: StorageAdapter? = null
    private var viewBinding: FragmentStorageSelectBinding? = null
    private val providerKey: String? by lazy { arguments?.getString(PROVIDER_KEY) }
    private val providerId: Int by lazy { arguments?.getInt(PROVIDER_ID) ?: -1 }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentStorageSelectBinding.inflate(layoutInflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        presenter.getStorages()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onItemClick(view: View, position: Int) {
        storageAdapter?.getItem(position)?.let {
            presenter.connect(it)
        }
    }

    private fun init() {
        setActionBarTitle(getString(R.string.storage_select_title))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        initViews()
    }

    private fun initViews() {
        storageAdapter = StorageAdapter().apply {
            setOnItemClickListener(this@SelectFragment)
        }
        viewBinding?.let {
            it.listOfItems.layoutManager = LinearLayoutManager(context)
            it.listOfItems.adapter = storageAdapter
            it.refreshLayout.setOnRefreshListener { presenter.getStorages() }
        }
    }

    override fun onUpdate(storages: List<String>) {
        val key = providerKey
        if (key == null) {
            storageAdapter?.setItems(storages)
        } else {
            presenter.connect(key)
        }
    }

    override fun showWebTokenFragment(storage: Storage) {
        showFragment(WebTokenFragment.newInstance(storage.copy(providerId = providerId)), WebTokenFragment.TAG, false)
    }

    override fun showWebDavFragment(providerKey: String, url: String, title: String) {
        showFragment(WebDavStorageFragment.newInstance(providerKey, url, title), WebDavStorageFragment.TAG, false)
    }

    override fun showProgress(isVisible: Boolean) {
        viewBinding?.refreshLayout?.isRefreshing = isVisible
    }

    override fun onError(message: String?) {
        message?.let { showSnackBar(it) }
    }

    companion object {
        val TAG = SelectFragment::class.java.simpleName
        private const val ROOM_STORAGE_KEY = "provider_key"
        private const val TITLE_KEY = "provider_key"
        private const val PROVIDER_KEY = "provider_key"
        private const val PROVIDER_ID = "provider_id"

        fun newInstance(
            isRoomStorage: Boolean = false,
            title: String? = null,
            providerKey: String? = null,
            providerId: Int,
        ): SelectFragment = SelectFragment()
            .putArgs(
                ROOM_STORAGE_KEY to isRoomStorage,
                TITLE_KEY to title,
                PROVIDER_KEY to providerKey,
                PROVIDER_ID to providerId
            )
    }
}