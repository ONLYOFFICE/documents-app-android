package app.editors.manager.managers.exceptions;

import java.io.IOException;

public class NoConnectivityException extends IOException {

    public NoConnectivityException() {
        super();
    }

    public NoConnectivityException(String message) {
        super(message);
    }

    public NoConnectivityException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoConnectivityException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "No connectivity exception";
    }

}