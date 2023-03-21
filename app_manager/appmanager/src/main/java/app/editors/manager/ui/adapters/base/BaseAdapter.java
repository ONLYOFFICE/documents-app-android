package app.editors.manager.ui.adapters.base;


import app.documents.core.network.manager.models.explorer.UploadFile;
import app.editors.manager.mvp.models.list.Header;

public abstract class BaseAdapter<D> extends lib.toolkit.base.ui.adapters.BaseListAdapter<D> {

    public void removeHeader(String title) {
        for (D item : mList) {
            if (item instanceof Header) {
                Header head = (Header) item;
                if (head.getTitle().equals(title)) {
                    removeItem((D) head);
                    return;
                }
            }
        }
    }

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
