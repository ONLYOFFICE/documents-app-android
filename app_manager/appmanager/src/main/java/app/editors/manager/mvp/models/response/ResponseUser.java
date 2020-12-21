package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.user.User;

public class ResponseUser extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private User response;

    public User getResponse() {
        return response;
    }

    public void setResponse(User response) {
        this.response = response;
    }

}