package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.user.Module;

public class ResponseModules extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<Module> response;

    public List<Module> getResponse() {
        return response;
    }

    public void setResponse(List<Module> response) {
        this.response = response;
    }
}
