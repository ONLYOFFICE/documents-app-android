package app.editors.manager.managers.exceptions;


import android.os.Build;

import androidx.annotation.RequiresApi;

public class UrlSyntaxMistake extends Exception {

    public UrlSyntaxMistake() {
        super();
    }

    public UrlSyntaxMistake(String message) {
        super(message);
    }

    public UrlSyntaxMistake(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlSyntaxMistake(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected UrlSyntaxMistake(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        return "Wrong address/portal format!";
    }

}
