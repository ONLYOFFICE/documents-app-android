
package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.user.Portal;

public class ResponsePortal extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private Portal response;

    public Portal getResponse() {
        return response;
    }

    public void setResponse(Portal response) {
        this.response = response;
    }

}
