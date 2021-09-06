package app.editors.manager.ui.views.custom;


import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import app.editors.manager.R;
import app.editors.manager.managers.exceptions.ButterknifeInitException;
import app.editors.manager.managers.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SocialViews {

    public static final String TAG = SocialViews.class.getSimpleName();
    public static final int GOOGLE_PERMISSION = 1212;
    private static final int RC_SIGN_IN = 9001;

    public interface OnSocialNetworkCallbacks {
        void onTwitterSuccess(String token);
        void onTwitterFailed();
        void onFacebookSuccess(String token);
        void onFacebookLogin(String message);
        void onFacebookCancel();
        void onFacebookFailed();
        void onGoogleSuccess(Account account);
        void onGoogleFailed();
    }

    @BindView(R.id.login_social_facebook_button)
    protected AppCompatImageButton mLoginPersonalSocialFacebookButton;
    @BindView(R.id.login_social_google_button)
    protected AppCompatImageButton mLoginPersonalSocialGoogleButton;
    @BindView(R.id.login_social_twitter_button)
    protected AppCompatImageButton mLoginPersonalSocialTwitterButton;
    @BindView(R.id.login_social_linkedin_button)
    protected AppCompatImageButton mLoginPersonalSocialLinkedinButton;

    private Activity mActivity;
    private Unbinder mUnbinder;
    private String mFacebookId;

    private OnSocialNetworkCallbacks mOnSocialNetworkCallbacks;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mFacebookCallbackManager;

    private LoginButton mLoginPersonalSocialFacebookNativeButton;

    public SocialViews(final Activity activity, final View view, @Nullable final String facebookId) {
        try {
            mUnbinder = ButterKnife.bind(this, view);
        } catch (RuntimeException e) {
            throw new ButterknifeInitException(SocialViews.class.getSimpleName() + " - must initWithPreferences with specific view!", e);
        }

        mActivity = activity;
        mFacebookId = facebookId;
        initFacebook();
    }

    public void showGoogleLogin(boolean isShow) {
        if (isShow) {
            mLoginPersonalSocialGoogleButton.setVisibility(View.VISIBLE);
        } else {
            mLoginPersonalSocialGoogleButton.setVisibility(View.GONE);
        }
    }

    public void showFacebookLogin(boolean isShow) {
        if (isShow) {
            mLoginPersonalSocialFacebookButton.setVisibility(View.VISIBLE);
        } else {
            mLoginPersonalSocialFacebookButton.setVisibility(View.GONE);
        }
    }


    /*
    * Facebook initWithPreferences
    * */
    private void initFacebook() {
        Log.d(TAG, "initFacebook() - app ID: " + mActivity.getString(R.string.facebook_app_id));
        if (mFacebookId != null) {
            FacebookSdk.setApplicationId(mFacebookId);
        }

        mLoginPersonalSocialFacebookNativeButton = new LoginButton(mActivity);
        mLoginPersonalSocialFacebookNativeButton.setLoginBehavior(LoginBehavior.WEB_ONLY);
        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookAuthCallback());
    }

    /*
    * Actions
    * */
    @OnClick({ R.id.login_social_twitter_button,
                     R.id.login_social_facebook_button,
                     R.id.login_social_google_button })
    public void onButtonsClick(final View view) {
        switch (view.getId()) {
            case R.id.login_social_facebook_button:
                onFacebookClick();
                break;
            case R.id.login_social_google_button:
                onGoogleClick();
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    public void onGoogleClick() {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_WEB_ID)
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(mActivity, gso);
        mGoogleSignInClient.signOut();
        final Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        mActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @SuppressLint("RestrictedApi")
    private void getGoogleToken(Task<GoogleSignInAccount> completedTask) {
        if (mOnSocialNetworkCallbacks != null) {
            try {
                final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                mOnSocialNetworkCallbacks.onGoogleSuccess(account.getAccount());
            } catch (ApiException e) {
                Log.e(TAG, "Status code: " + e.getStatusCode(), e);
                if (mGoogleSignInClient != null) {
                    mGoogleSignInClient.signOut();
                }
                mOnSocialNetworkCallbacks.onGoogleFailed();
            } catch (Exception e) {
                if (mGoogleSignInClient != null) {
                    mGoogleSignInClient.signOut();
                }
                mOnSocialNetworkCallbacks.onGoogleFailed();
            }
        }
    }

    /*
    * Facebook click. Get previous token or get new with button click.
    * */
    private void onFacebookClick() {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (mOnSocialNetworkCallbacks != null) {
            if (accessToken != null) {
                mOnSocialNetworkCallbacks.onFacebookLogin(accessToken.getUserId());
            } else {
                mLoginPersonalSocialFacebookNativeButton.performClick();
            }
        }
    }

    public void onFacebookContinue() {
        mOnSocialNetworkCallbacks.onFacebookSuccess(AccessToken.getCurrentAccessToken().getToken());
    }

    public void onFacebookLogout() {
        LoginManager.getInstance().logOut();
        mLoginPersonalSocialFacebookNativeButton.performClick();
    }


    /*
    * Lifecycle methods
    * */
    public void onDestroyView() {
        LoginManager.getInstance().unregisterCallback(mFacebookCallbackManager);
        setOnSocialNetworkCallbacks(null);
        mUnbinder.unbind();
    }

    @SuppressLint("RestrictedApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            getGoogleToken(GoogleSignIn.getSignedInAccountFromIntent(data));
        } else {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*
    * Getters/Setters
    * */
    public void setOnSocialNetworkCallbacks(OnSocialNetworkCallbacks onSocialNetworkCallbacks) {
        mOnSocialNetworkCallbacks = onSocialNetworkCallbacks;
    }

    /*
    * Facebook callback
    * */
    private class FacebookAuthCallback implements FacebookCallback<LoginResult> {

        @Override
        public void onSuccess(LoginResult loginResult) {
            if (mOnSocialNetworkCallbacks != null) {
                mOnSocialNetworkCallbacks.onFacebookSuccess(loginResult.getAccessToken().getToken());
            }
        }

        @Override
        public void onCancel() {
            if (mOnSocialNetworkCallbacks != null) {
                mOnSocialNetworkCallbacks.onFacebookCancel();
            }
        }

        @Override
        public void onError(FacebookException exception) {
            LoginManager.getInstance().logOut();
            if (mOnSocialNetworkCallbacks != null) {
                mOnSocialNetworkCallbacks.onFacebookFailed();
            }
        }
    }

}
