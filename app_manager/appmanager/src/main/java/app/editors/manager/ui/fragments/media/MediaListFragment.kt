package app.editors.manager.ui.fragments.media

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.databinding.FragmentMediaListBinding
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.adapters.MediaAdapter
import app.editors.manager.ui.fragments.base.ListFragment
import lib.toolkit.base.managers.utils.UiUtils.getScreenSize
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.ui.adapters.BaseAdapter
import java.util.*

class MediaListFragment : ListFragment(), BaseAdapter.OnItemClickListener, Toolbar.OnMenuItemClickListener {

    private var mediaExplorer: Explorer? = null
    private var mediaAdapter: MediaAdapter? = null
    private var columnsCount = 0
    private var isWebDav = false
    private var viewBinding: FragmentMediaListBinding? = null

    private val position: Int by lazy { requireArguments().getInt(TAG_POSITION) }
    private val mediaActivity by lazy {
        checkNotNull(requireActivity() as? MediaActivity) { "$TAG must implement ${MediaActivity.TAG}" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentMediaListBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mediaAdapter?.setCellSize(getCellSize(columnsCount))
        viewBinding?.fragmentList?.listOfItems?.adapter = mediaAdapter
    }

    override fun onRefresh() {
        // Stub
    }

    override fun onItemClick(view: View, position: Int) {
        showFragment(MediaPagerFragment.newInstance(mediaExplorer, isWebDav, position), null, false)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.toolbarViewMode -> {
                showFragment(MediaPagerFragment.newInstance(mediaExplorer, isWebDav, position), null, false)
            }
        }
        return true
    }

    private fun init() {
        getArgs()
        initViews()
        initToolbar()
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            mediaExplorer = Objects.requireNonNull(bundle, "Media must not be null")
                .getSerializableExt(TAG_MEDIA, Explorer::class.java)
            isWebDav = bundle.getBoolean(TAG_WEB_DAV)
        }
    }

    @SuppressLint("InflateParams")
    private fun initViews() {
        mediaActivity.shareButtonVisible = false
        columnsCount = resources.getInteger(lib.toolkit.base.R.integer.screen_media_grid_columns)
        viewBinding?.fragmentList?.let { binding ->
            binding.listSwipeRefresh.isEnabled = false
            binding.listOfItems.layoutManager = GridLayoutManager(requireContext(), columnsCount)
            binding.listOfItems.setItemViewCacheSize(RECYCLER_CACHE_SIZE)
            binding.listOfItems.adapter = MediaAdapter(getCellSize(columnsCount), lifecycleScope).apply {
                setOnItemClickListener(this@MediaListFragment)
                setItems(mediaExplorer?.files.orEmpty())
            }.also { mediaAdapter = it }
        }
    }

    private fun initToolbar() {
        val sizeText = getString(R.string.media_image_list_info, mediaExplorer?.files?.size.toString())
        //        mediaActivity.shareVisible = false
        mediaActivity.setOnMenuItemClickListener(this)
        if (!mediaExplorer?.current?.title.isNullOrBlank()) {
            mediaActivity.setToolbarTitle(mediaExplorer?.current?.title)
            mediaActivity.setToolbarSubtitle(sizeText)
        } else {
            mediaActivity.setToolbarTitle(sizeText)
        }
    }

    private fun getCellSize(columns: Int): Int {
        return getScreenSize(requireContext()).x / columns
    }

    companion object {
        val TAG: String = MediaListFragment::class.java.simpleName
        private const val TAG_POSITION = "TAG_POSITION"
        private const val TAG_MEDIA = "TAG_MEDIA"
        private const val TAG_WEB_DAV = "TAG_WEB_DAV"
        private const val RECYCLER_CACHE_SIZE = 30

        @JvmStatic
        fun newInstance(explorer: Explorer?, isWebDav: Boolean, position: Int): MediaListFragment {
            return MediaListFragment().apply {
                arguments = Bundle(3).apply {
                    putInt(TAG_POSITION, position)
                    putSerializable(TAG_MEDIA, explorer)
                    putBoolean(TAG_WEB_DAV, isWebDav)
                }
            }
        }
    }
}