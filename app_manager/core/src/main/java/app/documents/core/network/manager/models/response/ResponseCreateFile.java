package app.documents.core.network.manager.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.explorer.CloudFile;

public class ResponseCreateFile extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private CloudFile response;

    public CloudFile getResponse() {
        return response;
    }

    public void setResponse(CloudFile response) {
        this.response = response;
    }

}
