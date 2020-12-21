package app.editors.manager.mvp.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestShareItem {

    @SerializedName("ShareTo")
    @Expose
    private String shareTo;
    @SerializedName("Access")
    @Expose
    private String access;

    public String getShareTo() {
        return shareTo;
    }

    public void setShareTo(String shareTo) {
        this.shareTo = shareTo;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

}