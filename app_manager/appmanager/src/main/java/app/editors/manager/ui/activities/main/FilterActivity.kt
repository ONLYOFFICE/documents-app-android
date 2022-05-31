package app.editors.manager.ui.activities.main

import android.os.Bundle
import androidx.core.view.isVisible
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.databinding.ActivityFilterBinding
import app.editors.manager.mvp.presenters.main.DocsFilterPresenter
import app.editors.manager.mvp.views.main.DocsFilterView
import app.editors.manager.ui.activities.base.BaseAppActivity
import com.google.android.material.chip.Chip
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


enum class FilterType(val checkedId: Int, val filterVal: String) {
    Folders(R.id.folders, ApiContract.Parameters.VAL_FILTER_BY_FOLDERS),
    Documents(R.id.documents, ApiContract.Parameters.VAL_FILTER_BY_DOCUMENTS),
    Presentations(R.id.presentations, ApiContract.Parameters.VAL_FILTER_BY_PRESENTATIONS),
    Spreadsheets(R.id.spreadsheets, ApiContract.Parameters.VAL_FILTER_BY_SPREADSHEETS),
    Images(R.id.images, ApiContract.Parameters.VAL_FILTER_BY_IMAGES),
    Media(R.id.media, ApiContract.Parameters.VAL_FILTER_BY_MEDIA),
    Archives(R.id.archives, ApiContract.Parameters.VAL_FILTER_BY_ARCHIVE),
    All(R.id.all, ApiContract.Parameters.VAL_FILTER_BY_FILES),
    None(-1, ApiContract.Parameters.VAL_FILTER_BY_NONE)
}

class FilterActivity : BaseAppActivity(), DocsFilterView {

    companion object {
        const val KEY_ID = "key_id"
        const val REQUEST_ACTIVITY_FILTERS_CHANGED = 1004

        fun getFilterType(checkedId: Int): FilterType {
            return FilterType.values().find { it.checkedId == checkedId } ?: FilterType.None
        }
    }

    @InjectPresenter
    lateinit var presenter: DocsFilterPresenter

    @ProvidePresenter
    fun providePresenter(): DocsFilterPresenter {
        return DocsFilterPresenter(intent.extras?.getString(KEY_ID))
    }

    private var viewBinding: ActivityFilterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    override fun updateViewState() {
        viewBinding?.resetButton?.isEnabled = presenter.hasFilter
    }

    override fun onFilterReset() {
        viewBinding?.typesGroup?.clearCheck()
        viewBinding?.subfolder?.isChecked = false
    }

    override fun onFilterProgress() {
        viewBinding?.resultText?.isVisible = false
        viewBinding?.resultProgress?.isVisible = true
    }

    override fun onFilterResult(count: Int) {
        viewBinding?.resultProgress?.isVisible = false
        viewBinding?.resultText?.isVisible = true
        viewBinding?.resultText?.text = getString(R.string.filter_show_results, count.toString())
        if (presenter.hasChanged) setResult(RESULT_OK)
    }

    override fun handleError(message: String?) {
        showToast(message ?: getString(R.string.errors_connection_error))
    }

    private fun init() {
        initToolbar()
        initChipGroup()
        initViews()
        presenter.update(initialCall = true)
    }

    private fun initViews() {
        viewBinding?.let { binding ->
            binding.users.setOnClickListener {
                // TODO
            }
            binding.groups.setOnClickListener {
                // TODO
            }
            binding.subfolder.isChecked = presenter.filterSubfolder
            binding.subfolder.setOnCheckedChangeListener { _, checked ->
                presenter.filterSubfolder = checked
            }
            binding.showButton.setOnClickListener {
                finish()
            }
            binding.resetButton.isEnabled = presenter.hasFilter
            binding.resetButton.setOnClickListener {
                presenter.reset()
            }
        }
    }

    private fun initChipGroup() {
        viewBinding?.typesGroup?.let { typesGroup ->
            typesGroup.findViewById<Chip>(presenter.filterType.checkedId)?.isChecked = true
            typesGroup.setOnCheckedChangeListener { _, checkedId ->
                presenter.filterType = getFilterType(checkedId)
            }
        }
    }

    private fun initToolbar() {
        viewBinding?.toolbar?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
    }
}