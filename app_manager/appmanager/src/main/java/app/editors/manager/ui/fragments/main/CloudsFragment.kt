package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.databinding.FragmentChooseCloudsBinding
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment

class CloudsFragment : BaseAppFragment() {

    companion object {
        val TAG: String = CloudsFragment::class.java.simpleName

        private const val KEY_IS_BACK = "KEY_IS_BACK"

        fun newInstance(isBack: Boolean): CloudsFragment {
            return CloudsFragment().apply {
                arguments = Bundle(1).apply {
                    putBoolean(KEY_IS_BACK, isBack)
                }
            }
        }
    }

    private var isBack = false
    private var viewBinding: FragmentChooseCloudsBinding? = null

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentChooseCloudsBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isBack = it.getBoolean(KEY_IS_BACK)
        }
        init()
    }

    fun init() {
        setActionBarTitle(getString(R.string.fragment_clouds_title))

        viewBinding?.cloudsItemOnlyOffice?.setOnClickListener {
            PortalsActivity.show(requireActivity())
        }

        viewBinding?.cloudsItemNextCloud?.bind(
            R.drawable.ic_storage_nextcloud,
            R.string.storage_select_next_cloud
        ) {
            WebDavLoginActivity.show(requireActivity(), WebDavApi.Providers.NextCloud, null)
        }

        viewBinding?.cloudsItemOwnCloud?.bind(
            R.drawable.ic_storage_owncloud,
            R.string.storage_select_own_cloud
        ) {
            WebDavLoginActivity.show(requireActivity(), WebDavApi.Providers.OwnCloud, null)
        }

        //TODO need KDrive icon
        viewBinding?.cloudsItemKDrive?.bind(
            R.drawable.ic_storage_webdav,
            R.string.storage_select_kdrive
        ) {
            WebDavLoginActivity.show(requireActivity(), WebDavApi.Providers.KDrive, null)
        }

        viewBinding?.cloudsItemYandex?.isVisible = false
//        viewBinding?.cloudsItemYandex?.bind(
//            R.drawable.ic_storage_yandex,
//            R.string.storage_select_yandex
//        ) {
//            WebDavLoginActivity.show(requireActivity(), WebDavApi.Providers.Yandex, null)
//        }

        viewBinding?.cloudsItemWebDav?.bind(
            R.drawable.ic_storage_webdav,
            R.string.storage_select_web_dav
        ) {
            WebDavLoginActivity.show(requireActivity(), WebDavApi.Providers.WebDav, null)
        }
    }


}