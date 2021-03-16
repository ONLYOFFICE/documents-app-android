package lib.toolkit.base.ui.adapters;

import moxy.MvpDelegate;

public abstract class BaseMvpAdapter<D> extends BaseListAdapter<D> {

    private MvpDelegate<? extends BaseMvpAdapter> mMvpDelegate;
    private MvpDelegate<?> mParentDelegate;
    private String mChildId;

    public BaseMvpAdapter(MvpDelegate<?> parentDelegate, String childId) {
        mParentDelegate = parentDelegate;
        mChildId = childId;
        getMvpDelegate().onCreate();
    }

    public MvpDelegate getMvpDelegate() {
        if (mMvpDelegate == null) {
            mMvpDelegate = new MvpDelegate<>(this);
            mMvpDelegate.setParentDelegate(mParentDelegate, mChildId);
        }
        return mMvpDelegate;
    }

}
