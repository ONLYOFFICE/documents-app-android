package app.editors.manager.mvp.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.File;

public class ResponseFile extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private File response;

    public File getResponse() {
        return response;
    }

    public void setResponse(File response) {
        this.response = response;
    }

}
