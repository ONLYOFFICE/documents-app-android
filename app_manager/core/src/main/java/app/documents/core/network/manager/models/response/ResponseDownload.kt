/*
 * Created by Michael Efremov on 05.10.20 16:35
 */

package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.documents.core.network.common.models.BaseResponse;
import app.documents.core.network.manager.models.base.Download;

public class ResponseDownload extends BaseResponse {

    @SerializedName(BaseResponse.KEY_RESPONSE)
    @Expose
    private List<Download> response;

    public List<Download> getResponse() {
        return response;
    }

    public void setResponse(List<Download> response) {
        this.response = response;
    }

}
