package app.editors.manager.mvp.models.explorer;


import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Explorer implements Cloneable, Serializable {

    @SerializedName("files")
    @Expose
    private List<CloudFile> files = new ArrayList<>();

    @SerializedName("folders")
    @Expose
    private List<CloudFolder> folders = new ArrayList<>();

    @SerializedName("current")
    @Expose
    private Current current = new Current();

    @SerializedName("pathParts")
    @Expose
    private List<String> pathParts = new ArrayList<>();

    @SerializedName("startIndex")
    @Expose
    private String startIndex = "";

    @SerializedName("count")
    @Expose
    private int count = 0;

    @SerializedName("total")
    @Expose
    private int total = 0;

    private String destFolderId = "";

    public List<CloudFile> getFiles() {
        return files;
    }

    public void setFiles(List<CloudFile> files) {
        this.files = files;
    }

    public List<CloudFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<CloudFolder> folders) {
        this.folders = folders;
    }

    public Current getCurrent() {
        return current;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public List<String> getPathParts() {
        return pathParts;
    }

    public void setPathParts(List<String> pathParts) {
        this.pathParts = pathParts;
    }

    public String getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(String startIndex) {
        this.startIndex = startIndex;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getDestFolderId() {
        return destFolderId;
    }

    public void setDestFolderId(String destFolderId) {
        this.destFolderId = destFolderId;
    }

    public int getItemsCount() {
        final int countFolders = folders != null? folders.size() : 0;
        final int countFiles = files != null? files.size() : 0;
        return countFolders + countFiles;
    }

    @Override
    public int hashCode() {
        return current.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Explorer)) {
            return false;
        }

        final Explorer explorer = (Explorer) obj;
        return (current != null && !current.getId().isEmpty()) && current.getId().equals(explorer.current.getId());
    }

    @Override
    @Nullable
    public Explorer clone() {
        try {
            final Explorer explorer = (Explorer) super.clone();
            explorer.setCurrent(current.clone());
            explorer.setFiles(new ArrayList<>(files));
            explorer.setFolders(new ArrayList<>(folders));
            explorer.setPathParts(new ArrayList<>(pathParts));
            return explorer;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Explorer add (Explorer exp) {
        this.getFolders().addAll(exp.getFolders());
        this.getFiles().addAll(exp.getFiles());
        return this;
    }

}
