package app.editors.manager.ui.fragments.media

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import app.editors.manager.R
import app.editors.manager.databinding.FragmentMediaPagerBinding
import app.editors.manager.databinding.IncludeMediaHeaderPagerBinding
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.pager.PagingViewPager
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.isImageGif

class MediaPagerFragment : BaseAppFragment() {

    interface OnMediaListener : PagingViewPager.OnPagerListener {
        fun onShareClick()
    }

    private var toolbarView: View? = null
    private var mediaActivity: MediaActivity? = null
    private var toolbarViewHolder: ToolbarViewHolder? = null
    private var pagerAdapter: PagerAdapter? = null
    private var selectedPosition = 0
    private var mediaExplorer: Explorer? = null
    private var isWebDav = false
    private var viewBinding: FragmentMediaPagerBinding? = null

    private val writePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            shareFile()
        } else {
            showSnackBar("Not permission")
        }
    }

    private val pagerPositionRunnableGone = Runnable {
        viewBinding?.mediaPagerPosition?.isVisible = false
    }

    private val pagerPositionRunnableVisible = Runnable {
        viewBinding?.mediaPagerPosition?.isVisible = true
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
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding?.mediaPagerPosition?.removeCallbacks(pagerPositionRunnableGone)
        viewBinding = null
    }

    private fun init() {
        getArgs()
        initViews()
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            mediaExplorer = bundle.getSerializable(TAG_MEDIA) as Explorer?
            isWebDav = bundle.getBoolean(TAG_WEB_DAV)
        }
    }

    @SuppressLint("InflateParams")
    private fun initViews() {
        mediaActivity?.setToolbarState(true)
        toolbarView = layoutInflater.inflate(R.layout.include_media_header_pager, null)
        toolbarViewHolder = ToolbarViewHolder(toolbarView)
        mediaActivity?.setToolbarView(toolbarView)
        pagerAdapter = PagerAdapter(childFragmentManager)
        pagerAdapter?.onPageSelected(clickedPosition)
        viewBinding?.let {
            it.mediaPager.adapter = pagerAdapter
            it.mediaPager.addOnPageChangeListener(pagerAdapter!!)
            it.mediaPager.currentItem = clickedPosition
        }
    }

    private val clickedPosition: Int
        get() {
            mediaExplorer?.files?.forEachIndexed { index, file ->
                if (file.isClicked) {
                    return index
                }
            }
            return 0
        }

    @SuppressLint("MissingPermission")
    private fun shareFile() {
        (activeFragment as OnMediaListener).onShareClick()
    }

    @SuppressLint("SetTextI18n")
    private fun setPagerPosition(position: Int) {
        viewBinding?.mediaPagerPosition?.text = "${position + 1}/${mediaExplorer?.files?.size}"
    }

    @SuppressLint("SetTextI18n")
    private fun showPagerPosition(isVisible: Boolean) {
        viewBinding?.mediaPagerPosition?.let {
            it.removeCallbacks(pagerPositionRunnableGone)
            it.removeCallbacks(pagerPositionRunnableVisible)
            it.text = "${selectedPosition + 1}/${mediaExplorer?.files?.size}"
            it.isVisible = true
            it.alpha = if (isVisible) MediaActivity.ALPHA_TO else MediaActivity.ALPHA_FROM
            it.animate()
                .alpha(if (isVisible) MediaActivity.ALPHA_FROM else MediaActivity.ALPHA_TO)
                .setDuration(MediaActivity.ALPHA_DELAY.toLong())
                .withEndAction(if (isVisible) pagerPositionRunnableGone else pagerPositionRunnableVisible)
                .start()
        }
    }

    val activeFragment: Fragment
        get() = pagerAdapter?.instantiateItem(viewBinding?.mediaPager!!, selectedPosition) as Fragment

    fun isActiveFragment(fragment: Fragment): Boolean {
        return fragment == activeFragment
    }

    fun pageClicked(isPosition: Boolean) {
        val isVisible = mediaActivity?.showToolbar() == true
        if (isPosition) {
            showPagerPosition(isVisible)
        }
    }

    fun hidePosition() {
        if (viewBinding?.mediaPagerPosition?.isVisible == true) {
            showPagerPosition(true)
        }
    }

    fun showPosition() {
        if (viewBinding?.mediaPagerPosition?.isVisible == false) {
            showPagerPosition(false)
        }
    }

    /*
     * Pager adapter
     * */
    private inner class PagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(
        fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ), OnPageChangeListener {

        override fun getItem(position: Int): Fragment {
            mediaExplorer?.files?.let { files ->
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

        override fun getCount() =
            mediaExplorer?.files.let { if (it?.isNotEmpty() == true) it.size else 0 }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            Log.d(TAG, "Position: $position; Offset: $positionOffset; " +
                    "Position offset: $positionOffsetPixels")
        }

        override fun onPageSelected(position: Int) {
            if (mediaExplorer?.files?.isNotEmpty() == true) {
                selectedPosition = position
                toolbarViewHolder?.headerNameText?.text =
                    mediaExplorer?.files?.let { it[position].title }
                setPagerPosition(position)
                hideShareForVideo()
                notifyFragmentsScroll(selectedPosition)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                notifyFragmentState(activeFragment)
            }
        }

        private fun hideShareForVideo() {
            val ext = mediaExplorer?.files?.let { it[selectedPosition].fileExst }.toString()
            if (activeFragment is MediaVideoFragment || isImageGif(ext)) {
                toolbarViewHolder?.shareImageButton?.isVisible = false
            } else {
                toolbarViewHolder?.shareImageButton?.isVisible = true
                if (mediaActivity?.isToolbarVisible == true) {
                    showPosition()
                }
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
            notifyFragmentScroll(activeFragment, true)

            // Notify previous fragment
            if (count > 0 && position - 1 >= 0) {
                notifyFragmentScroll(instantiateItem(viewBinding?.mediaPager!!,
                    position - 1) as Fragment, false)
            }

            // Notify next fragment
            if (position + 1 < count) {
                notifyFragmentScroll(instantiateItem(viewBinding?.mediaPager!!,
                    position + 1) as Fragment, false)
            }
        }
    }

    /*
     * Custom view for toolbar
     * */
    private inner class ToolbarViewHolder(view: View?) {
        private var viewBinding = IncludeMediaHeaderPagerBinding.bind(checkNotNull(view)).apply {
            mediaPagerHeaderShare.setOnClickListener {
                writePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            mediaPagerHeaderViewMode.setOnClickListener {
                showFragment(MediaListFragment.newInstance(mediaExplorer, isWebDav), null, false)
            }
        }

        var headerNameText = viewBinding.mediaPagerHeaderName
        var shareImageButton = viewBinding.mediaPagerHeaderShare
    }

    companion object {
        val TAG: String = MediaPagerFragment::class.java.simpleName
        private const val TAG_MEDIA = "TAG_MEDIA"
        private const val TAG_WEB_DAV = "TAG_WEB_DAV"

        fun newInstance(explorer: Explorer?, mIsWebDAv: Boolean): MediaPagerFragment =
            MediaPagerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TAG_MEDIA, explorer)
                    putSerializable(TAG_WEB_DAV, mIsWebDAv)
                }
            }

    }
}