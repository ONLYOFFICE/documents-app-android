package app.editors.manager.ui.fragments.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.pager.PagingViewPager
import app.editors.manager.ui.views.pager.ViewPagerAdapter

class PortalsPagerFragment : BaseAppFragment() {

    companion object {
        val TAG: String = PortalsPagerFragment::class.java.simpleName

        fun newInstance(): PortalsPagerFragment {
            return PortalsPagerFragment()
        }
    }


    private var viewpager: PagingViewPager? = null
    private var portalsActivity: PortalsActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        portalsActivity = try {
            context as PortalsActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                PortalsPagerFragment::class.java.simpleName + " - must implement - " +
                        PortalsActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_pager, container, false)?.apply {
            viewpager = findViewById(R.id.on_boarding_view_pager)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }


    private fun init() {
        portalsActivity?.tabLayout?.setupWithViewPager(viewpager, true)
        viewpager?.adapter = ViewPagerAdapter(childFragmentManager, fragments)
    }

    private val fragments: List<ViewPagerAdapter.Container> by lazy {
        val list = arrayListOf<ViewPagerAdapter.Container>()
        list.add(ViewPagerAdapter.Container(
            EnterprisePortalFragment.newInstance(),
            getString(R.string.login_pager_enterprise)
        ))
        if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents") {
            list.add(ViewPagerAdapter.Container(
                PersonalPortalFragment.newInstance(),
                getString(R.string.login_pager_personal)
            ))
        }
        return@lazy list
    }

}