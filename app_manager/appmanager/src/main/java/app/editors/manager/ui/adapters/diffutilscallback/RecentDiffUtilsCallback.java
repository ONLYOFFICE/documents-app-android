package app.editors.manager.ui.adapters.diffutilscallback;

import java.util.List;

import app.editors.manager.mvp.models.account.Recent;
import app.editors.manager.mvp.models.base.Entity;

public class RecentDiffUtilsCallback extends BaseDiffUtilsCallback<Entity> {

    public RecentDiffUtilsCallback(List<Entity> mNewList, List<Entity> mOldList) {
        super(mNewList, mOldList);
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        if (mOldList.get(oldItemPosition) instanceof Recent && mNewList.get(newItemPosition) instanceof Recent) {
            Recent oldRecent = (Recent) mOldList.get(oldItemPosition);
            Recent newRecent = (Recent) mNewList.get(newItemPosition);
            return oldRecent.getId().equals(newRecent.getId());
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (mOldList.get(oldItemPosition) instanceof Recent && mNewList.get(newItemPosition) instanceof Recent) {
            Recent oldRecent = (Recent) mOldList.get(oldItemPosition);
            Recent newRecent = (Recent) mNewList.get(newItemPosition);
            return oldRecent.getDate().equals(newRecent.getDate());
        }
        return false;
    }
}
