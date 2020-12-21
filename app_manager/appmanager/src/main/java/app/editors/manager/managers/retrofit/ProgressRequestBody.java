package app.editors.manager.managers.retrofit;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import lib.toolkit.base.managers.utils.ContentResolverUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface OnUploadCallbacks {
        void onProgressUpdate(long total, long progress);
    }

    private volatile boolean mIsCanceled;
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final Uri mUri;
    private final String mContentType;
    private final long mTotalSize;
    private OnUploadCallbacks mOnUploadCallbacks;

    public ProgressRequestBody(final Context context, final Uri uri) {
        mIsCanceled = false;
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mUri = uri;
        mTotalSize = ContentResolverUtils.getSize(mContext, mUri);
        mContentType = ContentResolverUtils.getMimeType(mContext, mUri);
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(mContentType);
    }

    @Override
    public long contentLength() {
        return mTotalSize;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        try (InputStream inputStream = mContentResolver.openInputStream(mUri)) {
            int countBytes;
            long totalBytes = 0;
            while ((countBytes = inputStream.read(buffer)) != -1 && !mIsCanceled) {
                totalBytes += countBytes;
                sink.write(buffer, 0, countBytes);
                if (mOnUploadCallbacks != null) {
                    mOnUploadCallbacks.onProgressUpdate(mTotalSize, totalBytes);
                }
            }
        }
    }

    public void cancel() {
        mIsCanceled = true;
    }

    public void setOnUploadCallbacks(OnUploadCallbacks onUploadCallbacks) {
        mOnUploadCallbacks = onUploadCallbacks;
    }

}