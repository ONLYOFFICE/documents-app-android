package lib.toolkit.base.ui.adapters;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseAdapter extends RecyclerView.Adapter {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_ITEM_ONE = 2;
    public static final int TYPE_ITEM_TWO = 3;
    public static final int TYPE_FOOTER = 4;
    public static final int TYPE_STUB = 5;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public interface OnItemCheckListener {
        void onItemCheck(View view, int position);
    }

    public interface OnItemContextListener {
        void onItemContextClick(View view, int position);
    }

    public OnItemClickListener mOnItemClickListener;
    public OnItemLongClickListener mOnItemLongClickListener;
    public OnItemCheckListener mOnItemCheckListener;
    public OnItemContextListener mOnItemContextListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public void setOnItemCheckListener(OnItemCheckListener listener) {
        mOnItemCheckListener = listener;
    }

    public void setOnItemContextListener(OnItemContextListener listener) {
        mOnItemContextListener = listener;
    }

}
