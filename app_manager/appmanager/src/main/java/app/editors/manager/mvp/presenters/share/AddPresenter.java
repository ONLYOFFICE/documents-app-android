package app.editors.manager.mvp.presenters.share;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.utils.CollectionUtils;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.models.models.ModelShareStack;
import app.editors.manager.mvp.models.request.RequestShare;
import app.editors.manager.mvp.models.request.RequestShareItem;
import app.editors.manager.mvp.models.response.ResponseGroups;
import app.editors.manager.mvp.models.response.ResponseShare;
import app.editors.manager.mvp.models.response.ResponseUsers;
import app.editors.manager.mvp.models.user.Group;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.share.AddView;
import app.editors.manager.ui.fragments.share.AddFragment;
import moxy.InjectViewState;
import retrofit2.Call;
import retrofit2.Response;

@InjectViewState
public class AddPresenter extends BasePresenter<AddView, ResponseUsers> {

    public static final String TAG = AddPresenter.class.getSimpleName();

    private final String mToken;
    private Item mItem;
    private AddFragment.Type mType;
    private ModelShareStack mModelShareStack;
    private boolean mIsCommon;
    private int mCommonResponse;
    private String mSearchValue;

    private Call<ResponseUsers> mResponseUsersCall;
    private Call<ResponseGroups> mResponseGroupsCall;
    private Call<ResponseShare> mResponseAddCall;

    public AddPresenter() {
        App.getApp().getAppComponent().inject(this);
        mModelShareStack = ModelShareStack.getInstance();
        mToken = mPreferenceTool.getToken();
        mIsCommon = false;
        mCommonResponse = 0;
    }

    /*
    * Requests
    * */
    private void getUsers() {
        mIsCommon = false;
        mResponseUsersCall = mRetrofitTool.getApiWithPreferences().getUsers(mToken, getArgs());
        mResponseUsersCall.enqueue(new CallbackUsersCallback());
    }

    private void getGroups() {
        mIsCommon = false;
        mResponseGroupsCall = mRetrofitTool.getApiWithPreferences().getGroups(mToken, getArgs());
        mResponseGroupsCall.enqueue(new CallbackGroupsResponse());
    }

    public void getCommons() {
        mIsCommon = true;
        mCommonResponse = 0;
        mResponseUsersCall = mRetrofitTool.getApiWithPreferences().getUsers(mToken, getArgs());
        mResponseUsersCall.enqueue(new CallbackUsersCallback());
        mResponseGroupsCall = mRetrofitTool.getApiWithPreferences().getGroups(mToken, getArgs());
        mResponseGroupsCall.enqueue(new CallbackGroupsResponse());
    }

    private void shareFileTo(final String id) {
        final RequestShare requestShare = getRequestShare();
        if (requestShare != null) {
            mResponseAddCall = mRetrofitTool.getApiWithPreferences().setFileAccess(mToken, id, requestShare);
            mResponseAddCall.enqueue(new CallbackAddResponse());
        }
    }

    private void shareFolderTo(final String id) {
        final RequestShare requestShare = getRequestShare();
        if (requestShare != null) {
            mResponseAddCall = mRetrofitTool.getApiWithPreferences().setFolderAccess(mToken, id, requestShare);
            mResponseAddCall.enqueue(new CallbackAddResponse());
        }
    }

    @Nullable
    private RequestShare getRequestShare() {
        final List<RequestShareItem> shareItems = new ArrayList<>();

        // Get users access list
        for (User item : mModelShareStack.getUserSet()) {
            if (item.isSelected()) {
                final String idItem = item.getId();
                final int accessCode = mModelShareStack.getAccessCode();
                final RequestShareItem requestShareItem = new RequestShareItem();
                requestShareItem.setShareTo(idItem);
                requestShareItem.setAccess(String.valueOf(accessCode));
                shareItems.add(requestShareItem);
            }
        }

        // Get groups access list
        for (Group item : mModelShareStack.getGroupSet()) {
            if (item.isSelected()) {
                final String idItem = item.getId();
                final int accessCode = mModelShareStack.getAccessCode();
                final RequestShareItem requestShareItem = new RequestShareItem();
                requestShareItem.setShareTo(idItem);
                requestShareItem.setAccess(String.valueOf(accessCode));
                shareItems.add(requestShareItem);
            }
        }

        if (!shareItems.isEmpty()) {
            final RequestShare requestShare = new RequestShare();
            requestShare.setShare(shareItems);
            final String message = mModelShareStack.getMessage();
            if (message != null && !message.isEmpty()) {
                requestShare.setNotify(true);
                requestShare.setSharingMessage(message);
            }

            return requestShare;
        }

        return null;
    }

    private List<Entity> getCommonList() {
        final List<Entity> commonList = new ArrayList<>();

        // Users
        if (!mModelShareStack.isUserEmpty()) {
            commonList.add(new Header(mContext.getString(R.string.share_add_common_header_users)));
            commonList.addAll(getUserListItems());
        }

        // Groups
        if (!mModelShareStack.isGroupEmpty()) {
            commonList.add(new Header(mContext.getString(R.string.share_add_common_header_groups)));
            commonList.addAll(getGroupListItems());
        }

        return commonList;
    }

    public void getShared() {
        switch (mType) {
            case USERS:
                getUsers();
                break;
            case GROUPS:
                getGroups();
                break;
        }
    }

    public void shareItem() {
        if (mItem instanceof Folder) {
            shareFolderTo(mItem.getId());
        } else if (mItem instanceof File) {
            shareFileTo(mItem.getId());
        }
    }

    /*
    * Update states
    * */
    public void updateTypeSharedListState() {
        switch (mType) {
            case USERS:
                getViewState().onGetUsers(getUserListItems());
                break;
            case GROUPS:
                getViewState().onGetGroups(getGroupListItems());
                break;
        }
    }

    public void updateCommonSharedListState() {
        getViewState().onGetCommon(getCommonList());
    }

    public void updateSearchState() {
        getViewState().onSearchValue(mSearchValue);
    }

    /*
    * Getters/Setters
    * */
    public void setItem(final Item item) {
        mItem = item;
    }

    public void setType(final AddFragment.Type type) {
        mType = type;
    }

    private Map<String, String> getArgs() {
        final Map<String, String> args = new TreeMap<>();
        return args;
    }

    private List<Entity> getUserListItems() {
        return CollectionUtils.convertUsersToItems(mModelShareStack.getUserSet());
    }

    private List<Entity> getGroupListItems() {
        return CollectionUtils.convertGroupsToItems(mModelShareStack.getGroupSet());
    }

    public int getCountChecked() {
        return mModelShareStack.getCountChecked();
    }

    public void resetChecked() {
        mModelShareStack.resetChecked();
    }

    public void setAccessCode(final int accessCode) {
        mModelShareStack.setAccessCode(accessCode);
    }

    public int getAccessCode() {
        return mModelShareStack.getAccessCode();
    }

    public void setMessage(final String message) {
        mModelShareStack.setMessage(message);
    }

    @Nullable
    public void setSearchValue(String searchValue) {
        mSearchValue = searchValue;
    }

    /*
    * Common callback for users
    * */
    private class CallbackUsersCallback extends CommonCallback<ResponseUsers> {

        @Override
        public void onSuccessResponse(Response<ResponseUsers> response) {
            if (!mResponseUsersCall.isCanceled()) {
                mModelShareStack.addUsers(response.body().getResponse());
                mModelShareStack.removeById(mPreferenceTool.getSelfId());
                if (mIsCommon) {
                    if (++mCommonResponse == 2) {
                        getViewState().onGetCommon(getCommonList());
                    }
                } else {
                    getViewState().onGetUsers(getUserListItems());
                }
            }
        }
    }

    /*
    * Common callback for groups
    * */
    private class CallbackGroupsResponse extends CommonCallback<ResponseGroups> {

        @Override
        public void onSuccessResponse(Response<ResponseGroups> response) {
            if (!mResponseGroupsCall.isCanceled()) {
                mModelShareStack.addGroups(response.body().getResponse());
                if (mIsCommon) {
                    if (++mCommonResponse == 2) {
                        getViewState().onGetCommon(getCommonList());
                    }
                } else {
                    getViewState().onGetGroups(getGroupListItems());
                }
            }
        }
    }

    /*
    * Common on added
    * */
    private class CallbackAddResponse extends CommonCallback<ResponseShare> {

        @Override
        public void onSuccessResponse(Response<ResponseShare> response) {
            if (!mResponseAddCall.isCanceled()) {
                mModelShareStack.resetChecked();
                getViewState().onSuccessAdd();
            }
        }
    }

}
