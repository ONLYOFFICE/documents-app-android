package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.user.User;

public class ResponseUsers extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<User> response;

    public List<User> getResponse() {
        return response;
    }

    public void setResponse(List<User> response) {
        this.response = response;
    }

}