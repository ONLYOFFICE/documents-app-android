package app.editors.manager.ui.adapters.diffutilscallback;

import java.util.List;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.list.Footer;
import app.editors.manager.mvp.models.list.Header;

public class EntityDiffUtilsCallback extends BaseDiffUtilsCallback<Entity> {

    public EntityDiffUtilsCallback(List<Entity> mNewList, List<Entity> mOldList) {
        super(mNewList, mOldList);
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Entity oldEntity = mOldList.get(oldItemPosition);
        Entity newEntity = mNewList.get(newItemPosition);
        if (oldEntity instanceof Header && newEntity instanceof Header) {
            return true;
        } else if (oldEntity instanceof File && newEntity instanceof File) {
            File newFile = (File) newEntity;
            File oldFile = (File) oldEntity;
            return newFile.getId().equals(oldFile.getId());
        } else if (oldEntity instanceof Folder && newEntity instanceof Folder) {
            Folder newFolder = (Folder) newEntity;
            Folder oldFolder = (Folder) oldEntity;
            return newFolder.getId().equals(oldFolder.getId());
        } else return oldEntity instanceof Footer && newEntity instanceof Footer;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Entity oldEntity = mOldList.get(oldItemPosition);
        Entity newEntity = mNewList.get(newItemPosition);
        if (oldEntity instanceof Header && newEntity instanceof Header) {
            Header newHeader = (Header) newEntity;
            Header oldHeader = (Header) oldEntity;
            return newHeader.getTitle().equals(oldHeader.getTitle());
        } else if (oldEntity instanceof File && newEntity instanceof File) {
            File newFile = (File) newEntity;
            File oldFile = (File) oldEntity;
            return newFile.getTitle().equals(oldFile.getTitle()) && newFile.getVersion() == oldFile.getVersion();
        } else if (oldEntity instanceof Folder && newEntity instanceof Folder) {
            Folder newFolder = (Folder) newEntity;
            Folder oldFolder = (Folder) oldEntity;
            return newFolder.getTitle().equals(oldFolder.getTitle()) && newFolder.getFilesCount() == oldFolder.getFilesCount();
        } else return oldEntity instanceof Footer && newEntity instanceof Footer;
    }
}
