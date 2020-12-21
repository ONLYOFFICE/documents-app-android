package app.editors.manager.mvp.models.models;


import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import app.editors.manager.app.Api;
import app.editors.manager.mvp.models.user.Group;
import app.editors.manager.mvp.models.user.User;

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

    private Set<User> mUserSet;
    private Set<Group> mGroupSet;
    private int mAccessCode;
    private String mMessage;
    private boolean mIsRefresh;

    public ModelShareStack() {
        mAccessCode = Api.ShareCode.READ;
        mUserSet = new TreeSet<>();
        mGroupSet = new TreeSet<>();
        mIsRefresh = false;
    }

    public int getCountChecked() {
        int count = 0;
        for (User item : mUserSet) {
            if (item.isSelected()) {
                ++count;
            }
        }

        for (Group item : mGroupSet) {
            if (item.isSelected()) {
                ++count;
            }
        }

        return count;
    }

    public void resetChecked() {
        for (User item : mUserSet) {
            item.setSelected(false);
        }

        for (Group item : mGroupSet) {
            item.setSelected(false);
        }
    }

    public void clearModel() {
        mIsRefresh = false;
        mUserSet.clear();
        mGroupSet.clear();
    }

    public Set<User> getUserSet() {
        return mUserSet;
    }

    public boolean removeById(final String id) {
        final Iterator<User> userSet = getUserSet().iterator();
        while(userSet.hasNext()) {
            final User user = userSet.next();
            if (user.getId().equalsIgnoreCase(id)) {
                userSet.remove();
                return true;
            }
        }

        final Iterator<Group> groupSet = getGroupSet().iterator();
        while(groupSet.hasNext()) {
            final Group group = groupSet.next();
            if (group.getId().equalsIgnoreCase(id)) {
                groupSet.remove();
                return true;
            }
        }

        return false;
    }

    public Set<Group> getGroupSet() {
        return mGroupSet;
    }

    public boolean isUserEmpty() {
        return mUserSet.isEmpty();
    }

    public boolean isGroupEmpty() {
        return mGroupSet.isEmpty();
    }

    public void addUser(final User user) {
        mUserSet.add(user);
    }

    public void addUsers(final List<User> userList) {
        mUserSet.addAll(userList);
    }

    public void addGroup(final Group group) {
        mGroupSet.add(group);
    }

    public void addGroups(final List<Group> groupList) {
        mGroupSet.addAll(groupList);
    }

    public int getAccessCode() {
        return mAccessCode;
    }

    public void setAccessCode(int accessCode) {
        mAccessCode = accessCode;
    }

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
