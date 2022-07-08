package app.editors.manager.ui.fragments.media

import android.Manifest
import android.accounts.Account
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.lifecycle.lifecycleScope
import app.documents.core.account.AccountDao
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentMediaImageBinding
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.fragments.media.MediaPagerFragment.OnMediaListener
import app.editors.manager.ui.views.custom.PlaceholderViews
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.tools.GlideTool
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils.OnUriListener
import lib.toolkit.base.managers.utils.ContentResolverUtils.getBitmapUriAsync
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import javax.inject.Inject

class MediaImageFragment : BaseAppFragment(), OnMediaListener, PlaceholderViews.OnClickListener, OnUriListener {

    @Inject
    lateinit var glideTool: GlideTool

    @Inject
    lateinit var accountsDao: AccountDao

    private var bitmap: Bitmap? = null
    private var gifDrawable: GifDrawable? = null
    private var asyncTaskShare: Job? = null
    private var isWebDav: Boolean = false
    private var image: CloudFile? = null
    private var placeholderViews: PlaceholderViews? = null
    private var viewBinding: FragmentMediaImageBinding? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMediaImageBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        tag?.let {
            if (TAG_DIALOG_SHARE == it) {
                cancelSharing()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        viewBinding?.mediaImageView?.let { photoView ->
            return if (isActivePage && photoView.minimumScale < photoView.scale) {
                photoView.setScale(photoView.minimumScale, true)
                true
            } else {
                super.onBackPressed()
            }
        } ?: run {
            return super.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelSharing()
        glideTool.clear(gifTarget)
        glideTool.clear(bitmapTarget)
    }

    override fun onPageScroll(isActive: Boolean) {
        if (isActive) {
            gifDrawable?.start()
        }
    }

    override fun onStartScroll() {
        // Stub
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun onShareClick() {
        bitmap?.let {
            cancelSharing()
            showWaitingDialog(null, getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_SHARE)
            asyncTaskShare = getBitmapUriAsync(
                requireContext(), it, this,
                StringUtils.getNameWithoutExtension(image?.title ?: ""),
                lifecycleScope
            )
        }
    }

    override fun onRetryClick() {
        setImageState()
        loadImage()
    }

    override fun onGetUri(uri: Uri) {
        hideDialog()
        showFileShareActivity(uri)
    }

    private fun init() {
        getArgs()
        initViews()
        loadImage()
    }

    private fun getArgs() {
        arguments?.let {
            image = it.getSerializable(TAG_IMAGE) as CloudFile
            isWebDav = it.getBoolean(TAG_WEB_DAV)
        }
    }

    private fun initViews() {
        placeholderViews = PlaceholderViews(viewBinding?.placeholderLayout?.root).apply {
            setViewForHide(viewBinding?.mediaImageLayout)
            setOnClickListener(this@MediaImageFragment)
        }
        viewBinding?.let {
            it.mediaImageView.maximumScale = SCALE_MAX
            it.mediaImageView.minimumScale = SCALE_MIN
            it.mediaImageContainer.setOnClickListener { pageClicked() }
            it.mediaImageView.setOnClickListener { pageClicked() }
            UiUtils.setColorFilter(
                requireContext(),
                it.mediaImageProgress.indeterminateDrawable,
                lib.toolkit.base.R.color.colorSecondary)
        }
        setImageState()
    }

    private fun setImageState() {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
        viewBinding?.mediaImageView?.isVisible = false
        viewBinding?.mediaImageProgress?.isVisible = true
    }

    private fun setImageBitmap(bitmap: Bitmap?) {
        viewBinding?.mediaImageView?.let {
            it.setImageBitmap(bitmap)
            setImageViewReady()
        }
    }

    private fun setImageDrawable(gifDrawable: GifDrawable?) {
        viewBinding?.mediaImageView?.let {
            it.setImageDrawable(gifDrawable)
            setImageViewReady()
        }
    }

    private fun setImageViewReady() {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
        viewBinding?.mediaImageProgress?.isVisible = false
        viewBinding?.mediaImageView?.apply {
            visibility = View.VISIBLE
            alpha = ALPHA_FROM
            scale = SCALE_MIN
            animate().alpha(ALPHA_TO).setDuration(ALPHA_DELAY.toLong()).start()
        }
    }

    private fun loadImage() {
        when {
            isWebDav -> {
                loadWebDavImage()
            }
            image?.id == "" -> {
                loadLocalImage()
            }
            else -> {
                loadCloudImage()
            }
        }
    }

    private fun loadCloudImage() {
        lifecycleScope.launch {
            accountsDao.getAccountOnline()?.let { account ->
                AccountUtils.getToken(
                    requireContext(),
                    Account(account.getAccountName(), getString(lib.toolkit.base.R.string.account_type))
                )?.let { token ->
                    val url = GlideUtils.getCorrectLoad(image?.viewUrl!!, token)
                    withContext(Dispatchers.Main) {
                        if (StringUtils.isImageGif(image?.fileExst!!)) {
                            glideTool.loadGif(gifTarget, url, false, gifRequestListener)
                        } else {
                            glideTool.load(bitmapTarget, url, false, bitmapRequestListener)
                        }
                    }
                }
            } ?: run {
                Log.d(TAG, "loadCloudImage: ")
            }
        }

    }

    private fun loadWebDavImage() {
        lifecycleScope.launch {
            accountsDao.getAccountOnline()?.let { account ->
                AccountUtils.getPassword(
                    requireContext(),
                    Account(account.getAccountName(), getString(lib.toolkit.base.R.string.account_type))
                )?.let { password ->
                    val url = GlideUtils.getWebDavUrl(image?.id!!, account, password)
                    withContext(Dispatchers.Main) {
                        if (StringUtils.isImageGif(image?.fileExst!!)) {
                            glideTool.loadGif(gifTarget, url, false, gifRequestListener)
                        } else {
                            glideTool.load(bitmapTarget, url, false, bitmapRequestListener)
                        }
                    }
                }
            } ?: run {
                Log.d(TAG, "loadWebDavImage: ")
            }
        }
    }

    private fun loadLocalImage() {
        if (StringUtils.isImageGif(image?.fileExst!!)) {
            glideTool.loadGif(gifTarget, image?.webUrl!!, false, gifRequestListener)
        } else {
            glideTool.load(bitmapTarget, image?.webUrl!!, false, bitmapRequestListener)
        }
    }

    private fun cancelSharing() {
        asyncTaskShare?.cancel()
    }

    private val isActivePage: Boolean
        get() {
            val fragment = parentFragment
            return if (fragment is MediaPagerFragment) {
                fragment.isActiveFragment(this)
            } else true
        }

    private fun pageClicked() {
        val fragment = parentFragment
        if (fragment is MediaPagerFragment) {
            fragment.pageClicked(true)
        }
    }

    /*
     * Glide callbacks
     * */
    private val gifRequestListener: RequestListener<GifDrawable> = object : RequestListener<GifDrawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<GifDrawable>,
            isFirstResource: Boolean
        ): Boolean {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.MEDIA)
            return false
        }

        override fun onResourceReady(
            resource: GifDrawable,
            model: Any,
            target: Target<GifDrawable>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            gifDrawable = resource
            resource.start()
            setImageDrawable(gifDrawable)
            return false
        }
    }
    private val gifTarget: CustomTarget<GifDrawable> = object : CustomTarget<GifDrawable>() {

        override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
            Log.d(TAG, this.javaClass.simpleName + " - onResourceReady()")
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            // stub
        }
    }

    private val bitmapRequestListener: RequestListener<Bitmap> = object : RequestListener<Bitmap> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Bitmap?>,
            isFirstResource: Boolean
        ): Boolean {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.MEDIA)
            return false
        }

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any,
            target: Target<Bitmap?>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            bitmap = resource
            setImageBitmap(bitmap)
            return false
        }
    }

    private val bitmapTarget: CustomTarget<Bitmap> = object : CustomTarget<Bitmap>() {

        override fun onLoadCleared(placeholder: Drawable?) {
            //stub
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            Log.d(TAG, this.javaClass.simpleName + " - onResourceReady()")
        }
    }


    companion object {
        val TAG: String = MediaImageFragment::class.java.simpleName

        private const val TAG_DIALOG_SHARE = "TAG_DIALOG_SHARE"
        private const val TAG_IMAGE = "TAG_IMAGE"
        private const val TAG_WEB_DAV = "TAG_WEB_DAV"

        private const val ALPHA_DELAY = 500
        private const val ALPHA_FROM = 0.0f
        private const val ALPHA_TO = 1.0f
        private const val SCALE_MAX = 5.0f
        private const val SCALE_MIN = 1.0f

        fun newInstance(image: CloudFile, isWebDav: Boolean): MediaImageFragment {
            return MediaImageFragment().apply {
                arguments = Bundle(2).apply {
                    putSerializable(TAG_IMAGE, image)
                    putBoolean(TAG_WEB_DAV, isWebDav)
                }
            }
        }
    }
}