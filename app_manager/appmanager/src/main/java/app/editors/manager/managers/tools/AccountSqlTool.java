package app.editors.manager.managers.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.editors.manager.app.App;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.account.Recent;
import app.editors.manager.mvp.models.account.Storage;

@Deprecated
public class AccountSqlTool extends OrmLiteSqliteOpenHelper {

    public static final String TAG = AccountSqlTool.class.getSimpleName();
    private static final int VERSION = 5;

    private Context mContext;
    private Dao<AccountsSqlData, Integer> mAccountsDao;
    private Dao<Recent, Integer> mRecentDao;
    private Dao<Storage, Integer> mStorageDao;
    private RuntimeExceptionDao<AccountsSqlData, Integer> mAccountsRuntimeExceptionDao;

    public AccountSqlTool(final Context context) {
        super(context, TAG, null, VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, AccountsSqlData.class);
            TableUtils.createTableIfNotExists(connectionSource, Storage.class);
            TableUtils.createTableIfNotExists(connectionSource, Recent.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {
            switch (newVer) {
                case 2:
                    TableUtils.dropTable(connectionSource, AccountsSqlData.class, true);
                    onCreate(db, connectionSource);
                    break;
                case 3:
                    TableUtils.createTableIfNotExists(connectionSource, Recent.class);
                    onCreate(db, connectionSource);
                    break;
                case 4:
                    TableUtils.createTableIfNotExists(connectionSource, Storage.class);
                    onCreate(db, connectionSource);
                    break;
                case 5:
                    TableUtils.createTableIfNotExists(connectionSource, Storage.class);
                    TableUtils.createTableIfNotExists(connectionSource, Recent.class);
                    onCreate(db, connectionSource);
                    updateToVersionFive();
                    break;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateToVersionFive() {
        Dao<AccountsSqlData, Integer> accountsDao;
        try {
            accountsDao = getAccountsDao();
            accountsDao.executeRaw("ALTER TABLE " +
                    AccountsSqlData.TABLE_NAME + " ADD COLUMN " +
                    AccountsSqlData.FIELD_NAME_IS_WEB_DAV + " BOOLEAN;");
            accountsDao.executeRaw("ALTER TABLE " +
                    AccountsSqlData.TABLE_NAME + " ADD COLUMN " +
                    AccountsSqlData.FIELD_NAME_IS_ONLINE + " BOOLEAN;");
            accountsDao.executeRaw("ALTER TABLE " +
                    AccountsSqlData.TABLE_NAME + " ADD COLUMN " +
                    AccountsSqlData.FIELD_NAME_PASSWORD + " STRING;");
            accountsDao.executeRaw("ALTER TABLE " +
                    AccountsSqlData.TABLE_NAME + " ADD COLUMN " +
                    AccountsSqlData.FIELD_NAME_WEB_DAV_PROVIDER + " STRING;");
            accountsDao.executeRaw("ALTER TABLE " +
                    AccountsSqlData.TABLE_NAME + " ADD COLUMN " +
                    AccountsSqlData.FIELD_NAME_WEB_DAV_PATH + " STRING;");

            List<AccountsSqlData> accounts = getAccounts();
            PreferenceTool preferenceTool = App.getApp().getAppComponent().getPreference();
            AccountsSqlData accountOnline = getAccount(preferenceTool.getPortal(), preferenceTool.getLogin(), preferenceTool.getSocialProvider());

            if (accounts != null && !accounts.isEmpty()) {
                for (AccountsSqlData account : accounts) {
                    account.setWebDav(false);
                    account.setWebDavPath("");
                    account.setWebDavProvider("");
                    account.setPassword("");

                    if (accountOnline != null && accountOnline.equals(account)){
                        account.setOnline(true);
                    } else {
                        account.setOnline(false);
                    }

                    setAccount(account);
                }
            }

        } catch (SQLException e) {
            Log.d(TAG, "error update to version five");
        }

    }

    @Override
    public void close() {
        super.close();
        mAccountsDao = null;
        mStorageDao = null;
        mRecentDao = null;
        mAccountsRuntimeExceptionDao = null;
    }

    private Dao<AccountsSqlData, Integer> getAccountsDao() throws SQLException {
        if (mAccountsDao == null) {
            mAccountsDao = getDao(AccountsSqlData.class);
        }
        return mAccountsDao;
    }

    private Dao<Recent, Integer> getRecentDao() throws SQLException {
        if (mRecentDao == null) {
            mRecentDao = getDao(Recent.class);
        }
        return mRecentDao;
    }

    public void addRecent(String fileId, String path, String name, long size, boolean isLocal, boolean isWebDav, Date date, AccountsSqlData account) {
        Recent recent = new Recent();
        recent.setIdFile(fileId);
        recent.setPath(path);
        recent.setWebDav(isWebDav);
        recent.setName(name);
        recent.setDate(date);
        recent.setSize(size);
        recent.setLocal(isLocal);
        recent.setAccountsSqlData(account);
        addRecent(recent);
    }

    public void addRecent(Recent recent) {
        try {
            QueryBuilder queryBuilder = getRecentDao().queryBuilder();
            if (queryBuilder.where().eq("id", recent.getId()).countOf() > 0) {
                getRecentDao().update(recent);
            } else {
                getRecentDao().create(recent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Recent> getRecent() {
        try {
            return getRecentDao().queryBuilder().limit(25L).query();
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public boolean delete(Recent recent) {
        try {
            return getRecentDao().delete(recent) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void deleteAccountRecent(AccountsSqlData accountsSqlData) {
        try {
            List<Recent> recents = getRecentDao().queryForAll();
            for (Recent recent : recents) {
                if (recent.getAccountsSqlData() != null &&
                        recent.getAccountsSqlData().getId().equals(accountsSqlData.getId())) {
                    getRecentDao().delete(recent);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Dao<Storage, Integer> getStorageDao() throws SQLException {
        if (mStorageDao == null) {
            mStorageDao = getDao(Storage.class);
        }
        return mStorageDao;
    }

    private void addStorage(AccountsSqlData account, Storage storage) {
        try {
            storage.setAccountsSqlData(account);
            account.getStorage().add(storage);
            getAccountsDao().createOrUpdate(account);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Storage> getStorage() {
        try {
            return getStorageDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void addStorage(AccountsSqlData account, Collection<Storage> storage) {
        account.getStorage().clear();
        for (Storage s : storage) {
            addStorage(account, s);
        }
    }

    public RuntimeExceptionDao<AccountsSqlData, Integer> getAccountsRuntimeExceptionDao() {
        if (mAccountsRuntimeExceptionDao == null) {
            mAccountsRuntimeExceptionDao = getRuntimeExceptionDao(AccountsSqlData.class);
        }
        return mAccountsRuntimeExceptionDao;
    }

    public boolean setAccount(@NonNull AccountsSqlData accountsSqlData) {
        try {
            getAccountsDao().createOrUpdate(accountsSqlData);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public boolean setAccount(@NonNull String portal, @NonNull String login, @NonNull final String scheme,
                              @Nullable final String name, @NonNull final String token, @Nullable String provider,
                              @Nullable final String avatarUrl, @Nullable byte[] imageBytes,
                              boolean isSslCipher, boolean isSslState) {

        final AccountsSqlData accountsSqlData = getAccount(portal, login, provider);
        if (accountsSqlData != null) {
            accountsSqlData.setPortal(portal);
            accountsSqlData.setLogin(login);
            accountsSqlData.setScheme(scheme);
            accountsSqlData.setName(Html.fromHtml(name).toString());
            accountsSqlData.setToken(token);
            accountsSqlData.setProvider(provider);
            accountsSqlData.setAvatarUrl(avatarUrl);
            return setAccount(accountsSqlData);
        }

        return setAccount(new AccountsSqlData(portal, login, scheme, name, token, provider, avatarUrl, imageBytes, isSslCipher, isSslState, new ArrayList<>()));
    }

    public boolean setAccountAvatar(@NonNull final String portal, @Nullable final String login,
                                    @Nullable final String provider, @Nullable byte[] imageBytes) {
        final AccountsSqlData accountsSqlData = getAccount(portal, login, provider);
        if (accountsSqlData != null) {
            accountsSqlData.setImageBytes(imageBytes);
            return setAccount(accountsSqlData);
        }
        return false;
    }

    public boolean setAccountName(@NonNull final String portal, @Nullable final String login, @Nullable final String provider,
                                  @NonNull final String name) {
        final AccountsSqlData accountsSqlData = getAccount(portal, login, provider);
        if (accountsSqlData != null) {
            accountsSqlData.setName(name);
            return setAccount(accountsSqlData);
        }
        return false;
    }

    public AccountsSqlData getAccountOnline() {
        try {
            QueryBuilder<AccountsSqlData, Integer> queryBuilder = getAccountsDao().queryBuilder();
            queryBuilder.where().eq(AccountsSqlData.FIELD_NAME_IS_ONLINE, true);
            List<AccountsSqlData> list = queryBuilder.query();
            if (list.isEmpty()) {
                return null;
            } else {
                return queryBuilder.query().get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<AccountsSqlData> getAccounts() {
        try {
            return getAccountsDao().queryForAll();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    @Nullable
    public AccountsSqlData getAccount(@NonNull String portal, @Nullable String login, @Nullable String provider) {
        try {
            return getAccountsDao().queryForSameId(new AccountsSqlData(portal, login, "", "", "", provider, "", null, false, false, null));
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean delete(@NonNull AccountsSqlData accountsSqlData) {
        try {
            getStorageDao().delete(accountsSqlData.getStorage());
            deleteAccountRecent(accountsSqlData);
            return getAccountsDao().delete(accountsSqlData) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(@NonNull String portal, @Nullable String login, @Nullable String provider) {
        return delete(new AccountsSqlData(portal, login, "", "", "", provider, "", null, false, false, null));
    }

    public long getCount() {
        try {
            return getAccountsDao().countOf();
        } catch (SQLException e) {
            return 0;
        }
    }

}