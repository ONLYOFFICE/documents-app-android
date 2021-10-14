package app.editors.manager.mvp.models.account;


import androidx.annotation.Nullable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class Accounts {

    public static final String FIELD_NAME_PORTAL = "PORTAL";
    public static final String FIELD_NAME_SCHEME = "PREFIX";
    public static final String FIELD_NAME_LOGIN = "EMAIL";
    public static final String FIELD_NAME_TOKEN = "TOKEN";
    public static final String FIELD_NAME_NAME = "NAME";
    public static final String FIELD_NAME_PROVIDER = "PROVIDER";
    public static final String FIELD_NAME_AVATAR_URL = "AVATAR_URL";

    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME_PORTAL)
    protected String portal;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME_LOGIN)
    protected String login;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME_SCHEME)
    protected String scheme;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME_NAME)
    protected String name;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME_TOKEN)
    protected String token;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME_PROVIDER)
    protected String provider;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME_AVATAR_URL)
    protected String avatarUrl;

    public Accounts(String portal, String login, String scheme, String name, String token, String provider, String avatarUrl) {
        this.portal = portal;
        this.login = login;
        this.scheme = scheme;
        this.name = name;
        this.token = token;
        this.provider = provider;
        this.avatarUrl = avatarUrl;
    }

    /*
    * Needed for ORMLite.
    * */
    public Accounts() {

    }

    public String getPortal() {
        return portal;
    }

    public void setPortal(String portal) {
        this.portal = portal;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Nullable
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }


}
