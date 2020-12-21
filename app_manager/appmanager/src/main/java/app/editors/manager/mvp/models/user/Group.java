package app.editors.manager.mvp.models.user;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.editors.manager.mvp.models.base.ItemProperties;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class Group extends ItemProperties implements Serializable, Comparable {

    @SerializedName("id")
    @Expose
    private String id = "";

    @SerializedName("name")
    @Expose
    private String name = "";

    @SerializedName("manager")
    @Expose
    private String manager = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
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

        if (!(obj instanceof Group)) {
            return false;
        }

        Group group = (Group) obj;
        return !id.isEmpty() && id.equals(group.getId());
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return name.compareTo(((Group) o).getName());
    }

    @Override
    public int getType(TypeFactory factory) {
        return 0;
    }
}