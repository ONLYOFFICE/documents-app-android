package app.editors.manager.mvp.models.user;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import app.editors.manager.mvp.models.base.ItemProperties;
import lib.toolkit.base.managers.utils.StringUtils;

public class User extends ItemProperties implements Serializable, Comparable {

    @SerializedName("id")
    @Expose
    private String id = "";

    @SerializedName("userName")
    @Expose
    private String userName = "";

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

    @SerializedName("birthday")
    @Expose
    private String birthday = "";

    @SerializedName("sex")
    @Expose
    private String sex = "";

    @SerializedName("status")
    @Expose
    private String status = "";

    @SerializedName("activationStatus")
    @Expose
    private String activationStatus = "";

    @SerializedName("terminated")
    @Expose
    private String terminated = "";

    @SerializedName("department")
    @Expose
    private String department = "";

    @SerializedName("workFrom")
    @Expose
    private String workFrom = "";

    @SerializedName("location")
    @Expose
    private String location = "";

    @SerializedName("notes")
    @Expose
    private String notes = "";

    @SerializedName("displayName")
    @Expose
    private String displayName = "";

    @SerializedName("title")
    @Expose
    private String title = "";

    @SerializedName("contacts")
    @Expose
    private List<Contact> contacts = new ArrayList<>();

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

    @SerializedName("cultureName")
    @Expose
    private String cultureName = "";

    @SerializedName("isSSO")
    @Expose
    private boolean isSSO = false;

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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }

    public String getTerminated() {
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
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

    public String getCultureName() {
        return cultureName;
    }

    public void setCultureName(String cultureName) {
        this.cultureName = cultureName;
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

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        User user = (User) obj;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int compareTo(@NonNull Object o) {
        int compare = displayName.compareTo(((User) o).getDisplayNameHtml());
        if (compare != 0) {
            return compare;
        }

        compare = department.compareTo(((User) o).getDepartment());
        return compare == 0? id.compareTo(((User) o).getId()) : compare;
    }

}

