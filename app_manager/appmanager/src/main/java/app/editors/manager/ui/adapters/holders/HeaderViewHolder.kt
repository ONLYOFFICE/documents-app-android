package app.editors.manager.ui.adapters.holders;

import android.view.View;
import android.widget.TextView;

import app.editors.manager.R;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import butterknife.BindView;

public class HeaderViewHolder extends BaseViewHolderExplorer<Header> {

    public static final int LAYOUT = R.layout.list_explorer_header;

    @BindView(R.id.list_explorer_header_title)
    public
    TextView mHeaderTitle;

    public HeaderViewHolder(View itemView, ExplorerAdapter adapter) {
        super(itemView, adapter);
    }

    @Override
    public void bind(Header header) {
        mHeaderTitle.setText(header.getTitle());
    }
}

