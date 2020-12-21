package app.editors.manager.managers.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import javax.inject.Inject;

import app.editors.manager.BuildConfig;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;

public class AccountProvider extends ContentProvider {

    public static final String TAG = AccountProvider.class.getSimpleName();

    public static final String AUTHORITY_SELF = BuildConfig.APPLICATION_ID + ".provider";
    public static final String AUTHORITY_RELEASE = BuildConfig.RELEASE_ID + ".provider";
    public static final String AUTHORITY_BETA = BuildConfig.BETA_ID + ".provider";
    public static final Uri CONTENT_URI_SELF;
    public static final Uri CONTENT_URI_RELEASE;
    public static final Uri CONTENT_URI_BETA;

    private static final int CODE_ACCOUNTS = 0;
    private static final UriMatcher URI_MATCHER;

    static {
        CONTENT_URI_SELF = Uri.parse("content://" + AUTHORITY_SELF + "/" + AccountsSqlData.TABLE_NAME);
        CONTENT_URI_RELEASE = Uri.parse("content://" + AUTHORITY_RELEASE + "/" + AccountsSqlData.TABLE_NAME);
        CONTENT_URI_BETA = Uri.parse("content://" + AUTHORITY_BETA + "/" + AccountsSqlData.TABLE_NAME);
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY_SELF, AccountsSqlData.TABLE_NAME, CODE_ACCOUNTS);
    }

    @Inject
    AccountSqlTool mAccountSqlTool;

    @Override
    public boolean onCreate() {
        App.getApp().getAppComponent().inject(this);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_ACCOUNTS:
                final RuntimeExceptionDao<AccountsSqlData, Integer> dao = mAccountSqlTool.getAccountsRuntimeExceptionDao();
                final AndroidDatabaseResults results = (AndroidDatabaseResults) dao.iterator().getRawResults();
                final Cursor cursor = results.getRawCursor();
                cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI_SELF);
                return cursor;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_ACCOUNTS:
                return AccountsSqlData.TABLE_NAME;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

}
