package app.editors.manager.mvp.models.account;


import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

import lib.toolkit.base.managers.utils.CryptUtils;

import static app.editors.manager.mvp.models.account.AccountsSqlData.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class AccountsSqlData extends Accounts implements Parcelable {

    public static final String TABLE_NAME = "USER_ACCOUNTS";
    public static final String FIELD_NAME_ID = "ID";
    public static final String FIELD_NAME_AVATAR_IMAGE = "USER_AVATAR";
    public static final String FIELD_NAME_SSL_CIPHERS = "SSL_CIPHERS";
    public static final String FIELD_NAME_SSL_STATE = "SSL_STATE";
    public static final String FIELD_NAME_IS_ONLINE = "IS_LOGIN";
    public static final String FIELD_NAME_IS_WEB_DAV = "IS_WEB_DAV";
    public static final String FIELD_NAME_WEB_DAV_PROVIDER = "WEB_DAV_PROVIDER";
    public static final String FIELD_NAME_PASSWORD = "PASSWORD";
    public static final String FIELD_NAME_WEB_DAV_PATH = "WEB_DAV_PATH";

    @DatabaseField(id = true, useGetSet = true, dataType = DataType.STRING, columnName = FIELD_NAME_ID)
    private String id;
    @DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = FIELD_NAME_AVATAR_IMAGE)
    private byte[] imageBytes;
    @DatabaseField(dataType = DataType.BOOLEAN_OBJ, canBeNull = false, defaultValue = "false", columnName = FIELD_NAME_SSL_CIPHERS)
    private Boolean isSslCiphers;
    @DatabaseField(dataType = DataType.BOOLEAN_OBJ, canBeNull = false, defaultValue = "true", columnName = FIELD_NAME_SSL_STATE)
    private Boolean isSslState;
    @DatabaseField(dataType = DataType.BOOLEAN_OBJ, canBeNull = false, defaultValue = "false", columnName = FIELD_NAME_IS_ONLINE)
    private Boolean isOnline;
    @DatabaseField(dataType = DataType.BOOLEAN_OBJ, canBeNull = false, defaultValue = "false", columnName = FIELD_NAME_IS_WEB_DAV)
    private Boolean isWebDav;
    @DatabaseField(dataType = DataType.STRING, canBeNull = false, defaultValue = "", columnName = FIELD_NAME_WEB_DAV_PROVIDER)
    private String webDavProvider;
    @DatabaseField(dataType = DataType.STRING, canBeNull = false, defaultValue = "", columnName = FIELD_NAME_WEB_DAV_PATH)
    private String webDavPath;
    @DatabaseField(dataType = DataType.STRING, canBeNull = false, defaultValue = "", columnName = FIELD_NAME_PASSWORD)
    private String password;
    @ForeignCollectionField(eager = true)
    private Collection<Storage> storage;
    private Boolean isSelection = false;

    public AccountsSqlData(String portal, String login, String scheme, String name, String token, String provider,
                           String avatarUrl, byte[] imageBytes, boolean isSslCiphers, boolean isSslState, Collection<Storage> storage) {
        super(portal, login, scheme, name, token, provider, avatarUrl);
        this.imageBytes = imageBytes;
        this.isSslCiphers = isSslCiphers;
        this.isSslState = isSslState;
        this.storage = storage;
    }

    /*
     * Needed for ORMLite.
     * */
    public AccountsSqlData() {
        super();
    }

    public String getId() {
        return getPortal() + "-" + (getLogin() != null ? getLogin() : (getProvider() != null ? getProvider() : ""));
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public boolean isSslCiphers() {
        return isSslCiphers;
    }

    public void setSslCiphers(boolean sslCiphers) {
        isSslCiphers = sslCiphers;
    }

    public boolean isSslState() {
        return isSslState;
    }

    public void setSslState(boolean sslState) {
        isSslState = sslState;
    }

    public Collection<Storage> getStorage() {
        return storage;
    }

    public void setStorage(Collection<Storage> storage) {
        this.storage = storage;
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public void setOnline(Boolean login) {
        isOnline = login;
    }

    public Boolean isWebDav() {
        return isWebDav;
    }

    public void setWebDav(Boolean webDav) {
        isWebDav = webDav;
    }

    public String getWebDavProvider() {
        return webDavProvider;
    }

    public void setWebDavProvider(String webDavProvider) {
        this.webDavProvider = webDavProvider;
    }

    public String getPassword() {
        return CryptUtils.decryptAES128(this.password, portal + login);
    }

    public void setPassword(String password) {
        this.password = CryptUtils.encryptAES128(password, portal + login);
    }

    public String getWebDavPath() {
        return webDavPath;
    }

    public void setWebDavPath(String path) {
        this.webDavPath = path;
    }

    public Boolean isSelection() {
        return isSelection;
    }

    public void setSelection(Boolean selection) {
        isSelection = selection;
    }

    public boolean isIdNull() {
        return getId().equals("null-");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeByteArray(this.imageBytes);
        dest.writeValue(this.isSslCiphers);
        dest.writeValue(this.isSslState);
        dest.writeString(this.portal);
        dest.writeString(this.login);
        dest.writeString(this.scheme);
        dest.writeString(this.name);
        dest.writeString(this.token);
        dest.writeString(this.provider);
        dest.writeString(this.avatarUrl);
        dest.writeValue(this.isWebDav);
        dest.writeValue(this.isOnline);
        dest.writeString(this.webDavProvider);
        dest.writeString(this.password);
        dest.writeString(this.webDavPath);
        dest.writeValue(this.isSelection);
    }

    protected AccountsSqlData(Parcel in) {
        this.id = in.readString();
        this.imageBytes = in.createByteArray();
        this.isSslCiphers = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.isSslState = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.portal = in.readString();
        this.login = in.readString();
        this.scheme = in.readString();
        this.name = in.readString();
        this.token = in.readString();
        this.provider = in.readString();
        this.avatarUrl = in.readString();
        this.isWebDav = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.isOnline = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.webDavProvider = in.readString();
        this.password = in.readString();
        this.webDavPath = in.readString();
        this.isSelection = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Parcelable.Creator<AccountsSqlData> CREATOR = new Parcelable.Creator<AccountsSqlData>() {
        @Override
        public AccountsSqlData createFromParcel(Parcel source) {
            return new AccountsSqlData(source);
        }

        @Override
        public AccountsSqlData[] newArray(int size) {
            return new AccountsSqlData[size];
        }
    };
}
