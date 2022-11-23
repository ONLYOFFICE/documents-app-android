package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.explorer.Explorer;

public class ResponseCloudTree extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private List<Explorer> response;

    public List<Explorer> getResponse() {
        return response;
    }

    public void setResponse(List<Explorer> response) {
        this.response = response;
    }
}
