package app.editors.manager.mvp.models.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Error {

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_HRESULT = "hresult";
    public static final String KEY_DATA = "data";
    public static final String KEY_STACK = "stack";

    @SerializedName(KEY_MESSAGE)
    @Expose
    private String message = "";

    @SerializedName(KEY_HRESULT)
    @Expose
    private String hresult = "";

    @SerializedName(KEY_STACK)
    @Expose
    private String stack = "";

    @SerializedName(KEY_DATA)
    @Expose
    private String data = "";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHresult() {
        return hresult;
    }

    public void setHresult(String hresult) {
        this.hresult = hresult;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

}