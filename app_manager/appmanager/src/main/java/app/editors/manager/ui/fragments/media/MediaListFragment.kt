package app.editors.manager.ui.fragments.media

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import app.editors.manager.R
import app.editors.manager.databinding.FragmentMediaListBinding
import app.editors.manager.databinding.IncludeMediaHeaderListBinding
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.adapters.MediaAdapter
import app.editors.manager.ui.fragments.base.ListFragment
import lib.toolkit.base.managers.utils.UiUtils.getScreenSize
import lib.toolkit.base.ui.adapters.BaseAdapter
import java.util.*

class MediaListFragment : ListFragment(), BaseAdapter.OnItemClickListener {

    private var mediaActivity: MediaActivity? = null
    private var toolbarView: View? = null
    private var toolbarViewHolder: ToolbarViewHolder? = null
    private var mediaExplorer: Explorer? = null
    private var gridLayoutManager: GridLayoutManager? = null
    private var mediaAdapter: MediaAdapter? = null
    private var columnsCount = 0
    private var isWebDav = false
    private var viewBinding: FragmentMediaListBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaActivity = try {
            context as MediaActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                MediaListFragment::class.java.simpleName + " - must implement - " +
                        MediaActivity::class.java.simpleName
            )
        }
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
        setClickedItem(position)
        showFragment(MediaPagerFragment.newInstance(mediaExplorer, isWebDav), null, false)
    }

    private fun init() {
        getArgs()
        initViews()
        setHeader()
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            mediaExplorer = Objects.requireNonNull(bundle, "Media must not be null")
                .getSerializable(TAG_MEDIA) as Explorer?
            isWebDav = bundle.getBoolean(TAG_WEB_DAV)
        }
    }

    @SuppressLint("InflateParams")
    private fun initViews() {
        viewBinding?.fragmentList?.let {
            mediaActivity?.setToolbarState(false)
            toolbarView = layoutInflater.inflate(R.layout.include_media_header_list, null)
            toolbarViewHolder = ToolbarViewHolder(toolbarView)
            mediaActivity?.setToolbarView(toolbarView)
            columnsCount = resources.getInteger(R.integer.screen_media_grid_columns)
            mediaAdapter = MediaAdapter(getCellSize(columnsCount)).apply {
                setOnItemClickListener(this@MediaListFragment)
                setItems(mediaExplorer?.files!!)
            }
            gridLayoutManager = GridLayoutManager(requireContext(), columnsCount)
            it.listSwipeRefresh.isEnabled = false
            it.listOfItems.layoutManager = gridLayoutManager
            it.listOfItems.adapter = mediaAdapter
            it.listOfItems.setItemViewCacheSize(RECYCLER_CACHE_SIZE)
        }
    }

    private fun setHeader() {
        toolbarViewHolder?.mediaListHeaderName?.text = mediaExplorer?.current?.title
        toolbarViewHolder?.mediaListHeaderInfo?.text =
            getString(R.string.media_image_list_info, mediaExplorer!!.files.size.toString())
    }

    private fun getCellSize(columns: Int): Int {
        return getScreenSize(requireContext()).x / columns
    }

    private fun setClickedItem(position: Int) {
        mediaExplorer?.files?.let { files ->
            for (i in files.indices) {
                files[i].isClicked = i == position
            }
        }
    }

    /*
    * Custom view for toolbar
    * */
    inner class ToolbarViewHolder(view: View?) {
        private val viewBinding = IncludeMediaHeaderListBinding.bind(view!!).apply {
            mediaListHeaderViewMode.setOnClickListener {
                showFragment(MediaPagerFragment
                    .newInstance(mediaExplorer, isWebDav), null, false)
            }
        }
        val mediaListHeaderName = viewBinding.mediaListHeaderName
        val mediaListHeaderInfo = viewBinding.mediaListHeaderInfo
    }

    companion object {
        val TAG = MediaListFragment::class.java.simpleName
        private const val TAG_MEDIA = "TAG_MEDIA"
        private const val TAG_WEB_DAV = "TAG_WEB_DAV"
        private const val RECYCLER_CACHE_SIZE = 30

        @JvmStatic
        fun newInstance(explorer: Explorer?, isWebDav: Boolean): MediaListFragment {
            return MediaListFragment().apply {
                arguments = Bundle(2).apply {
                    putSerializable(TAG_MEDIA, explorer)
                    putBoolean(TAG_WEB_DAV, isWebDav)
                }
            }
        }
    }
}