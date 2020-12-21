package app.editors.manager.mvp.models.account;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static app.editors.manager.mvp.models.account.Storage.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class Storage {

    public static final String TABLE_NAME = "STORAGE";
    public static final String FIELD_NAME_ID = "ID";
    public static final String FIELD_NAME = "NAME";
    public static final String FIELD_NAME_CLIENT_ID = "CLIENT_ID";
    public static final String FIELD_NAME_REDIRECT_URL = "REDIRECT_URL";

    @DatabaseField(id = true, useGetSet = true, dataType = DataType.STRING, columnName = FIELD_NAME_ID)
    private String id;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME)
    private String name;
    @DatabaseField(dataType = DataType.STRING, canBeNull = true, columnName = FIELD_NAME_CLIENT_ID)
    private String clientId;
    @DatabaseField(dataType = DataType.STRING, canBeNull = true, columnName = FIELD_NAME_REDIRECT_URL)
    private String redirectUrl;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private AccountsSqlData accountsSqlData;

    public Storage() {
    }

    public Storage(String name, String clientId, String redirectUrl) {
        this.name = name;
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
    }

    public Storage(String name, String clientId, String redirectUrl, AccountsSqlData accountsSqlData) {
        this.name = name;
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
        this.accountsSqlData = accountsSqlData;
    }

    public String getId() {
        return getRedirectUrl() != null ? getClientId() + " - id - " + accountsSqlData.getId() : getName() + " - id - " + accountsSqlData.getId();
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public AccountsSqlData getAccountsSqlData() {
        return accountsSqlData;
    }

    public void setAccountsSqlData(AccountsSqlData accountsSqlData) {
        this.accountsSqlData = accountsSqlData;
    }

}
