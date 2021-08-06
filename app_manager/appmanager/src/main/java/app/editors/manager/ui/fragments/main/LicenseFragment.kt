package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.editors.manager.R
import app.editors.manager.databinding.FragmentAboutLicenseBinding
import app.editors.manager.ui.fragments.base.BaseAppFragment

class LicenseFragment : BaseAppFragment() {

    companion object {
        val TAG: String = LicenseFragment::class.java.simpleName

        fun newInstance(): LicenseFragment {
            return LicenseFragment()
        }
    }

    private var viewBinding: FragmentAboutLicenseBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentAboutLicenseBinding.inflate(inflater, container, false)
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
        setActionBarTitle(getString(R.string.about_license))
        viewBinding?.licenseWebview?.loadUrl(getString(R.string.app_licenses_path))
    }
}