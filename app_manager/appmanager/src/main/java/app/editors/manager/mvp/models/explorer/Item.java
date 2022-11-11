package app.editors.manager.mvp.models.explorer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import app.documents.core.network.ApiContract;
import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.base.ItemProperties;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class Item extends ItemProperties implements Serializable, Entity, Cloneable {


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
    private boolean shared = false;

    @SerializedName("rootFolderType")
    @Expose
    private String rootFolderType = "-1";

    @SerializedName("updatedBy")
    @Expose
    private UpdatedBy updatedBy = new UpdatedBy();

    @SerializedName("created")
    @Expose
    private Date created = new Date();

    @SerializedName("createdBy")
    @Expose
    private CreatedBy createdBy = new CreatedBy();

    @SerializedName("updated")
    @Expose
    private Date updated = new Date();

    @SerializedName("providerItem")
    @Expose
    private boolean providerItem = false;

    @SerializedName("favorite")
    @Expose
    private boolean favorite = false;

    @SerializedName("canShare")
    @Expose
    private boolean canShare = false;

    @SerializedName("canEdit")
    @Expose
    private boolean canEdit = false;

    public boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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

    public String getRootFolderType() {
        return rootFolderType;
    }

    public void setRootFolderType(String rootFolderType) {
        this.rootFolderType = rootFolderType;
    }

    public UpdatedBy getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UpdatedBy updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public boolean getProviderItem() {
        return providerItem;
    }

    public void setProviderItem(boolean providerItem) {
        this.providerItem = providerItem;
    }

    public boolean isCanShare() {
        return canShare;
    }

    public void setCanShare(boolean canShare) {
        this.canShare = canShare;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public int getIntAccess() {
        final String access = getAccess();
        try {
            return Integer.parseInt(access);
        }catch (NumberFormatException error) {
            return ApiContract.ShareType.INSTANCE.getCode(getAccess());
        }
    }

    public void setItem(Item item) {
        id = item.getId();
        title = item.getTitle();
        access = item.getAccess();
        shared = item.getShared();
        rootFolderType = item.getRootFolderType();
        updatedBy = item.getUpdatedBy();
        updated = item.getUpdated();
        createdBy = item.getCreatedBy();
        created = item.getCreated();
        providerItem = item.getProviderItem();
        favorite = item.getFavorite();
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Item)) {
            return false;
        }

        final Item item = (Item) obj;
        return !id.isEmpty() && id.equals(item.getId());
    }

    @Override
    public Item clone() {
        try {
            return (Item) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public int getType(TypeFactory factory) {
        return 0;
    }

    /*
    * Comparators
    * */
    public static class SortCreateDate extends Base.AbstractSort<Item> {

        public SortCreateDate(boolean isSortAsc) {
            super(isSortAsc);
        }

        @Override
        public int compare(Item o1, Item o2) {
            return mSortOrder * o1.getCreated().compareTo(o2.getCreated());
        }
    }

    public static class SortUpdateDate  extends Base.AbstractSort<Item> {

        public SortUpdateDate(boolean isSortAsc) {
            super(isSortAsc);
        }

        @Override
        public int compare(Item o1, Item o2) {
            return mSortOrder * o1.getUpdated().compareTo(o2.getUpdated());
        }
    }

    public static class SortTitle extends Base.AbstractSort<Item> {

        public SortTitle(boolean isSortAsc) {
            super(isSortAsc);
        }

        @Override
        public int compare(Item o1, Item o2) {
            return mSortOrder * o1.getTitle().compareTo(o2.getTitle());
        }
    }

    public static class SortOwner extends Base.AbstractSort<Item> {

        public SortOwner(boolean isSortAsc) {
            super(isSortAsc);
        }

        @Override
        public int compare(Item o1, Item o2) {
            return mSortOrder * o1.getCreatedBy().getDisplayName().compareTo(o2.getCreatedBy().getDisplayName());
        }
    }

}