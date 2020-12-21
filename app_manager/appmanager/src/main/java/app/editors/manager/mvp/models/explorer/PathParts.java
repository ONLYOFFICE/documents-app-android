package app.editors.manager.mvp.models.explorer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PathParts {

    @SerializedName("key")
    @Expose
    private String key = "";

    @SerializedName("path")
    @Expose
    private String path = "";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}