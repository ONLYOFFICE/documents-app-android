package app.editors.manager.ui.fragments.media

import android.accounts.Account
import android.content.res.Configuration
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import app.documents.core.account.AccountDao
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentMediaVideoBinding
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.fragments.media.MediaPagerFragment.OnMediaListener
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.media.MediaVideoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.UiUtils
import java.io.File
import javax.inject.Inject

class MediaVideoFragment : BaseAppFragment(), MediaPlayer.OnErrorListener, OnPreparedListener, OnCompletionListener,
    OnMediaListener, MediaVideoView.OnMediaVideoListener, PlaceholderViews.OnClickListener {

    companion object {
        val TAG: String = MediaVideoFragment::class.java.simpleName

        private const val TAG_VIDEO = "TAG_VIDEO"
        private const val TIME_MEDIA_DURATION = 3000
        private const val TIME_START = 0
        private const val TIME_PREVIEW = 1

        fun newInstance(video: CloudFile): MediaVideoFragment {
            return MediaVideoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TAG_VIDEO, video)
                }
            }
        }
    }

    @Inject
    lateinit var accountDao: AccountDao

    init {
        App.getApp().appComponent.inject(this)
    }

    private lateinit var placeholderViews: PlaceholderViews
    private lateinit var videoFile: CloudFile

    private var videoUri: Uri? = null
    private var mediaController: MediaController? = null
    private var isPrepared = false
    private var isError = false

    private var viewBinding: FragmentMediaVideoBinding? = null

    private val headers: Map<String, String?> by lazy {
        mapOf(
            ApiContract.HEADER_AUTHORIZATION to runBlocking(Dispatchers.Default) {
                accountDao.getAccountOnline()?.let { account ->
                    AccountUtils.getToken(
                        App.getApp().applicationContext,
                        Account(account.getAccountName(), App.getApp().applicationContext.getString(lib.toolkit.base.R.string.account_type))
                    )
                        ?.let {
                            return@runBlocking it
                        }
                } ?: run {
                    throw Error("No account")
                }
            },
            ApiContract.HEADER_ACCEPT to ApiContract.VALUE_ACCEPT
        )
    }

    private val wrapperListener: (id: Int) -> Unit = { id ->
        when (id) {
            R.id.media_video_container -> pageClicked(true)
            R.id.media_video_wrapper -> if (viewBinding?.mediaVideoView?.isPlaying == true || isPrepared) {
                pageClicked(false)
                mediaController?.show(TIME_MEDIA_DURATION)
            } else {
                pageClicked(true)
            }
            R.id.view_icon_background_layout -> if (isPrepared) {
                if (viewBinding?.mediaVideoView?.isPlaying == true) {
                    mediaController?.show(TIME_MEDIA_DURATION)
                } else {
                    viewBinding?.mediaVideoView?.start()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMediaVideoBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()
        startPrepareVideo()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateVideoLayoutParams()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding?.mediaVideoView?.stopPlayback()
        viewBinding = null
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        isPrepared = true
        placeholderViews.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
        viewBinding?.mediaVideoProgress?.visibility = View.GONE
        viewBinding?.viewIconBackgroundLayout?.root?.visibility = View.VISIBLE
        viewBinding?.viewIconBackgroundLayout?.root?.setBackgroundResource(R.drawable.drawable_media_background_video_play_grey)
        viewBinding?.mediaVideoView?.seekTo(TIME_PREVIEW)
        viewBinding?.mediaVideoView?.pause()
        hidePosition()
        updateVideoLayoutParams()
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (!isError) {
            viewBinding?.viewIconBackgroundLayout?.root?.visibility = View.VISIBLE
            viewBinding?.mediaVideoView?.seekTo(TIME_START)
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        isError = true
        placeholderViews.setTemplatePlaceholder(PlaceholderViews.Type.MEDIA)
        showSnackBar(getString(R.string.media_video_error, what.toString()))
        return false
    }

    override fun onMediaStart() {
        setStateStart()
    }

    override fun onMediaPause() {
        setStatePause()
    }

    override fun onMediaSuspend() {
        isPrepared = false
    }

    override fun onMediaResume() {
        // Stub
    }

    override fun onPageScroll(isActive: Boolean) {
        if (isActive) {
            startPrepareVideo()
            if (isPrepared) {
                hidePosition()
            }
        } else {
            stopPrepareVideo()
        }
    }

    override fun onStartScroll() {
        mediaController!!.hide()
    }

    override fun onShareClick() {
        // Stub
    }

    override fun onRetryClick() {
        setVideoInitState()
        startPrepareVideo()
    }

    private fun init() {
        getArgs()
        initViews()
        setListeners()
        restoreViews()
    }

    private fun getArgs() {
        videoFile = arguments?.getSerializable(TAG_VIDEO) as CloudFile
        videoUri = if (videoFile.id == "") {
            Uri.fromFile(File(videoFile.webUrl))
        } else {
            Uri.parse(videoFile.viewUrl)
        }
    }

    private fun initViews() {
        placeholderViews = PlaceholderViews(viewBinding?.placeholderLayout?.root)
        placeholderViews.setViewForHide(viewBinding?.mediaVideoContainer)
        placeholderViews.setOnClickListener(this)
        viewBinding?.viewIconBackgroundLayout?.root?.setBackgroundResource(R.drawable.drawable_media_background_video_play_light)
        UiUtils.setImageTint(
            viewBinding?.viewIconBackgroundLayout?.viewIconBackgroundImage!!,
            R.drawable.ic_media_play,
            lib.toolkit.base.R.color.colorPrimary
        )
        mediaController = MediaController(context)
        mediaController!!.setMediaPlayer(viewBinding?.mediaVideoView)
        mediaController!!.setAnchorView(viewBinding?.mediaVideoView)
        viewBinding?.mediaVideoView?.setOnErrorListener(this)
        viewBinding?.mediaVideoView?.setOnPreparedListener(this)
        viewBinding?.mediaVideoView?.setOnCompletionListener(this)
        viewBinding?.mediaVideoView?.setOnMediaVideoListener(this)
        viewBinding?.mediaVideoView?.setMediaController(mediaController)
        setVideoInitState()
    }



    private fun setListeners() {
        viewBinding?.mediaVideoContainer?.setOnClickListener { wrapperListener(it.id) }
        viewBinding?.mediaVideoWrapper?.setOnClickListener { wrapperListener(it.id) }
        viewBinding?.viewIconBackgroundLayout?.root?.setOnClickListener { wrapperListener(it.id) }
    }

    private fun setVideoInitState() {
        placeholderViews.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
        viewBinding?.mediaVideoProgress?.visibility = View.VISIBLE
        viewBinding?.viewIconBackgroundLayout?.root?.visibility = View.GONE
    }

    private fun restoreViews() {
        isPrepared = false
        isError = false
    }

    private fun updateVideoLayoutParams() {
        val params = viewBinding?.mediaVideoView?.layoutParams as FrameLayout.LayoutParams
        if (isLandscape) {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        viewBinding?.mediaVideoView?.layoutParams = params
    }

    private fun startPrepareVideo() {
        viewBinding?.mediaVideoView?.let { view ->
            if (isActivePage && videoUri != null) {
                if (!isPrepared) {
                    if (videoFile.id != "") {
                        view.setVideoURI(videoUri, headers)
                    } else {
                        view.setVideoURI(videoUri)
                    }
                    isError = false
                }
            }
        }

    }

    private fun stopPrepareVideo() {
        viewBinding?.mediaVideoView?.let { view ->
            if (mediaController != null) {
                if (isPrepared) {
                    view.pause()
                } else {
                    view.stopPlayback()
                }
            }
        }
    }


    private fun setStatePause() {
        mediaController?.hide()
        viewBinding?.viewIconBackgroundLayout?.root?.visibility = View.VISIBLE
    }

    private fun setStateStart() {
        mediaController!!.show(TIME_MEDIA_DURATION)
        viewBinding?.viewIconBackgroundLayout?.root?.visibility = View.GONE
    }

    private val isActivePage: Boolean
        get() {
            val fragment = parentFragment
            return if (fragment is MediaPagerFragment) {
                fragment.isActiveFragment(this)
            } else true
        }

    private fun pageClicked(isPosition: Boolean) {
        val fragment = parentFragment
        if (fragment is MediaPagerFragment) {
            fragment.pageClicked(isPosition)
        }
    }

    private fun hidePosition() {
        val fragment = parentFragment
        if (fragment is MediaPagerFragment) {
            fragment.hidePosition()
        }
    }

}