package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.user.Thirdparty;

public class ResponseThirdparty extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<Thirdparty> response;

    public List<Thirdparty> getResponse() {
        return response;
    }

    public void setResponse(List<Thirdparty> response) {
        this.response = response;
    }
}
