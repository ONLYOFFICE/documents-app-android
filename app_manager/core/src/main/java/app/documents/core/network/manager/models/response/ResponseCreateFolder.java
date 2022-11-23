package app.documents.core.network.manager.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.explorer.CloudFolder;

public class ResponseCreateFolder extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private CloudFolder response;

    public CloudFolder getResponse() {
        return response;
    }

    public void setResponse(CloudFolder response) {
        this.response = response;
    }

}
