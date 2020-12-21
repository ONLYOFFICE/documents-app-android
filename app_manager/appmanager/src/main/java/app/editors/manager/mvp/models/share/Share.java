package app.editors.manager.mvp.models.share;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.editors.manager.app.Api;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class Share implements Serializable, Entity {

    @SerializedName("access")
    @Expose
    private int access = Api.ShareCode.NONE;
    @SerializedName("sharedTo")
    @Expose
    private SharedTo sharedTo = new SharedTo();
    @SerializedName("isLocked")
    @Expose
    private boolean isLocked = false;
    @SerializedName("isOwner")
    @Expose
    private boolean isOwner = false;

    private int newAccess = Api.ShareCode.NONE;

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public SharedTo getSharedTo() {
        return sharedTo;
    }

    public void setSharedTo(SharedTo sharedTo) {
        this.sharedTo = sharedTo;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean getIsOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public int getNewAccess() {
        return newAccess;
    }

    public void setNewAccess(int newAccess) {
        this.newAccess = newAccess;
    }

    @Override
    public int getType(TypeFactory factory) {
        return 0;
    }
}