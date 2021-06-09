package lib.toolkit.base.ui.adapters;

import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<D> extends BaseAdapter {

    protected List<D> mList;

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public void addItems(final List<D> list) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.addAll(list);
        notifyItemRangeInserted(mList.size(), list.size());
    }

    public void addItemsAtTop(final List<D> list) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.addAll(0, list);
        notifyItemRangeInserted(0, list.size());
    }

    public void setItems(final List<D> list) {
        if (list == null) {
            return;
        }

        if (mList != null) {
            mList.clear();
        } else {
            mList = new ArrayList<>();
        }
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void setItem(final D item, final int position) {
        if (getItem(position) != null) {
            mList.set(position, item);
            notifyItemChanged(position);
        }
    }

    public int addItem(final D item) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(item);
        notifyItemInserted(mList.size());
        return mList.size();
    }

    public void addItemAtTop(final D item) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(0, item);
        notifyItemInserted(0);
    }

    public List<D> getItemList() {
        return mList;
    }

    public D getItem(int index) {
        return mList != null && index >= 0 && index < mList.size() ? mList.get(index) : null;
    }

    public void clear() {
        if (mList != null) {
            mList.clear();
            notifyItemRangeRemoved(0, mList.size());
        }
    }

    public void removeItem(int position) {
        if (getItem(position) != null) {
            mList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeItem(D item) {
        if (mList != null && !mList.isEmpty() && mList.contains(item)) {
            int position = mList.indexOf(item);
            mList.remove(item);
            notifyItemRemoved(position);
        }
    }

    public void removeItems(List<D> items) {
        if (mList != null && !items.isEmpty()) {
            for (D item: items) {
                int position = mList.indexOf(item);
                mList.remove(item);
                notifyItemRemoved(position);
            }
        }
    }

    public void updateItem(D item) {
        if (mList != null && !mList.isEmpty()) {
            int position = mList.indexOf(item);
            if (position != -1) {
                mList.set(position, item);
                notifyItemChanged(position, item);
            }
        }
    }

    public void updateItem(D item, int position) {
        if (mList != null && !mList.isEmpty()) {
            if (position != -1) {
                mList.set(position, item);
                notifyItemChanged(position, item);
            }
        }
    }

    public void set(List<D> list, DiffUtil.DiffResult result) {
        if (list != null) {
            mList.clear();
            mList.addAll(list);
            result.dispatchUpdatesTo(this);
        }
    }

    public void setData(List<D> list) {
        mList = list;
    }
}
