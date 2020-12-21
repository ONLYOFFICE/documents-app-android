package app.editors.manager.ui.views.media;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class MediaVideoView extends VideoView {

    public interface OnMediaVideoListener {
        void onMediaStart();
        void onMediaPause();
        void onMediaSuspend();
        void onMediaResume();
    }

    protected OnMediaVideoListener mOnMediaVideoListener;

    public MediaVideoView(Context context) {
        super(context);
    }

    public MediaVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MediaVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void start() {
        super.start();
        if (mOnMediaVideoListener != null) {
            mOnMediaVideoListener.onMediaStart();
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (mOnMediaVideoListener != null) {
            mOnMediaVideoListener.onMediaPause();
        }
    }

    @Override
    public void suspend() {
        super.suspend();
        if (mOnMediaVideoListener != null) {
            mOnMediaVideoListener.onMediaSuspend();
        }
    }

    @Override
    public void resume() {
        super.resume();
        if (mOnMediaVideoListener != null) {
            mOnMediaVideoListener.onMediaResume();
        }
    }

    public void setOnMediaVideoListener(final OnMediaVideoListener onMediaVideoListener) {
        mOnMediaVideoListener = onMediaVideoListener;
    }

}
