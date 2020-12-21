package app.editors.manager.mvp.models.register;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tenant {

    @SerializedName("created")
    @Expose
    private String created = "";
    @SerializedName("domain")
    @Expose
    private String domain = "";
    @SerializedName("language")
    @Expose
    private String language = "";
    @SerializedName("ownerId")
    @Expose
    private String ownerId = "";
    @SerializedName("portalName")
    @Expose
    private String portalName = "";
    @SerializedName("status")
    @Expose
    private String status = "";
    @SerializedName("tenantId")
    @Expose
    private int tenantId = 0;
    @SerializedName("timeZoneName")
    @Expose
    private String timeZoneName = "";

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPortalName() {
        return portalName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String timeZoneName) {
        this.timeZoneName = timeZoneName;
    }

}
