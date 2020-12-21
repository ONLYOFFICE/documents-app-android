package app.editors.manager.mvp.models.explorer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpdatedBy implements Cloneable, Serializable {

    @SerializedName("id")
    @Expose
    private String id = "";
    @SerializedName("displayName")
    @Expose
    private String displayName = "";
    @SerializedName("avatarSmall")
    @Expose
    private String avatarSmall = "";
    @SerializedName("profileUrl")
    @Expose
    private String profileUrl = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarSmall() {
        return avatarSmall;
    }

    public void setAvatarSmall(String avatarSmall) {
        this.avatarSmall = avatarSmall;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    @Override
    public UpdatedBy clone() throws CloneNotSupportedException {
        return (UpdatedBy) super.clone();
    }
}