package app.editors.manager.mvp.models.account;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

import static app.editors.manager.mvp.models.account.Recent.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class Recent implements Entity, Comparable<Recent> {

    static final String TABLE_NAME = "recent";
    static final String FIELD_ID = "id";
    static final String FIELD_ID_FILE = "idFile";
    static final String FIELD_PATH = "path";
    static final String FIELD_NAME = "name";
    static final String FIELD_DATE = "date";
    static final String FIELD_IS_LOCAL = "idLocal";
    static final String FIELD_SIZE = "size";
    static final String FIELD_IS_WEB_DAV = "isWebDav";


    @DatabaseField(id = true, useGetSet = true, dataType = DataType.STRING, columnName = FIELD_ID)
    private String id;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_ID_FILE)
    private String idFile;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_PATH)
    private String path;
    @DatabaseField(dataType = DataType.STRING, columnName = FIELD_NAME)
    private String name;
    @DatabaseField(dataType = DataType.DATE, columnName = FIELD_DATE)
    private Date date;
    @DatabaseField(dataType = DataType.BOOLEAN, defaultValue = "true", columnName = FIELD_IS_LOCAL)
    private boolean isLocal;
    @DatabaseField(dataType = DataType.BOOLEAN, defaultValue = "false", columnName = FIELD_IS_WEB_DAV)
    private boolean isWebDav;
    @DatabaseField(dataType = DataType.LONG, columnName = FIELD_SIZE)
    private long size;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private AccountsSqlData accountsSqlData;

    public Recent() {
    }

    public String getId() {
        return idFile != null ? idFile : path;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdFile() {
        return idFile;
    }

    public void setIdFile(String idFile) {
        this.idFile = idFile;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isWebDav() {
        return isWebDav;
    }

    public void setWebDav(boolean webDav) {
        isWebDav = webDav;
    }

    public AccountsSqlData getAccountsSqlData() {
        return accountsSqlData;
    }

    public void setAccountsSqlData(AccountsSqlData accountsSqlData) {
        this.accountsSqlData = accountsSqlData;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int getType(TypeFactory factory) {
        return 0;
    }

    @Override
    public int compareTo(Recent o) {
        return date.compareTo(o.getDate());
    }
}
