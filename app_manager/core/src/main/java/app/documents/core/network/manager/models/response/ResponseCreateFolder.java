package app.documents.core.network.manager.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.CloudFolder;

public class ResponseCreateFolder extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private CloudFolder response;

    public CloudFolder getResponse() {
        return response;
    }

    public void setResponse(CloudFolder response) {
        this.response = response;
    }

}
