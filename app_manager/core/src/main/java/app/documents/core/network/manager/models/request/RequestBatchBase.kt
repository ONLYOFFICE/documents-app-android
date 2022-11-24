package app.documents.core.network.manager.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestBatchBase {

    @SerializedName("folderIds")
    @Expose
    private List<String> folderIds;

    @SerializedName("fileIds")
    @Expose
    private List<String> fileIds;

    @SerializedName("deleteAfter")
    @Expose
    private boolean deleteAfter;

    public List<String> getFolderIds() {
        return folderIds;
    }

    public void setFolderIds(List<String> folderIds) {
        this.folderIds = folderIds;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public boolean isDeleteAfter() {
        return deleteAfter;
    }

    public void setDeleteAfter(boolean deleteAfter) {
        this.deleteAfter = deleteAfter;
    }

}