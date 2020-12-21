package app.editors.manager.mvp.models.explorer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class UploadFile implements Entity, Parcelable {

    private String id;
    private String folderId;
    private String name;
    private Uri uri;
    private String size;
    private int progress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public int getType(TypeFactory factory) {
        return factory.type(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadFile that = (UploadFile) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(folderId, that.folderId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(uri, that.uri) &&
                Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, folderId, name, uri, size);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.folderId);
        dest.writeString(this.name);
        dest.writeParcelable(this.uri, flags);
        dest.writeString(this.size);
        dest.writeInt(this.progress);
    }

    public UploadFile() {
    }

    protected UploadFile(Parcel in) {
        this.id = in.readString();
        this.folderId = in.readString();
        this.name = in.readString();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.size = in.readString();
        this.progress = in.readInt();
    }

    public static final Parcelable.Creator<UploadFile> CREATOR = new Parcelable.Creator<UploadFile>() {
        @Override
        public UploadFile createFromParcel(Parcel source) {
            return new UploadFile(source);
        }

        @Override
        public UploadFile[] newArray(int size) {
            return new UploadFile[size];
        }
    };
}
