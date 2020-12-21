/*
 * Created by Michael Efremov on 05.10.20 16:45
 */

package app.editors.manager.mvp.models.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Download {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("operation")
    @Expose
    private Integer operation;
    @SerializedName("progress")
    @Expose
    private Integer progress;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("processed")
    @Expose
    private String processed;
    @SerializedName("finished")
    @Expose
    private Boolean finished;
    @SerializedName("url")
    @Expose
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
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

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
