
package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.user.Portal;

public class ResponsePortal extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private Portal response;

    public Portal getResponse() {
        return response;
    }

    public void setResponse(Portal response) {
        this.response = response;
    }

}
