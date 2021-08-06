package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.databinding.FragmentAboutBinding
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.FileUtils.readSdkVersion

class AboutFragment : BaseAppFragment(), View.OnClickListener {

    companion object {
        val TAG: String = AboutFragment::class.java.simpleName
        const val ASSETS_SDK_VERSION_PATH = "sdk.version"

        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }

    private var viewBinding: FragmentAboutBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentAboutBinding.inflate(inflater, container, false)
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

    private fun init() {
        setActionBarTitle(getString(R.string.about_title))
        val sdkVersion = readSdkVersion(requireContext(), ASSETS_SDK_VERSION_PATH)
        viewBinding?.aboutAppVersion?.text = getString(
            R.string.about_app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE.toString(),
            sdkVersion
        )
        viewBinding?.aboutTerms?.setOnClickListener(this)
        viewBinding?.aboutPolicy?.setOnClickListener(this)
        viewBinding?.aboutLicense?.setOnClickListener(this)
        viewBinding?.aboutWebsite?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.about_terms -> showUrlInBrowser(getString(R.string.app_url_terms))
            R.id.about_policy -> showUrlInBrowser(getString(R.string.app_url_policy))
            R.id.about_license -> showFragment(LicenseFragment.newInstance(), LicenseFragment.TAG, false)
            R.id.about_website -> showUrlInBrowser(getString(R.string.app_url_main))
        }
    }
}