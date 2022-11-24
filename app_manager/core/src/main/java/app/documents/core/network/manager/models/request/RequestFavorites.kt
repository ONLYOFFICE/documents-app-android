package app.documents.core.network.manager.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestFavorites {

    @SerializedName("fileIds")
    @Expose
    private List<String> fileIds;

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }


}
