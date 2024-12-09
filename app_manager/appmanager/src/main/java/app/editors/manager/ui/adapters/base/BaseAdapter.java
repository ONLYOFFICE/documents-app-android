package app.editors.manager.ui.adapters.base;


import app.documents.core.network.manager.models.explorer.UploadFile;

public abstract class BaseAdapter<D> extends lib.toolkit.base.ui.adapters.BaseListAdapter<D> {

    @Override
    public void updateItem(D item) {
        if (mList != null && !mList.isEmpty()) {
            int position = mList.indexOf(item);

            if (position != -1) {
                mList.set(position, item);
                if (item instanceof UploadFile) {
                    notifyItemChanged(position, item);
                } else {
                    notifyItemChanged(position);
                }
            }
        }
    }
}
