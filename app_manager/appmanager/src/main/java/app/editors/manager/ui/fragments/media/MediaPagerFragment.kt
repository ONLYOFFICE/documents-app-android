package app.editors.manager.ui.fragments.media

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.databinding.FragmentMediaPagerBinding
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.pager.PagingViewPager
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.getSerializableExt

class MediaPagerFragment : BaseAppFragment(), Toolbar.OnMenuItemClickListener {

    interface OnMediaListener : PagingViewPager.OnPagerListener {
        fun onShareClick()
    }

    private var mediaActivity: MediaActivity? = null
    private var pagerAdapter: PagerAdapter2? = null
    private var viewBinding: FragmentMediaPagerBinding? = null

    private val position: Int by lazy { requireArguments().getInt(TAG_POSITION) }
    private val isWebDav: Boolean by lazy { requireArguments().getBoolean(TAG_WEB_DAV) }
    private val mediaExplorer: Explorer by lazy {
        requireArguments().getSerializableExt(TAG_MEDIA, Explorer::class.java)
    }

    private val writePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            (activeFragment as? OnMediaListener)?.onShareClick()
        } else {
            showSnackBar("Not permission")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaActivity = try {
            context as MediaActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                MediaPagerFragment::class.java.simpleName + " - must implement - " +
                        MediaActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentMediaPagerBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.toolbarViewMode -> {
                showFragment(MediaListFragment.newInstance(mediaExplorer, isWebDav, position), null, false)
            }
            R.id.toolbarShare -> shareFile()
        }
        return true
    }

    @SuppressLint("InflateParams")
    private fun initViews() {
        pagerAdapter = PagerAdapter2(this, mediaExplorer)
        mediaActivity?.setOnMenuItemClickListener(this)
        viewBinding?.mediaPager?.let { pager ->
            pager.adapter = pagerAdapter
            pager.registerOnPageChangeCallback(PagerListener(mediaExplorer))
            pager.setCurrentItem(position, false)
            setPagerPosition(position)
            hideShareForVideo(position)
        }
    }

    @SuppressLint("MissingPermission")
    private fun shareFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            (activeFragment as? OnMediaListener)?.onShareClick()
        } else {
            writePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPagerPosition(position: Int) {
        viewBinding?.mediaPagerPosition?.text = "${position + 1}/${mediaExplorer.files.size}"
    }

    @SuppressLint("SetTextI18n")
    private fun showPagerPosition(isVisible: Boolean) {
        viewBinding?.mediaPagerPosition?.isVisible = isVisible
    }

    private fun hideShareForVideo(position: Int) {
        val ext = getExtension(mediaExplorer.files[position].fileExst)
        val shareVisible = ext == StringUtils.Extension.IMAGE
        mediaActivity?.shareButtonVisible = shareVisible
        mediaActivity?.shareVisible = shareVisible
    }

    val activeFragment: Fragment?
        get() = childFragmentManager.findFragmentByTag("f" + viewBinding?.mediaPager?.currentItem)

    fun isActiveFragment(fragment: Fragment): Boolean {
        return fragment == activeFragment
    }

    fun pageClicked(isPosition: Boolean) {
        val isVisible = mediaActivity?.toggleToolbar() == true
        if (isPosition) {
            showPagerPosition(isVisible)
        }
    }

    fun hidePosition() {
        if (viewBinding?.mediaPagerPosition?.isVisible == true) {
            showPagerPosition(true)
        }
    }

    private inner class PagerAdapter2(fragment: Fragment, val explorer: Explorer) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = explorer.files.size

        override fun createFragment(position: Int): Fragment {
            explorer.files.let { files ->
                if (files.size > 0) {
                    val file = files[position]
                    return when (getExtension(file.fileExst)) {
                        StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> {
                            MediaImageFragment.newInstance(file, arguments?.getBoolean(TAG_WEB_DAV)!!)
                        }
                        StringUtils.Extension.VIDEO_SUPPORT -> {
                            MediaVideoFragment.newInstance(file)
                        }
                        else -> {
                            throw RuntimeException("${file.fileExst} is unsupported format")
                        }
                    }
                }
            }
            throw RuntimeException(TAG + "getItem() - can't return null")
        }

    }

    private inner class PagerListener(private val explorer: Explorer) : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            Log.d(
                TAG, "Position: $position; Offset: $positionOffset; " +
                        "Position offset: $positionOffsetPixels"
            )
        }

        override fun onPageSelected(position: Int) {
            if (mediaExplorer.files.isNotEmpty()) {
                mediaActivity?.setToolbarTitle(mediaExplorer.files[position].title)
                setPagerPosition(position)
                hideShareForVideo(position)
                notifyFragmentsScroll(position)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                activeFragment?.let { notifyFragmentState(it) }
            }
        }

        private fun notifyFragmentScroll(fragment: Fragment, isActive: Boolean) {
            if (fragment is PagingViewPager.OnPagerListener) {
                (fragment as PagingViewPager.OnPagerListener).onPageScroll(isActive)
            }
        }

        private fun notifyFragmentState(fragment: Fragment) {
            if (fragment is PagingViewPager.OnPagerListener) {
                (fragment as PagingViewPager.OnPagerListener).onStartScroll()
            }
        }

        private fun notifyFragmentsScroll(position: Int) {
            activeFragment?.let { notifyFragmentScroll(it, true) }

            // Notify previous fragment
            if (explorer.count > 0 && position - 1 >= 0) {
                notifyFragmentScroll(
                    childFragmentManager.findFragmentByTag(
                        "f" + (viewBinding?.mediaPager?.currentItem?.minus(
                            1
                        ))
                    )!!, false
                )
            }

            // Notify next fragment
            if (position + 1 < explorer.count) {
                notifyFragmentScroll(
                    childFragmentManager.findFragmentByTag(
                        "f" + (viewBinding?.mediaPager?.currentItem?.plus(
                            1
                        ))
                    )!!, false
                )

            }
        }

    }

    companion object {
        val TAG: String = MediaPagerFragment::class.java.simpleName
        private const val TAG_POSITION = "TAG_POSITION"
        private const val TAG_MEDIA = "TAG_MEDIA"
        private const val TAG_WEB_DAV = "TAG_WEB_DAV"

        fun newInstance(explorer: Explorer?, isWebDAv: Boolean, position: Int): MediaPagerFragment =
            MediaPagerFragment().apply {
                arguments = Bundle(3).apply {
                    putInt(TAG_POSITION, position)
                    putSerializable(TAG_MEDIA, explorer)
                    putSerializable(TAG_WEB_DAV, isWebDAv)
                }
            }

    }
}