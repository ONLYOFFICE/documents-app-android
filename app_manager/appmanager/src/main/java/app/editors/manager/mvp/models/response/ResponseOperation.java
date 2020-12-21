package app.editors.manager.mvp.models.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.explorer.Operation;

public class ResponseOperation extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<Operation> response;

    public List<Operation> getResponse() {
        return response;
    }

    public void setResponse(List<Operation> response) {
        this.response = response;
    }

}
