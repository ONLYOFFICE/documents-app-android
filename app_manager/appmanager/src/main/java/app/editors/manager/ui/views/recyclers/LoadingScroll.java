package app.editors.manager.ui.views.recyclers;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

abstract public class LoadingScroll extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_THRESHOLD = 5;
    private boolean mIsDown = false;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            final int visibleItemCount = layoutManager.getChildCount();
            final int totalItemCount = layoutManager.getItemCount();
            final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (mIsDown && visibleItemCount + firstVisibleItemPosition >= totalItemCount - VISIBLE_THRESHOLD) {
                mIsDown = false;
                onListEnd();
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        mIsDown = dy > 0;
    }

    abstract public void onListEnd();

}