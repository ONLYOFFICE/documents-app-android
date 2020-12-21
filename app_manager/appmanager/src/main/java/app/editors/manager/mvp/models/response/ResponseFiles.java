package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.File;

public class ResponseFiles extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<File> response;

    public List<File> getResponse() {
        return response;
    }

    public void setResponse(List<File> response) {
        this.response = response;
    }
}
