package app.editors.manager.mvp.models.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("communityServer")
    @Expose
    private String communityServer;

    @SerializedName("documentServer")
    @Expose
    private String documentServer;

    @SerializedName("mailServer")
    @Expose
    private String mailServer;

    public String getCommunityServer() {
        return communityServer;
    }

    public void setCommunityServer(String communityServer) {
        this.communityServer = communityServer;
    }

    public String getDocumentServer() {
        return documentServer;
    }

    public void setDocumentServer(String documentServer) {
        this.documentServer = documentServer;
    }

    public String getMailServer() {
        return mailServer;
    }

    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }
}
