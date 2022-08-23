package app.editors.manager.mvp.models.explorer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.documents.core.network.ApiContract;

public class Current implements Cloneable, Serializable {

    @SerializedName("parentId")
    @Expose
    private String parentId = "";

    @SerializedName("filesCount")
    @Expose
    private String filesCount = "";

    @SerializedName("foldersCount")
    @Expose
    private String foldersCount = "";

    @SerializedName("isShareable")
    @Expose
    private boolean isShareable;

    @SerializedName("id")
    @Expose
    private String id = "";

    @SerializedName("title")
    @Expose
    private String title = "";

    @SerializedName("access")
    @Expose
    private String access = ApiContract.ShareType.NONE;

    @SerializedName("shared")
    @Expose
    private boolean shared;

    @SerializedName("rootFolderType")
    @Expose
    private int rootFolderType = ApiContract.SectionType.UNKNOWN;

    @SerializedName("updatedBy")
    @Expose
    private UpdatedBy updatedBy = new UpdatedBy();

    @SerializedName("created")
    @Expose
    private String created = "";

    @SerializedName("createdBy")
    @Expose
    private CreatedBy createdBy = new CreatedBy();

    @SerializedName("updated")
    @Expose
    private String updated = "";

    @SerializedName("providerItem")
    @Expose
    private boolean providerItem;

    @SerializedName("pinned")
    @Expose
    private Boolean pinned = false;

    @SerializedName("roomType")
    @Expose
    private int roomType = -1;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(String filesCount) {
        this.filesCount = filesCount;
    }

    public String getFoldersCount() {
        return foldersCount;
    }

    public void setFoldersCount(String foldersCount) {
        this.foldersCount = foldersCount;
    }

    public boolean getIsShareable() {
        return isShareable;
    }

    public void setIsShareable(boolean isShareable) {
        this.isShareable = isShareable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public boolean getShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public int getRootFolderType() {
        return rootFolderType;
    }

    public void setRootFolderType(int rootFolderType) {
        this.rootFolderType = rootFolderType;
    }

    public UpdatedBy getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UpdatedBy updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public boolean getProviderItem() {
        return providerItem;
    }

    public void setProviderItem(boolean providerItem) {
        this.providerItem = providerItem;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    @Override
    public Current clone() {
        try {
            final Current current = (Current) super.clone();
            current.setCreatedBy(createdBy.clone());
            current.setUpdatedBy(updatedBy.clone());
            return current;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int getIntAccess() {
        final String access = getAccess();
        try {
            return Integer.parseInt(access);
        }catch (NumberFormatException error) {
            return ApiContract.ShareType.INSTANCE.getCode(getAccess());
        }
    }
}