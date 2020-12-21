package app.editors.manager.managers.tools;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.WeakAsyncUtils;

public class CacheTool extends lib.toolkit.base.managers.tools.CacheTool {

    public interface OnCacheWriteListener {
        void onCacheWrite(boolean isSuccess);
    }

    public interface OnCacheReadBitmapListener {
        void onCacheReadBitmap(@Nullable Bitmap bitmap);
    }

    public CacheTool(Context context) {
        super(context);
    }

    public WeakAsyncUtils addBitmap(@NonNull final String key, @NonNull final Bitmap bitmap, @Nullable final OnCacheWriteListener onCacheWriteListener) {
        if (key != null && bitmap != null) {
            final String keyForAsync = OnCacheWriteListener.class.getSimpleName() + "_" + key;
            final WeakAsyncUtils<Void, Void, Boolean, OnCacheWriteListener> asyncTask = new WeakAsyncUtils<Void, Void, Boolean, OnCacheWriteListener>(keyForAsync) {

                @Override
                protected Boolean doInBackground(Void... voids) {
                    final String newKey = StringUtils.getMd5(key);
                    addToMemoryCache(newKey, bitmap);
                    return addBytesToStorageCache(newKey, FileUtils.bitmapToBytes(bitmap));
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    if (mWeakReference != null) {
                        final OnCacheWriteListener listener = mWeakReference.get();
                        if (listener != null) {
                            listener.onCacheWrite(result);
                        }
                    }
                }
            };

            asyncTask.setWeakReference(onCacheWriteListener);
            asyncTask.execute(false);
            return asyncTask;
        }

        return null;
    }

    @Nullable
    public WeakAsyncUtils getBitmap(@NonNull final String key,  @NonNull final OnCacheReadBitmapListener onCacheReadBitmapListener) {
        if (key != null) {
            final String keyForAsync = OnCacheReadBitmapListener.class.getSimpleName() + "_" + key;
            final WeakAsyncUtils<Void, Void, Bitmap, OnCacheReadBitmapListener> asyncTask = new WeakAsyncUtils<Void, Void, Bitmap, OnCacheReadBitmapListener>(keyForAsync) {

                @Override
                protected Bitmap doInBackground(Void... voids) {
                    final String newKey = StringUtils.getMd5(key);

                    // First, try get cache from memory
                    final Object object = getFromMemoryCache(newKey);
                    if (object instanceof Bitmap) {
                        return (Bitmap) object;
                    }

                    // Else, try get cache from storage
                    final byte[] bytes = getBytesFromStorageCache(newKey);
                    if (bytes != null) {
                        final Bitmap bitmap = FileUtils.bytesToBitmap(bytes);
                        addToMemoryCache(key, bitmap);
                        return bitmap;
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    super.onPostExecute(result);
                    if (mWeakReference != null) {
                        final OnCacheReadBitmapListener listener = mWeakReference.get();
                        if (listener != null) {
                            listener.onCacheReadBitmap(result);
                        }
                    }
                }
            };

            asyncTask.setWeakReference(onCacheReadBitmapListener);
            asyncTask.execute(false);
            return asyncTask;
        }

        return null;
    }

}
