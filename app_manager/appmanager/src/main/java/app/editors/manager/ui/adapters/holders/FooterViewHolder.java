package app.editors.manager.ui.adapters.holders;

import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import app.editors.manager.R;
import app.editors.manager.mvp.models.list.Footer;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import butterknife.BindView;

public class FooterViewHolder extends BaseViewHolderExplorer<Footer> {

    public static final int LAYOUT = R.layout.list_explorer_footer;

    @BindView(R.id.list_explorer_footer_layout)
    ConstraintLayout mFooterLayout;
    @BindView(R.id.list_explorer_footer_progress)
    ProgressBar mFooterProgress;

    public FooterViewHolder(View parent, ExplorerAdapter adapter) {
        super(parent, adapter);
        mFooterProgress.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(adapter.mContext, R.color.colorAccent),
                        PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void bind(Footer footer) {
        mFooterLayout.setVisibility(mAdapter.isFooter() ? View.VISIBLE : View.INVISIBLE);
    }


}
