package app.editors.manager.ui.fragments.operations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentOperationSectionBinding
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocsOperationSectionFragment : BaseAppFragment() {

    companion object {
        val TAG: String = DocsOperationSectionFragment::class.java.simpleName

        fun newInstance(account: String): DocsOperationSectionFragment {
            return DocsOperationSectionFragment().apply {
                arguments = Bundle(1).apply {
                    putString(DocsCloudFragment.KEY_ACCOUNT, account)
                }
            }
        }
    }

    private var viewBinding: FragmentOperationSectionBinding? = null

    private val sectionClick: (id: Int) -> Unit = { id ->
        val sectionType = when (id) {
            R.id.operation_sections_my ->  ApiContract.SectionType.CLOUD_USER
            R.id.operation_sections_share ->  ApiContract.SectionType.CLOUD_SHARE
            R.id.operation_sections_common ->  ApiContract.SectionType.CLOUD_COMMON
            R.id.operation_sections_projects ->  ApiContract.SectionType.CLOUD_PROJECTS
            else -> ApiContract.SectionType.CLOUD_USER
        }
        showFragment(
            DocsCloudOperationFragment.newInstance(sectionType),
            DocsCloudOperationFragment.TAG, false
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentOperationSectionBinding.inflate(inflater, container, false)
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
        checkVisitor()
        setListeners()
        setActionBarTitle(getString(R.string.operation_choose_section))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

    }

    private fun checkVisitor() {
        CoroutineScope(Dispatchers.Default).launch {
            App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                withContext(Dispatchers.Main) {
                    viewBinding?.operationSectionsMy?.visibility = if (it.isVisitor) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun setListeners() {
        viewBinding?.operationSectionsMy?.setOnClickListener { sectionClick(it.id) }
        viewBinding?.operationSectionsCommon?.setOnClickListener { sectionClick(it.id) }
        viewBinding?.operationSectionsProjects?.setOnClickListener { sectionClick(it.id) }
        viewBinding?.operationSectionsShare?.setOnClickListener { sectionClick(it.id) }
    }


}