package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.user.Group;

public class ResponseGroups extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<Group> response;

    public List<Group> getResponse() {
        return response;
    }

    public void setResponse(List<Group> response) {
        this.response = response;
    }

}