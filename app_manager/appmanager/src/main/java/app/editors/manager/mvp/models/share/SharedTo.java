package app.editors.manager.mvp.models.share;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import app.editors.manager.mvp.models.user.Group;
import lib.toolkit.base.managers.utils.StringUtils;

public class SharedTo implements Serializable {

    @SerializedName("id")
    @Expose
    private String id = "";
    @SerializedName("userName")
    @Expose
    private String userName = null;
    @SerializedName("isVisitor")
    @Expose
    private boolean isVisitor = false;
    @SerializedName("firstName")
    @Expose
    private String firstName = "";
    @SerializedName("lastName")
    @Expose
    private String lastName = "";
    @SerializedName("email")
    @Expose
    private String email = "";
    @SerializedName("status")
    @Expose
    private int status = 0;
    @SerializedName("activationStatus")
    @Expose
    private int activationStatus = 0;
    @SerializedName("terminated")
    @Expose
    private String terminated = "";
    @SerializedName("department")
    @Expose
    private String department = "";
    @SerializedName("workFrom")
    @Expose
    private String workFrom = "";
    @SerializedName("displayName")
    @Expose
    private String displayName = "";
    @SerializedName("mobilePhone")
    @Expose
    private String mobilePhone = "";
    @SerializedName("groups")
    @Expose
    private List<Group> groups = new ArrayList<>();
    @SerializedName("avatarMedium")
    @Expose
    private String avatarMedium = "";
    @SerializedName("avatar")
    @Expose
    private String avatar = "";
    @SerializedName("isOnline")
    @Expose
    private boolean isOnline = false;
    @SerializedName("isAdmin")
    @Expose
    private boolean isAdmin = false;
    @SerializedName("isLDAP")
    @Expose
    private boolean isLDAP = false;
    @SerializedName("listAdminModules")
    @Expose
    private List<String> listAdminModules = new ArrayList<>();
    @SerializedName("isOwner")
    @Expose
    private boolean isOwner = false;
    @SerializedName("isSSO")
    @Expose
    private boolean isSSO = false;
    @SerializedName("avatarSmall")
    @Expose
    private String avatarSmall = "";
    @SerializedName("profileUrl")
    @Expose
    private String profileUrl = "";
    @SerializedName("name")
    @Expose
    private String name = null;
    @SerializedName("manager")
    @Expose
    private String manager = "";
    @SerializedName("shareLink")
    @Expose
    private String shareLink = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean getIsVisitor() {
        return isVisitor;
    }

    public void setIsVisitor(boolean isVisitor) {
        this.isVisitor = isVisitor;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(int activationStatus) {
        this.activationStatus = activationStatus;
    }

    public Object getTerminated() {
        return terminated;
    }

    public void setTerminated(String terminated) {
        this.terminated = terminated;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getWorkFrom() {
        return workFrom;
    }

    public void setWorkFrom(String workFrom) {
        this.workFrom = workFrom;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameHtml() {
        return StringUtils.getHtmlString(displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public String getAvatarMedium() {
        return avatarMedium;
    }

    public void setAvatarMedium(String avatarMedium) {
        this.avatarMedium = avatarMedium;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean getIsLDAP() {
        return isLDAP;
    }

    public void setIsLDAP(boolean isLDAP) {
        this.isLDAP = isLDAP;
    }

    public List<String> getListAdminModules() {
        return listAdminModules;
    }

    public void setListAdminModules(List<String> listAdminModules) {
        this.listAdminModules = listAdminModules;
    }

    public boolean getIsOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public boolean getIsSSO() {
        return isSSO;
    }

    public void setIsSSO(boolean isSSO) {
        this.isSSO = isSSO;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getShareLink() {
        return shareLink;
    }

    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }

}