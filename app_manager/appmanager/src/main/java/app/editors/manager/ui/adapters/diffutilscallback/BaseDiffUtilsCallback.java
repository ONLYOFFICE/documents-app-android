package app.editors.manager.ui.adapters.diffutilscallback;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public abstract class BaseDiffUtilsCallback<D> extends DiffUtil.Callback {

    protected List<D> mNewList;
    protected List<D> mOldList;

    public BaseDiffUtilsCallback(List<D> mNewList, List<D> mOldList) {
        this.mNewList = mNewList;
        this.mOldList = mOldList;
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }
}
