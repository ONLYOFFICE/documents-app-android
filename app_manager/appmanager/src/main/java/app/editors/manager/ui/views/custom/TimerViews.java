package app.editors.manager.ui.views.custom;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import app.editors.manager.ui.fragments.login.EnterpriseSmsFragment;

public class TimerViews extends AsyncTask<Void, Integer, Void> {

    private static final int SLEEP_TIMER = 1000;

    private WeakReference<EnterpriseSmsFragment> mWeakReference;
    private final int mTimerTime;
    private int mCurrentTimer;
    private boolean mIsActive;

    public TimerViews(final int time) {
        mTimerTime = time;
        mIsActive = false;
    }

    public void setFragment(EnterpriseSmsFragment callback) {
        mWeakReference = new WeakReference<>(callback);
    }

    public void removeFragment() {
        if (mWeakReference != null) {
            mWeakReference.clear();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mIsActive = true;
        mCurrentTimer = mTimerTime;
        try {
            while (mCurrentTimer-- > 1 && !isCancelled()) {
                publishProgress(mCurrentTimer);
                Thread.sleep(SLEEP_TIMER);
            }
        } catch (InterruptedException e) {
            // No need handle
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mWeakReference != null) {
            final EnterpriseSmsFragment inputSmsFragment = mWeakReference.get();
            if (inputSmsFragment != null) {
                inputSmsFragment.setTimerButton(values[0]);
            }
        }
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        mIsActive = false;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mIsActive = false;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mIsActive = false;
        if (mWeakReference != null) {
            final EnterpriseSmsFragment inputSmsFragment = mWeakReference.get();
            if (inputSmsFragment != null) {
                inputSmsFragment.setTimerButton();
            }
        }
    }

    public int getCurrentTimer() {
        return mCurrentTimer;
    }

    public boolean isActive() {
        return mIsActive;
    }
}