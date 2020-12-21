package app.editors.manager.mvp.models.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

public class Base {

    public static final String KEY_COUNT = "count";
    public static final String KEY_STATUS = "status";
    public static final String KEY_STATUS_CODE = "statusCode";
    public static final String KEY_ERROR = "error";
    public static final String KEY_RESPONSE = "response";

    @SerializedName(KEY_COUNT)
    @Expose
    private int count = 0;

    @SerializedName(KEY_STATUS)
    @Expose
    private String status = "";

    @SerializedName(KEY_STATUS_CODE)
    @Expose
    private String statusCode = "";

    @SerializedName(KEY_ERROR)
    @Expose
    private java.lang.Error error = new java.lang.Error();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public java.lang.Error getError() {
        return error;
    }

    public void setError(java.lang.Error error) {
        this.error = error;
    }

    /*
    * Comparators
    * */
    public static abstract class AbstractSort<Type> implements Comparator<Type> {

        public static final int SORT_ORDER_ASC = 1;
        public static final int SORT_ORDER_DESC = -1;

        protected int mSortOrder = SORT_ORDER_ASC;

        public AbstractSort(boolean isSortAsc) {
            mSortOrder = isSortAsc? SORT_ORDER_ASC : SORT_ORDER_DESC;
        }
    }

}
