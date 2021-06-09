package app.editors.manager.ui.fragments.login

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentCountriesCodesBinding
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.ui.adapters.CountriesCodesAdapter
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.ui.adapters.BaseAdapter
import javax.inject.Inject

class CountriesCodesFragment : BaseAppFragment(), SearchView.OnQueryTextListener,
    BaseAdapter.OnItemClickListener {


    companion object {

        val TAG: String = CountriesCodesFragment::class.java.simpleName

        fun newInstance(): CountriesCodesFragment {
            return CountriesCodesFragment()
        }
    }

    private var viewBinding: FragmentCountriesCodesBinding? = null

    private var searchView: SearchView? = null
    private var closeButton: ImageView? = null
    private var searchItem: MenuItem? = null

    private var countriesCodesAdapter: CountriesCodesAdapter? = null

    @Inject
    lateinit var countriesCodesTool: CountriesCodesTool

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentCountriesCodesBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.countries_codes, menu)
        searchItem = menu.findItem(R.id.menu_countries_search)
        searchView = searchItem?.actionView as SearchView
        searchView?.setOnQueryTextListener(this)
        searchView?.maxWidth = Int.MAX_VALUE
        searchView?.setOnCloseListener {
            if (searchView?.query?.isNotEmpty() == true) {
                searchView?.setQuery("", true)
                return@setOnCloseListener true
            }
            false
        }
        closeButton = searchView?.findViewById(androidx.appcompat.R.id.search_close_btn)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        countriesCodesAdapter?.filter?.filter(newText)
        return false
    }

    override fun onBackPressed(): Boolean {
        if (searchView?.isIconified == false) {
            searchView?.setQuery("", false)
            searchView?.isIconified = true
            return true
        }
        return super.onBackPressed()
    }

    override fun onItemClick(view: View, position: Int) {
        val codes = countriesCodesAdapter?.getItem(position)
        fragmentManager?.popBackStack(
            EnterprisePhoneFragment.TAG,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        fragmentManager?.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        codes?.let { EnterprisePhoneFragment.newInstance(it.mNumber, it.mName, it.mCode) }?.let {
            showFragment(
                it,
                EnterprisePhoneFragment.TAG,
                false
            )
        }
    }

    private fun init() {
        setActionBarTitle(getString(R.string.countries_codes_title))
        countriesCodesAdapter = context?.let { CountriesCodesAdapter(it) }
        countriesCodesAdapter?.setItems(countriesCodesTool.codes)
        countriesCodesAdapter?.setOnItemClickListener(this)
        viewBinding?.fragmentCountriesCodesList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = countriesCodesAdapter
        }
    }
}