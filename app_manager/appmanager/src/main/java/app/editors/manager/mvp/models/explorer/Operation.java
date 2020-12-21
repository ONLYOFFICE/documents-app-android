package app.editors.manager.mvp.models.explorer;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Operation implements Cloneable, Serializable {

    @SerializedName("id")
    @Expose
    private String id = "";
    @SerializedName("operation")
    @Expose
    private int operation = 0;
    @SerializedName("progress")
    @Expose
    private int progress = 0;
    @SerializedName("error")
    @Expose
    private String error = null;
    @SerializedName("processed")
    @Expose
    private String processed = "";
    @SerializedName("finished")
    @Expose
    private boolean finished = false;
    @SerializedName("url")
    @Expose
    private String url = "";
    @SerializedName("files")
    @Expose
    private List<File> files = new ArrayList<>();
    @SerializedName("folders")
    @Expose
    private List<Folder> folders = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public boolean getFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public int getItemsCount() {
        final int countFolders = folders != null? folders.size() : 0;
        final int countFiles = files != null? files.size() : 0;
        return countFolders + countFiles;
    }

    @Override
    public Operation clone() {
        try {
            final Operation operation = (Operation) super.clone();
            operation.setFiles(new ArrayList<>(files));
            operation.setFolders(new ArrayList<>(folders));
            return operation;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}
