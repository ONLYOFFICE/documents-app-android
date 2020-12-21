package app.editors.manager.mvp.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestBatchOperation extends RequestBatchBase {

    @SerializedName("destFolderId")
    @Expose
    private String destFolderId;

    @SerializedName("conflictResolveType")
    @Expose
    private int conflictResolveType;

    public String getDestFolderId() {
        return destFolderId;
    }

    public void setDestFolderId(String destFolderId) {
        this.destFolderId = destFolderId;
    }

    public int getConflictResolveType() {
        return conflictResolveType;
    }

    public void setConflictResolveType(int conflictResolveType) {
        this.conflictResolveType = conflictResolveType;
    }

}