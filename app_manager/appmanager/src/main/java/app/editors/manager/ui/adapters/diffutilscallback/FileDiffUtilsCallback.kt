package app.editors.manager.ui.adapters.diffutilscallback;

import java.io.File;
import java.util.List;

public class FileDiffUtilsCallback extends BaseDiffUtilsCallback<File> {

    public FileDiffUtilsCallback(List<File> mNewList, List<File> mOldList) {
        super(mNewList, mOldList);
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        File oldFile = mOldList.get(oldItemPosition);
        File newFile = mNewList.get(newItemPosition);
        return oldFile.getAbsolutePath().equals(newFile.getAbsolutePath());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        File oldFile = mOldList.get(oldItemPosition);
        File newFile = mNewList.get(newItemPosition);
        return oldFile.lastModified() == newFile.lastModified();
    }
}
