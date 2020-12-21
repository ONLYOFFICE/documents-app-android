package app.editors.manager.mvp.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.Explorer;

public class ResponseExplorer extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private Explorer response;

    public Explorer getResponse() {
        return response;
    }

    public void setResponse(Explorer response) {
        this.response = response;
    }

}
