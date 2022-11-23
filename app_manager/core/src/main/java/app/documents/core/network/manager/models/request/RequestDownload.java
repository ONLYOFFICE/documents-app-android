/*
 * Created by Michael Efremov on 02.10.20 17:02
 */

package app.documents.core.network.manager.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestDownload {

    @SerializedName("fileIds")
    @Expose
    private List<String> filesIds;

    @SerializedName("folderIds")
    @Expose
    private List<String> foldersIds;

    public List<String> getFilesIds() {
        return filesIds;
    }

    public void setFilesIds(List<String> filesIds) {
        this.filesIds = filesIds;
    }

    public List<String> getFoldersIds() {
        return foldersIds;
    }

    public void setFoldersIds(List<String> foldersIds) {
        this.foldersIds = foldersIds;
    }
}
