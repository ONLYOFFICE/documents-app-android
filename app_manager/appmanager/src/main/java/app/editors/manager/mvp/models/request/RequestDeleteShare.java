package app.editors.manager.mvp.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RequestDeleteShare {

    @SerializedName("folderIds")
    @Expose
    private List<String> folderIds = new ArrayList<>();

    @SerializedName("fileIds")
    @Expose
    private List<String> fileIds = new ArrayList<>();

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

}