package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.base.Capabilities;

public class ResponseCapabilities extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private Capabilities response;

    public Capabilities getResponse() {
        return response;
    }

    public void setResponse(Capabilities response) {
        this.response = response;
    }

}