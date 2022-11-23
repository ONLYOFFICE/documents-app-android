package app.documents.core.network.manager.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.explorer.Explorer;

public class ResponseExplorer extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private Explorer response;

    public Explorer getResponse() {
        return response;
    }

    public void setResponse(Explorer response) {
        this.response = response;
    }

}
