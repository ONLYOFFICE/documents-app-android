package app.editors.manager.mvp.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestExternal {

    @SerializedName("share")
    @Expose
    private String share;

    public String getShare() {
        return share;
    }

    public void setShare(String title) {
        this.share = title;
    }

}