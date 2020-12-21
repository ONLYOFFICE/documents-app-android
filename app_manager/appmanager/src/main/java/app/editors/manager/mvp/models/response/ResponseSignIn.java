package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.user.Token;

public class ResponseSignIn extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private Token response;

    public Token getResponse() {
        return response;
    }

    public void setResponse(Token response) {
        this.response = response;
    }

}