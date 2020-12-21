package app.editors.manager.managers.exceptions;


import android.os.Build;

import androidx.annotation.RequiresApi;

public class ApiInitException extends RuntimeException {

    public ApiInitException() {
        super();
    }

    public ApiInitException(String message) {
        super(message);
    }

    public ApiInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiInitException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected ApiInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
