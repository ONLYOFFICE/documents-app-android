package app.documents.core.network.manager.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.CloudFile;

public class ResponseCreateFile extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private CloudFile response;

    public CloudFile getResponse() {
        return response;
    }

    public void setResponse(CloudFile response) {
        this.response = response;
    }

}
