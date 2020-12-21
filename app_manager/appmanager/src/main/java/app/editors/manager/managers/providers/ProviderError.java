/*
 * Created by Michael Efremov on 21.10.20 12:37
 */

package app.editors.manager.managers.providers;

public class ProviderError extends Exception {

    public final static String FORBIDDEN = "Forbidden symbol";
    public final static String INTERRUPT = "Interrupt";
    public final static String FILE_EXIST = "Exist";
    public final static String UNSUPPORTED_PATH = "Unsupported path";
    public final static String ERROR_CREATE_LOCAL = "Error create";

    public ProviderError(String message) {
        super(message);
    }

    public static ProviderError throwForbiddenError() {
        return new ProviderError(FORBIDDEN);
    }

    public static ProviderError throwInterruptException() {
        return new ProviderError(INTERRUPT);
    }

    public static ProviderError throwExistException() {
        return new ProviderError(FILE_EXIST);
    }

    public static ProviderError throwUnsupportedException() {
        return new ProviderError(UNSUPPORTED_PATH);
    }

    public static ProviderError throwErrorCreate() {
        return new ProviderError(ERROR_CREATE_LOCAL);
    }
}
