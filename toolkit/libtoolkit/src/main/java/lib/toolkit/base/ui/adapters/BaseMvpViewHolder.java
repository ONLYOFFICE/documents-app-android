package lib.toolkit.base.ui.adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import moxy.MvpDelegate;
import moxy.MvpPresenter;
import moxy.presenter.ProvidePresenter;

public abstract class BaseMvpViewHolder extends RecyclerView.ViewHolder {

    protected MvpDelegate mParentDelegate;
    protected MvpDelegate mMvpDelegate;
    protected int mPosition = -1;

    public BaseMvpViewHolder(@NonNull final View view, @NonNull final MvpDelegate<?> parentDelegate) {
        super(view);
        mParentDelegate = parentDelegate;
    }

    protected MvpDelegate getMvpDelegate() {
        if (mMvpDelegate == null) {
            mMvpDelegate = new MvpDelegate<>(this);
            mMvpDelegate.setParentDelegate(mParentDelegate, String.valueOf(mPosition));
        }
        return mMvpDelegate;
    }

    public void bind(int position) {
        unbind();
        mPosition = position;
        getMvpDelegate().onCreate();
        getMvpDelegate().onAttach();
        onViewsInit();
    }

    protected void unbind() {
        if (mPosition >= 0) {
            getMvpDelegate().onSaveInstanceState();
            getMvpDelegate().onDetach();
            getMvpDelegate().onDestroyView();
            mMvpDelegate = null;
        }
    }

    @ProvidePresenter
    protected abstract MvpPresenter providePresenter();

    protected abstract void onViewsInit();

}
