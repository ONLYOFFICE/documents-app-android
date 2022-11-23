package app.documents.core.network.manager.models.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestValidatePortal {

    @SerializedName("portalName")
    @Expose
    private String portalName;

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

}