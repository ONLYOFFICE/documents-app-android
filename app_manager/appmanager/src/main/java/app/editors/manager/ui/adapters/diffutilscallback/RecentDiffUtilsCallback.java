package app.editors.manager.ui.adapters.diffutilscallback;

import java.util.List;

import app.documents.core.account.Recent;


public class RecentDiffUtilsCallback extends BaseDiffUtilsCallback<Recent> {

    public RecentDiffUtilsCallback(List<Recent> mNewList, List<Recent> mOldList) {
        super(mNewList, mOldList);
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        if (mOldList.get(oldItemPosition) instanceof Recent && mNewList.get(newItemPosition) instanceof Recent) {
            Recent oldRecent = (Recent) mOldList.get(oldItemPosition);
            Recent newRecent = (Recent) mNewList.get(newItemPosition);
            return oldRecent.getId() == newRecent.getId();
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (mOldList.get(oldItemPosition) instanceof Recent && mNewList.get(newItemPosition) instanceof Recent) {
            Recent oldRecent = (Recent) mOldList.get(oldItemPosition);
            Recent newRecent = (Recent) mNewList.get(newItemPosition);
            return oldRecent.getDate() == newRecent.getDate();
        }
        return false;
    }
}
