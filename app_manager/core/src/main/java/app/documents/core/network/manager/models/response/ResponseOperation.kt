package app.documents.core.network.manager.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.explorer.Operation;

public class ResponseOperation extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private List<Operation> response;

    public List<Operation> getResponse() {
        return response;
    }

    public void setResponse(List<Operation> response) {
        this.response = response;
    }

}
