package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;

public class ResponseValidatePortal extends Base {

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_ERROR = "errors";
    public static final String KEY_VARIANTS = "variants";

    @SerializedName(KEY_MESSAGE)
    @Expose
    private String message;

    @SerializedName(KEY_ERROR)
    @Expose
    private String errors;

    @SerializedName(KEY_VARIANTS)
    @Expose
    private String variants;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getVariants() {
        return variants;
    }

    public void setVariants(String variants) {
        this.variants = variants;
    }

}