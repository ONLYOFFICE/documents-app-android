package app.editors.manager.mvp.models.explorer;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.documents.core.network.ApiContract;
import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class CloudFolder extends Item implements Serializable, Entity, Cloneable {

    @SerializedName("parentId")
    @Expose
    private String parentId = "";

    @SerializedName("filesCount")
    @Expose
    private int filesCount = 0;

    @SerializedName("foldersCount")
    @Expose
    private int foldersCount = 0;

    @SerializedName("providerKey")
    @Expose
    private String providerKey = "";

    @SerializedName("pinned")
    @Expose
    private Boolean pinned = false;

    @SerializedName("roomType")
    @Expose
    private int roomType = -1;

    @SerializedName("private")
    @Expose
    private boolean isPrivate = false;

    @SerializedName("new")
    @Expose
    private int newCount = 0;

    @SerializedName("tags")
    @Expose
    private String[] tags = new String[0];

    private String etag = "";

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(int filesCount) {
        this.filesCount = filesCount;
    }

    public int getFoldersCount() {
        return foldersCount;
    }

    public void setFoldersCount(int foldersCount) {
        this.foldersCount = foldersCount;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
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

    public void setFolder(CloudFolder folder) {
        setItem(folder);
        parentId = folder.getParentId();
        filesCount = folder.getFilesCount();
        foldersCount = folder.getFoldersCount();
        providerKey = folder.getProviderKey();
    }

    @Override
    @Nullable
    public CloudFolder clone() {
        return (CloudFolder) super.clone();
    }

    @Override
    public int getType(TypeFactory factory) {
        return factory.type(this);
    }

    public boolean isParentRoom() {
        return Integer.parseInt(getRootFolderType()) >= ApiContract.SectionType.CLOUD_VIRTUAL_ROOM ;
    }

    public boolean isRoom() {
        return isParentRoom() && roomType >= 1 ;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public int getNewCount() {
        return newCount;
    }

    public void setNewCount(int newCount) {
        this.newCount = newCount;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    /*
    * Comparators
    * */
    public static class SortFolderSize extends Base.AbstractSort<CloudFolder> {

        public SortFolderSize(boolean isSortAsc) {
            super(isSortAsc);
        }

        @Override
        public int compare(CloudFolder o1, CloudFolder o2) {
            return mSortOrder * Integer.compare(o1.getFilesCount(), o2.getFilesCount());
        }
    }

}