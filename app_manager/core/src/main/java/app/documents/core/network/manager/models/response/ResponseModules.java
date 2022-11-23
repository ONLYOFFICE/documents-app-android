package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.user.Module;

public class ResponseModules extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private List<Module> response;

    public List<Module> getResponse() {
        return response;
    }

    public void setResponse(List<Module> response) {
        this.response = response;
    }
}
