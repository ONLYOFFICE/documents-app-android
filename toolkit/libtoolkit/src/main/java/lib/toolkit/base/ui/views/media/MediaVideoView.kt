package lib.toolkit.base.ui.views.media

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class MediaVideoView : VideoView {

    protected var mOnMediaVideoListener: OnMediaVideoListener? = null

    interface OnMediaVideoListener {
        fun onMediaStart()
        fun onMediaPause()
        fun onMediaSuspend()
        fun onMediaResume()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun start() {
        super.start()
        mOnMediaVideoListener?.onMediaStart()
    }

    override fun pause() {
        super.pause()
        mOnMediaVideoListener?.onMediaPause()
    }

    override fun suspend() {
        super.suspend()
        mOnMediaVideoListener?.onMediaSuspend()
    }

    override fun resume() {
        super.resume()
        mOnMediaVideoListener?.onMediaResume()
    }

    fun setOnMediaVideoListener(onMediaVideoListener: OnMediaVideoListener) {
        mOnMediaVideoListener = onMediaVideoListener
    }

}
