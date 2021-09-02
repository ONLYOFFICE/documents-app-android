package app.editors.manager.ui.adapters.holders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import app.editors.manager.ui.adapters.ExplorerAdapter;
import butterknife.ButterKnife;

public abstract class BaseViewHolderExplorer<T> extends RecyclerView.ViewHolder {

    protected static final String PLACEHOLDER_POINT = " • ";

    protected ExplorerAdapter mAdapter;

    public BaseViewHolderExplorer(View itemView, ExplorerAdapter adapter) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mAdapter = adapter;
    }

    public abstract void bind(T element);
}
