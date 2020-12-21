package app.editors.manager.mvp.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestShare {

    @SerializedName("share")
    @Expose
    private List<RequestShareItem> share;
    @SerializedName("notify")
    @Expose
    private boolean notify;
    @SerializedName("sharingMessage")
    @Expose
    private String sharingMessage;

    public List<RequestShareItem> getShare() {
        return share;
    }

    public void setShare(List<RequestShareItem> share) {
        this.share = share;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public String getSharingMessage() {
        return sharingMessage;
    }

    public void setSharingMessage(String sharingMessage) {
        this.sharingMessage = sharingMessage;
    }

}