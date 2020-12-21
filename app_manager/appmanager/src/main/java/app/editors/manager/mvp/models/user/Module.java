package app.editors.manager.mvp.models.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Module {

    @SerializedName("webItemId")
    @Expose
    private String webItemId;

    @SerializedName("users")
    @Expose
    private List<User> users;

    @SerializedName("groups")
    @Expose
    private List<Group> groups;

    @SerializedName("enabled")
    @Expose
    private boolean enable;

    @SerializedName("isSubItem")
    @Expose
    private boolean subItem;

    public String getWebItemId() {
        return webItemId;
    }

    public void setWebItemId(String webItemId) {
        this.webItemId = webItemId;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isSubItem() {
        return subItem;
    }

    public void setSubItem(boolean subItem) {
        this.subItem = subItem;
    }
}
