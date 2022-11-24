package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.explorer.CloudFile;

public class ResponseFiles extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private List<CloudFile> response;

    public List<CloudFile> getResponse() {
        return response;
    }

    public void setResponse(List<CloudFile> response) {
        this.response = response;
    }
}
