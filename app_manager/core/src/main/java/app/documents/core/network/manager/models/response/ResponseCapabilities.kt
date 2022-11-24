package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.login.models.Capabilities;

public class ResponseCapabilities extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private Capabilities response;

    public Capabilities getResponse() {
        return response;
    }

    public void setResponse(Capabilities response) {
        this.response = response;
    }

}