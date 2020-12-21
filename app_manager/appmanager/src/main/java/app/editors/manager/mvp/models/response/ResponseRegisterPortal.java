package app.editors.manager.mvp.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.editors.manager.mvp.models.register.Tenant;

public class ResponseRegisterPortal {

    @SerializedName("reference")
    @Expose
    private String reference;
    @SerializedName("tenant")
    @Expose
    private Tenant tenant;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

}