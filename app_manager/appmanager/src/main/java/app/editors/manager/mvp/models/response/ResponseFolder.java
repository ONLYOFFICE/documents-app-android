package app.editors.manager.mvp.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.Folder;

public class ResponseFolder extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private Folder response;

    public Folder getResponse() {
        return response;
    }

    public void setResponse(Folder response) {
        this.response = response;
    }

}
