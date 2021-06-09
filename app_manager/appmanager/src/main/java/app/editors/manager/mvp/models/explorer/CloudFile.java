package app.editors.manager.mvp.models.explorer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class CloudFile extends Item implements Serializable, Entity, Cloneable {

    @SerializedName("folderId")
    @Expose
    private String folderId = "";

    @SerializedName("version")
    @Expose
    private int version = 0;

    @SerializedName("versionGroup")
    @Expose
    private String versionGroup = "";

    @SerializedName("contentLength")
    @Expose
    private String contentLength = "";

    @SerializedName("pureContentLength")
    @Expose
    private long pureContentLength = 0;

    @SerializedName("fileStatus")
    @Expose
    private String fileStatus = "";

    @SerializedName("viewUrl")
    @Expose
    private String viewUrl = "";

    @SerializedName("webUrl")
    @Expose
    private String webUrl = "";

    @SerializedName("fileType")
    @Expose
    private String fileType = "";

    @SerializedName("fileExst")
    @Expose
    private String fileExst = "";

    @SerializedName("comment")
    @Expose
    private String comment = "";

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public long getPureContentLength() {
        return pureContentLength;
    }

    public void setPureContentLength(long pureContentLength) {
        this.pureContentLength = pureContentLength;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileExst() {
        return fileExst;
    }

    public void setFileExst(String fileExst) {
        this.fileExst = fileExst;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getNextVersion() {
        return ++version;
    }

    public void setFile(CloudFile file) {
        setItem(file);
        folderId = file.getFolderId();
        version = file.getVersion();
        versionGroup = file.getVersionGroup();
        contentLength = file.getContentLength();
        pureContentLength = file.getPureContentLength();
        fileStatus = file.getFileStatus();
        viewUrl = file.getViewUrl();
        webUrl = file.getWebUrl();
        fileType = file.getFileType();
        fileExst = file.getFileExst();
        comment = file.getComment();
    }

    @Override
    public CloudFile clone() {
        return (CloudFile) super.clone();
    }

    @Override
    public int getType(TypeFactory factory) {
        return factory.type(this);
    }

    /*
    * Comparators
    * */
    public static class SortFilesType extends Base.AbstractSort<CloudFile> {

        public SortFilesType(boolean isSortAsc) {
            super(isSortAsc);
        }

        @Override
        public int compare(CloudFile o1, CloudFile o2) {
            return mSortOrder * o1.getFileExst().compareTo(o2.getFileExst());
        }
    }

    public static class SortFilesSize extends Base.AbstractSort<CloudFile> {

        public SortFilesSize(boolean isSortAsc) {
            super(isSortAsc);
        }

        @Override
        public int compare(CloudFile o1, CloudFile o2) {
            return mSortOrder * o1.getContentLength().compareTo(o2.getContentLength());
        }
    }

}