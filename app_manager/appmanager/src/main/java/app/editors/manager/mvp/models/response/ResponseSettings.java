package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.base.Settings;

public class ResponseSettings extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private Settings response;

    public Settings getResponse() {
        return response;
    }

    public void setResponse(Settings response) {
        this.response = response;
    }

}
