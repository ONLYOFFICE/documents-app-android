package app.editors.manager.mvp.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestDelete {

    @SerializedName("deleteAfter")
    @Expose
    private boolean deleteAfter;

    public boolean getDeleteAfter() {
        return deleteAfter;
    }

    public void setDeleteAfter(boolean deleteAfter) {
        this.deleteAfter = deleteAfter;
    }
}