package app.editors.manager.ui.adapters

import android.accounts.Account
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.storage.account.AccountDao
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.tools.CacheTool
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.ui.adapters.base.BaseAdapter
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.tools.GlideTool
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.UiUtils.setImageTint
import lib.toolkit.base.managers.utils.UiUtils.setLayoutParams
import javax.inject.Inject

class MediaAdapter(cellSize: Int, private val scope: CoroutineScope) : BaseAdapter<CloudFile?>() {

    companion object {
        private const val ALPHA_DELAY = 500
        private const val ALPHA_FROM = 0.0f
        private const val ALPHA_TO = 1.0f
    }


    @Inject
    lateinit var context: Context

    @Inject
    lateinit var glideTool: GlideTool

    @Inject
    lateinit var cacheTool: CacheTool

    @Inject
    lateinit var accountDao: AccountDao

    private var cellSize: Int

    init {
        App.getApp().appComponent.inject(this)
        this.cellSize = cellSize
    }

    private val token = runBlocking(Dispatchers.Default) {
        accountDao.getAccountOnline()?.let { account ->
            AccountUtils.getToken(context, Account(account.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type)))
                ?.let { token ->
                    return@runBlocking token
                }
        } ?: run {
            return@runBlocking ""
        }
    }

    private val headers: Map<String, String> by lazy {
        mapOf(
            ApiContract.HEADER_AUTHORIZATION to token,
            ApiContract.HEADER_ACCEPT to ApiContract.VALUE_ACCEPT
        )
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, typeHolder: Int): RecyclerView.ViewHolder {
        return when (typeHolder) {
            TYPE_ITEM_ONE -> {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_media_image, viewGroup, false)
                ViewHolderImage(view)
            }
            TYPE_ITEM_TWO -> {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_media_video, viewGroup, false)
                ViewHolderVideo(view)
            }
            else -> throw RuntimeException("Unknown type is unacceptable: $typeHolder")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val file = getItem(position)
        when (getExtension(position)) {
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> {
                val mViewHolder = viewHolder as ViewHolderImage
                mViewHolder.bind(file)
            }
            StringUtils.Extension.VIDEO_SUPPORT -> {
                val mViewHolder = viewHolder as ViewHolderVideo
                mViewHolder.bind(file)
            }
            else -> {
                // Stub
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getExtension(position)) {
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> TYPE_ITEM_ONE
            StringUtils.Extension.VIDEO_SUPPORT -> TYPE_ITEM_TWO
            else -> TYPE_UNKNOWN
        }
    }

    private fun getExtension(position: Int): StringUtils.Extension {
        val file = getItem(position)
        val ext = file!!.fileExst
        return getExtension(ext)
    }

    fun setCellSize(cellSize: Int) {
        this.cellSize = cellSize
    }

    inner class ViewHolderImage internal constructor(val view: View) : RecyclerView.ViewHolder(view) {

        private val imageLayout: FrameLayout = view.findViewById(R.id.media_image_layout)
        private val imageView: ImageView = view.findViewById(R.id.media_image_view)
        private val progressBar: ProgressBar = view.findViewById(R.id.media_image_progress)

        private val requestListener: RequestListener<Bitmap> = object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap?>,
                isFirstResource: Boolean
            ): Boolean {
                setLayoutParams(
                    imageView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setImageTint(imageView, R.drawable.ic_media_error, lib.toolkit.base.R.color.colorTextSecondary)
                showImage()
                return true
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap?>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                imageView.setImageBitmap(resource)
                showImage()
                return true
            }
        }

        init {
            setLayoutParams(imageLayout, cellSize, cellSize)

            imageView.isClickable = false
            UiUtils.setColorFilter(progressBar.context, progressBar.indeterminateDrawable, lib.toolkit.base.R.color.colorSecondary)

            view.setOnClickListener { v: View? ->
                mOnItemClickListener?.onItemClick(v, layoutPosition)
            }
        }

        fun bind(file: CloudFile?) {
            scope.launch {
                accountDao.getAccountOnline()?.let { account ->
                    when {
                        account.isWebDav && file?.id != "" -> {
                            loadWebDav(file, account)
                        }
                        file?.id == "" -> {
                            loadLocal(file)
                        }
                        else -> {
                            AccountUtils.getToken(
                                view.context,
                                Account(account.getAccountName(), view.context.getString(lib.toolkit.base.R.string.account_type))
                            )?.let { token ->
                                loadCloud(file, token)
                            }
                        }
                    }
                } ?: run {
                    if (file?.id == "") {
                        loadLocal(file)
                    }
                }
            }

        }

        private suspend fun loadCloud(file: CloudFile?, token: String) {
            val url = GlideUtils.getCorrectLoad(file?.viewUrl ?: "", token)
            withContext(Dispatchers.Main) {
                glideTool.load(imageView, url, false, Point(cellSize, cellSize), requestListener)
            }
        }

        private suspend fun loadLocal(file: CloudFile) {
            withContext(Dispatchers.Main) {
                glideTool.load(imageView, file.webUrl, false, Point(cellSize, cellSize), requestListener)
            }
        }

        private suspend fun loadWebDav(file: CloudFile?, account: CloudAccount) {
            AccountUtils.getPassword(
                view.context,
                Account(account.getAccountName(), view.context.getString(lib.toolkit.base.R.string.account_type))
            )?.let { pass ->
                val url = GlideUtils.getWebDavUrl(file?.id!!, account, pass)
                withContext(Dispatchers.Main) {
                    glideTool.load(imageView, url, false, Point(cellSize, cellSize), requestListener)
                }
            }
        }

        private fun showImage() {
            progressBar.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            imageView.alpha = ALPHA_FROM
            imageView.animate().alpha(ALPHA_TO).setDuration(ALPHA_DELAY.toLong()).start()
        }
    }

    inner class ViewHolderVideo internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        private val videoLayout: FrameLayout = view.findViewById(R.id.media_video_layout)
        private val videoView: ImageView= view.findViewById(R.id.media_video_view)

        init {
            setLayoutParams(videoLayout, cellSize, cellSize)
            videoView.isClickable = false
            view.setOnClickListener { v: View? ->
                mOnItemClickListener?.onItemClick(v, layoutPosition)
            }
        }

        fun bind(file: CloudFile?) {
            if (file!!.id == "") {
                cacheTool.getBitmap(file.webUrl) { bitmap: Bitmap? ->
                    if (bitmap == null) {
                        val b = ThumbnailUtils.createVideoThumbnail(file.webUrl, MediaStore.Images.Thumbnails.MINI_KIND)
                        videoView.setImageBitmap(b)
                        cacheTool.addBitmap(file.webUrl, checkNotNull(b), null)
                    } else {
                        videoView.setImageBitmap(bitmap)
                    }
                    showVideo()
                }
            } else {
                cacheTool.getBitmap(file.viewUrl) { bitmap: Bitmap? -> getFrame(bitmap, file.viewUrl) }
            }
        }

        private fun getFrame(bitmap: Bitmap?, url: String) {
            if (bitmap == null) {
                // TODO fix queue
                ContentResolverUtils.getFrameFromWebVideoAsync(url, headers, Point(cellSize, cellSize),
                    object : ContentResolverUtils.OnWebVideoListener {
                        override fun onGetFrame(bitmap: Bitmap) {
                            videoView.setImageBitmap(bitmap)
                            cacheTool.addBitmap(url, bitmap, null)
                            showVideo()
                        }

                    }, scope)
            } else {
                videoView.setImageBitmap(bitmap)
                showVideo()
            }
        }

        private fun showVideo() {
            videoView.visibility = View.VISIBLE
            videoView.alpha = ALPHA_FROM
            videoView.animate()?.alpha(ALPHA_TO)?.setDuration(ALPHA_DELAY.toLong())?.start()
        }

    }

}