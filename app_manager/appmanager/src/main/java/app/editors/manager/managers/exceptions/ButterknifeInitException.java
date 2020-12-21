package app.editors.manager.managers.exceptions;


import android.os.Build;

import androidx.annotation.RequiresApi;

public class ButterknifeInitException extends RuntimeException {

    public ButterknifeInitException() {
        super();
    }

    public ButterknifeInitException(String message) {
        super(message);
    }

    public ButterknifeInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ButterknifeInitException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected ButterknifeInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
