package app.editors.manager.mvp.models.models;


import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import app.documents.core.network.ApiContract;
import app.editors.manager.mvp.models.ui.GroupUi;
import app.editors.manager.mvp.models.ui.UserUi;

public class ModelShareStack {

    private static WeakReference<ModelShareStack> sWeakReference;

    public static ModelShareStack getInstance() {
        ModelShareStack modelShareStack;
        if (sWeakReference == null || sWeakReference.get() == null) {
            modelShareStack = new ModelShareStack();
            sWeakReference = new WeakReference<>(modelShareStack);
        } else {
            modelShareStack = sWeakReference.get();
        }

        return modelShareStack;
    }

    private Set<UserUi> mUserSet;
    private Set<GroupUi> mGroupSet;
    private int mAccessCode;
    private String mMessage;
    private boolean mIsRefresh;

    public ModelShareStack() {
        mAccessCode = ApiContract.ShareCode.READ;
        mUserSet = new TreeSet<>();
        mGroupSet = new TreeSet<>();
        mIsRefresh = false;
    }

    public int getCountChecked() {
        int count = 0;
        for (UserUi item : mUserSet) {
            if (item.isSelected()) {
                ++count;
            }
        }

        for (GroupUi item : mGroupSet) {
            if (item.isSelected()) {
                ++count;
            }
        }

        return count;
    }

    public void resetChecked() {
        for (UserUi item : mUserSet) {
            item.setSelected(false);
        }

        for (GroupUi item : mGroupSet) {
            item.setSelected(false);
        }
    }

    public void clearModel() {
        mIsRefresh = false;
        mUserSet.clear();
        mGroupSet.clear();
    }

    public Set<UserUi> getUserSet() {
        return mUserSet;
    }

    public boolean removeById(final String id) {
        final Iterator<UserUi> userSet = getUserSet().iterator();
        while(userSet.hasNext()) {
            final UserUi user = userSet.next();
            if (user.getId().equalsIgnoreCase(id)) {
                userSet.remove();
                return true;
            }
        }

        final Iterator<GroupUi> groupSet = getGroupSet().iterator();
        while(groupSet.hasNext()) {
            final GroupUi group = groupSet.next();
            if (group.getId().equalsIgnoreCase(id)) {
                groupSet.remove();
                return true;
            }
        }

        return false;
    }

    public Set<GroupUi> getGroupSet() {
        return mGroupSet;
    }

    public boolean isUserEmpty() {
        return mUserSet.isEmpty();
    }

    public boolean isGroupEmpty() {
        return mGroupSet.isEmpty();
    }

    public void addUser(final UserUi user) {
        mUserSet.add(user);
    }

    public void addUsers(final List<UserUi> userList) {
        mUserSet.addAll(userList);
    }

    public void addGroup(final GroupUi group) {
        mGroupSet.add(group);
    }

    public void addGroups(final List<GroupUi> groupList) {
        mGroupSet.addAll(groupList);
    }

    public int getAccessCode() {
        return mAccessCode;
    }

    public void setAccessCode(int accessCode) {
        mAccessCode = accessCode;
    }

    @Nullable
    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public boolean isRefresh() {
        return mIsRefresh;
    }

    public void setRefresh(boolean refresh) {
        mIsRefresh = refresh;
    }

}
