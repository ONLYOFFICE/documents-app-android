package app.editors.manager.managers.receivers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public abstract class BaseReceiver<T> extends BroadcastReceiver {

    public interface OnReceiveListener<T> {
        void onReceive(T value);
    }

    protected OnReceiveListener<T> mOnReceiveListener;

    public void setOnReceiveListener(OnReceiveListener<T> onReceiveListener) {
        mOnReceiveListener = onReceiveListener;
    }

    public abstract IntentFilter getFilter();

}
