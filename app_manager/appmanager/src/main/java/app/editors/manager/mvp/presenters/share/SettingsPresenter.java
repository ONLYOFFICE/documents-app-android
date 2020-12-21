package app.editors.manager.mvp.presenters.share;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.models.request.RequestExternal;
import app.editors.manager.mvp.models.request.RequestShare;
import app.editors.manager.mvp.models.request.RequestShareItem;
import app.editors.manager.mvp.models.response.ResponseExternal;
import app.editors.manager.mvp.models.response.ResponseShare;
import app.editors.manager.mvp.models.share.Share;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.share.SettingsView;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import moxy.InjectViewState;
import retrofit2.Call;
import retrofit2.Response;

@InjectViewState
public class SettingsPresenter extends BasePresenter<SettingsView, ResponseShare> {

    public static final String TAG = SettingsPresenter.class.getSimpleName();
    private static final String TAG_FOLDER_PATH = "/products/files/#";

    private final String mToken;
    private String mAccessType;
    private String mExternalLink;
    private Item mItem;
    private Share mShareItem;
    private int mSharePosition;
    private boolean mIsAccessDenied;
    private boolean isRemove = false;
    private boolean mIsShare = false;
    private List<Entity> mCommonList;
    private Call<ResponseExternal> mResponseExternalCall;
    private boolean mIsPopupShow = false;

    public SettingsPresenter() {
        App.getApp().getAppComponent().inject(this);
        mToken = mPreferenceTool.getToken();
        mCommonList = new ArrayList<>();
        mIsAccessDenied = false;
    }

    /*
     * Requests
     * */
    private void getShareFolder(final String id) {
        mRequestCall = mRetrofitTool.getApiWithPreferences().getShareFolder(mToken, id);
        mRequestCall.enqueue(new CallbackShare());
    }

    private void getShareFile(final String id) {
        mRequestCall = mRetrofitTool.getApiWithPreferences().getShareFile(mToken, id);
        mRequestCall.enqueue(new CallbackShare());
    }

    private void setShareFile(final String id, final boolean isNotify, @Nullable final String message,
                              @NonNull final Share... shareList) {
        final RequestShare requestShare = getRequestShare(isNotify, message, shareList);
        if (requestShare != null) {
            mRequestCall = mRetrofitTool.getApiWithPreferences().setFileAccess(mToken, id, requestShare);
            mRequestCall.enqueue(new CallbackShare());
        }
    }

    private void setShareFolder(@NonNull final String id, final boolean isNotify,
                                @Nullable final String message, @NonNull Share... shareList) {
        final RequestShare requestShare = getRequestShare(isNotify, message, shareList);
        if (requestShare != null) {
            mRequestCall = mRetrofitTool.getApiWithPreferences().setFolderAccess(mToken, id, requestShare);
            mRequestCall.enqueue(new CallbackShare());
        }
    }

    private boolean isShared(final int accessCode) {
        final boolean isShared = accessCode != Api.ShareCode.RESTRICT && accessCode != Api.ShareCode.NONE;
        getViewState().onResultState(isShared);
        return isShared;
    }

    @Nullable
    private RequestShare getRequestShare(final boolean isNotify, @Nullable final String message,
                                         @NonNull final Share... shareList) {
        final RequestShare requestShare = new RequestShare();
        final List<RequestShareItem> shareRights = new ArrayList<>();
        for (Share item : shareList) {
            final String idItem = item.getSharedTo().getId();
            final int accessCode = item.getNewAccess();
            final RequestShareItem requestShareFolder = new RequestShareItem();
            requestShareFolder.setShareTo(idItem);
            requestShareFolder.setAccess(String.valueOf(accessCode));
            shareRights.add(requestShareFolder);
        }

        if (!shareRights.isEmpty()) {
            requestShare.setShare(shareRights);
            requestShare.setNotify(isNotify);
            if (message != null) {
                requestShare.setSharingMessage(message);
            }

            return requestShare;
        }

        return null;
    }

    public void getShared() {
        if (mItem instanceof Folder) {
            getShareFolder(mItem.getId());
        } else if (mItem instanceof File) {
            getShareFile(mItem.getId());
        }
    }

    public void getInternalLink() {
        if (mItem instanceof Folder) {
            final String internalLink = mPreferenceTool.getPortalFullPath() + TAG_FOLDER_PATH + mItem.getId();
            getViewState().onInternalLink(internalLink);
        } else if (mItem instanceof File) {
            getViewState().onInternalLink(((File) mItem).getWebUrl());
        }
    }

    public void getExternalLink(final String share) {
        mAccessType = share;
        final RequestExternal requestExternal = new RequestExternal();
        requestExternal.setShare(mAccessType);
        mResponseExternalCall = mRetrofitTool.getApiWithPreferences().getExternalLink(mToken, mItem.getId(), requestExternal);
        mResponseExternalCall.enqueue(new CallbackExternal());
    }

    public void setItemAccess(final int accessCode) {
        if (mItem != null && mShareItem.getAccess() != accessCode) {
            mShareItem.setNewAccess(accessCode);
            if (accessCode == Api.ShareCode.NONE) {
                getViewState().onRemove(mShareItem, mSharePosition);
                isRemove = true;
            }
            if (mItem instanceof Folder) {
                setShareFolder(mItem.getId(), false, null, mShareItem);
            } else {
                setShareFile(mItem.getId(), false, null, mShareItem);
            }
        }
    }

    /*
     * Update states
     * */
    public void updateSharedListState() {
        getViewState().onGetShare(mCommonList, mItem.getAccess());
        if (mIsPopupShow) {
            mIsPopupShow = false;
            getViewState().onShowPopup(mSharePosition);
        }
    }

    public void updateSharedExternalState(boolean isMessage) {
        getViewState().onExternalAccess(mItem.getAccess(), isMessage);
    }

    public void updateActionButtonState() {
        getViewState().onActionButtonState(!mIsAccessDenied);
    }

    public void updatePlaceholderState() {
        if (mIsAccessDenied) {
            getViewState().onPlaceholderState(PlaceholderViews.Type.ACCESS);
        } else if (mCommonList == null || mCommonList.isEmpty()) {
            getViewState().onPlaceholderState(PlaceholderViews.Type.SHARE);
        } else {
            getViewState().onPlaceholderState(PlaceholderViews.Type.NONE);
        }
    }

    public void updateHeaderState() {
        getViewState().onItemType(mItem instanceof Folder);
    }

    /*
     * Getters/Setters
     * */
    public void setItem(final Item item) {
        mItem = item;
    }

    public Item getItem() {
        return mItem;
    }

    public void setShared(final Share share, final int position) {
        mShareItem = share;
        mSharePosition = position;
        mIsShare = true;
    }

    public void addShareItems() {
        getViewState().onAddShare(mItem);
    }

    @Nullable
    public String getExternalLink() {
        return mExternalLink;
    }

    public void sendLink(String externalLink) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_TEXT, externalLink);
        getViewState().onSendLink(intent);
    }

    public int getSharePosition() {
        return mSharePosition;
    }

    public void setIsPopupShow(boolean isPopupShow) {
        this.mIsPopupShow = isPopupShow;
    }

    /*
     * Callbacks for response
     * */
    private class CallbackShare extends BaseCallback {

        @Override
        public void onSuccessResponse(Response<ResponseShare> response) {
            mIsAccessDenied = false;
            // Sort for users/groups
            final List<Share> shareList = response.body().getResponse();
            final List<Entity> usersList = new ArrayList<>();
            final List<Entity> groupsList = new ArrayList<>();
            mCommonList.clear();

            for (Share item : shareList) {
                if (item.getSharedTo().getUserName() != null && !item.getIsOwner()) {
                    usersList.add(item);
                } else if (item.getSharedTo().getName() != null) {
                    groupsList.add(item);
                } else if (item.getSharedTo().getShareLink() != null) {
                    final int accessCode = item.getAccess();
                    mItem.setAccess(accessCode);
                    mItem.setShared(isShared(accessCode));
                    mExternalLink = item.getSharedTo().getShareLink();
                }
            }

            // Get result
            // Users
            if (!usersList.isEmpty()) {
                mCommonList.add(new Header(mContext.getString(R.string.share_goal_user)));
                mCommonList.addAll(usersList);
            }

            // Groups
            if (!groupsList.isEmpty()) {
                mCommonList.add(new Header(mContext.getString(R.string.share_goal_group)));
                mCommonList.addAll(groupsList);
            }

            if (isRemove) {
                if (mCommonList.isEmpty()) {
                    getViewState().onPlaceholderState(mCommonList != null && !mCommonList.isEmpty() ?
                            PlaceholderViews.Type.NONE : PlaceholderViews.Type.SHARE);
                }
                isRemove = false;
                mIsShare = false;
                return;
            }
            getViewState().onActionButtonState(true);
            if (mIsShare && !mCommonList.isEmpty()) {
                getViewState().onGetShareItem(mCommonList.get(mSharePosition), mSharePosition, mItem.getAccess());
                mIsShare = false;
            } else {
                getViewState().onGetShare(mCommonList, mItem.getAccess());
            }
            getViewState().onPlaceholderState(mCommonList != null && !mCommonList.isEmpty() ?
                    PlaceholderViews.Type.NONE : PlaceholderViews.Type.SHARE);
        }

        @Override
        public void onErrorResponse(Response<ResponseShare> response) {
            super.onErrorResponse(response);
            mIsAccessDenied = false;
            if (response.code() == Api.HttpCodes.CLIENT_FORBIDDEN) {
                mIsAccessDenied = true;
                getViewState().onPlaceholderState(PlaceholderViews.Type.ACCESS);
                getViewState().onPopupState(false);
                getViewState().onButtonState(false);
                getViewState().onActionButtonState(false);
            }
        }
    }

    private class CallbackExternal extends CommonCallback<ResponseExternal> {

        @Override
        public void onSuccessResponse(Response<ResponseExternal> response) {
            final int accessCode = Api.ShareType.getCode(mAccessType);
            mExternalLink = response.body().getResponse();
            mItem.setAccess(accessCode);
            mItem.setShared(isShared(accessCode));
            getViewState().onExternalAccess(accessCode, true);
        }
    }

}


