package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.Explorer;

public class ResponseCloudTree extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<Explorer> response;

    public List<Explorer> getResponse() {
        return response;
    }

    public void setResponse(List<Explorer> response) {
        this.response = response;
    }
}
