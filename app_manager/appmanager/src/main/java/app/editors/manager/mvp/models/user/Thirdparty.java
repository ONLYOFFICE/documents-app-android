package app.editors.manager.mvp.models.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Thirdparty implements Serializable {

    @SerializedName("corporate")
    @Expose
    private boolean corporate;
    @SerializedName("customer_title")
    @Expose
    private String title;
    @SerializedName("provider_id")
    @Expose
    private int id;
    @SerializedName("provider_key")
    @Expose
    private String providerKey;

    public boolean isCorporate() {
        return corporate;
    }

    public void setCorporate(boolean corporate) {
        this.corporate = corporate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }
}
