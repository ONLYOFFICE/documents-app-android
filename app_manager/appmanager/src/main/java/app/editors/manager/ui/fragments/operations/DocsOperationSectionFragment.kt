package app.editors.manager.ui.fragments.operations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.FragmentOperationSectionBinding
import app.editors.manager.mvp.models.states.OperationsState.OperationType
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import app.editors.manager.ui.fragments.base.BaseAppFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs

class DocsOperationSectionFragment : BaseAppFragment() {

    companion object {

        val TAG: String = DocsOperationSectionFragment::class.java.simpleName

        fun newInstance(
            destFolderId: String = "",
            operationType: OperationType,
            explorer: Explorer? = null
        ): DocsOperationSectionFragment = DocsOperationSectionFragment().putArgs(
            OperationDialogFragment.TAG_OPERATION_TYPE to operationType,
            OperationDialogFragment.TAG_DEST_FOLDER_ID to destFolderId,
            OperationDialogFragment.TAG_OPERATION_EXPLORER to explorer
        )
    }

    private var viewBinding: FragmentOperationSectionBinding? = null

    private val operationDialogFragment by lazy { parentFragment as? OperationDialogFragment }

    private val explorer: Explorer? by lazy {
        arguments?.getSerializableExt(OperationDialogFragment.TAG_OPERATION_EXPLORER)
    }

    private val sectionClick: (id: Int) -> Unit = { id ->
        val sectionType = when (id) {
            R.id.operation_sections_my ->  ApiContract.SectionType.CLOUD_USER
            R.id.operation_sections_share ->  ApiContract.SectionType.CLOUD_SHARE
            R.id.operation_sections_common ->  {
                if (context?.accountOnline.isDocSpace) {
                    ApiContract.SectionType.CLOUD_VIRTUAL_ROOM
                } else {
                    ApiContract.SectionType.CLOUD_COMMON
                }
            }
            R.id.operation_sections_projects ->  ApiContract.SectionType.CLOUD_PROJECTS
            else -> ApiContract.SectionType.CLOUD_USER
        }

        showFragment(
            DocsCloudOperationFragment.newInstance(
                sectionType = sectionType,
                destFolderId = arguments?.getString(OperationDialogFragment.TAG_DEST_FOLDER_ID).orEmpty(),
                operationType = requireNotNull(arguments?.getSerializableExt(OperationDialogFragment.TAG_OPERATION_TYPE)),
                explorer = explorer
            ),
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
        checkRoom()
        operationDialogFragment?.setToolbarTitle(getString(R.string.operation_choose_section))
        operationDialogFragment?.setCreateFolderVisible(false)
    }

    private fun checkRoom() {
        if (context?.accountOnline.isDocSpace) {
            viewBinding?.operationSectionsProjects?.isVisible = false
            viewBinding?.operationSectionsShare?.isVisible = false
            viewBinding?.sectionIcon?.setImageResource(R.drawable.ic_section_rooms)
            viewBinding?.sectionName?.text = getString(R.string.main_pager_docs_virtual_room)
        }
    }

    private fun checkVisitor() {
        lifecycleScope.launch {
            App.getApp().appComponent.accountOnline?.let {
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