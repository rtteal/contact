package com.contact.fragments.login;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.contact.GoogleApplication;
import com.contact.R;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.contact.fragments.login.CreateAccountFragment.AccountType.CREATE;
import static com.contact.fragments.login.CreateAccountFragment.AccountType.LOGIN;

public class CreateAccountFragment extends Fragment {
    private GoogleApplication.ParseAccountCreationListener listener;
    private static final String TAG = "CreateAccountFragment";
//    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
//    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
//    private static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1002;
    // we may want to grab some of the data from the profile if the user is new
    private static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String FULL_CONTACTS_SCOPE = "https://www.google.com/m8/feeds";
    public final static String SCOPES = "oauth2:" + PROFILE_SCOPE + " " + FULL_CONTACTS_SCOPE;
    private static final String CONTACT_PREFERENCES = "ContactPreferences";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private String userName;
    private String password;
    private String email;

    @Bind(R.id.btLogin) Button btLogin;
    @Bind(R.id.tvCreateAccount) TextView tvCreateAccount;
    @Bind(R.id.etUserName) EditText etUserName;
    @Bind(R.id.etPassword) EditText etPassword;
    @Bind(R.id.etEmail) EditText etEmail;

    enum AccountType{
        LOGIN,
        CREATE
    }

    AccountType accountType;

    public static CreateAccountFragment newInstance() {
        CreateAccountFragment fragment = new CreateAccountFragment();
        fragment.accountType = LOGIN;//default
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Contact);

        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View v = localInflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, v);

        TextView tvAppName = (TextView) v.findViewById(R.id.tvAppName);
        tvAppName.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Roboto-MediumItalic.ttf"));
        etEmail.setVisibility(View.INVISIBLE);

        tvCreateAccount.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoCondensed-Italic.ttf"));

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accountType == LOGIN){
                    accountType = CREATE;
                    revealEmail();
                }else if(accountType == CREATE){
                    accountType = LOGIN;
                    hideEmail();
                }
            }
        });
        etPassword.setImeActionLabel("done", EditorInfo.IME_ACTION_DONE);
        etPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE){
                    btLogin.callOnClick();
                }
                return false;
            }
        });
        revealLoginButton();
        return v;
    }

    private void revealEmail(){
        // get the center for the clipping circle
        int cx = etEmail.getMeasuredWidth() / 2;
        int cy = etEmail.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(etEmail.getWidth(), etEmail.getHeight()) / 2;
        final Animator reveal = ViewAnimationUtils.createCircularReveal(etEmail, cx, cy, 0, finalRadius);
        etEmail.setVisibility(View.VISIBLE);
        reveal.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation){}
            @Override
            public void onAnimationEnd(Animator animation){
                etEmail.requestFocus();
            }
            @Override
            public void onAnimationCancel(Animator animation){}
            @Override
            public void onAnimationRepeat(Animator animation){}
        });
        revealCreateAccountButton();
        etEmail.requestFocus();
        reveal.setDuration(300);
        reveal.start();
        tvCreateAccount.setText(R.string.login_existing_account);
    }

    private void hideEmail(){
        // get the center for the clipping circle
        int cx = etEmail.getMeasuredWidth() / 2;
        int cy = etEmail.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(etEmail.getWidth(), etEmail.getHeight()) / 2;
        final Animator reveal = ViewAnimationUtils.createCircularReveal(etEmail, cx, cy, finalRadius, 0);
        revealLoginButton();
        reveal.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation){}
            @Override
            public void onAnimationEnd(Animator animation){
                etEmail.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationCancel(Animator animation){}
            @Override
            public void onAnimationRepeat(Animator animation){}
        });
        reveal.start();
        //tvCreateAccount.setVisibility(View.INVISIBLE);
        tvCreateAccount.setText(R.string.create_account);
    }

    private void revealCreateAccountButton(){
        btLogin.setText(R.string.create_account);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCredentials()) {
                    signUpUserWithParse();
                }
            }
        });
    }

    private void revealLoginButton(){
        btLogin.setText(R.string.login_label);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCredentials()) {
                    signIntoParse();
                }
            }
        });
    }

    //TODO implement better validation logic
    private boolean validateCredentials() {
        userName = etUserName.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        if (userName == null || userName.length() < 2) {
            etUserName.setError("Invalid username");
            return false;
        }
        if (password == null || password.length() < 2) {
            etPassword.requestFocus();
            etPassword.setError("Invalid password");
            return false;
        }
        return true;
    }

    /**
     * This method will prompt the user to select an account
     * and then begin the request for an auth token.
     */
   /* public void getUserAccount() {
        Log.d(TAG, "getting auth token...");
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }*/

    public void signUpUserWithParse(){
        Log.d(TAG, "signing up user with parse...");
        GoogleApplication.signUpWithParse(userName, password, email,
                new GoogleApplication.ParseAccountCreationListener() {
                    @Override
                    public void onAccountCreationResponse(boolean success) {
                        if (success) {
                            CreateAccountFragment.this.saveUsernameAndPasswordLocally(userName, password);
                            Log.d(TAG, "done signing up user with parse...");
                        }

                        listener.onAccountCreationResponse(success);
                    }
                });
    }

    public void signIntoParse(){
        GoogleApplication.signIntoParse(userName, password,
                new GoogleApplication.ParseLoginListener() {
                    @Override
                    public void onLoginResponse(boolean success) {
                        if (success) {
                            CreateAccountFragment.this.saveUsernameAndPasswordLocally(userName, password);
                            Log.d(TAG, "done signing up user with parse...");
                        }

                        listener.onAccountCreationResponse(success);
                    }
                });
    }

    private void saveUsernameAndPasswordLocally(String userName, String password){
        // production version should use a more secure storage space for password
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(CONTACT_PREFERENCES, getActivity().MODE_PRIVATE).edit();
        editor.putString(USERNAME, userName);
        editor.putString(PASSWORD, password);
        editor.commit();

        // Register userName for push notifications on this device
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("username", ParseUser.getCurrentUser().getUsername());
        installation.saveInBackground();
    }

  /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "resultCode: " + resultCode + ", requestCode: " + requestCode);
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == getActivity().RESULT_OK) {
                email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                //if (isDeviceOnline()) {
                new GetAuthTokenTask(getActivity(), email, SCOPES).execute();
                /*} else {
                    Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
                }/
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(getActivity(), R.string.pick_account, Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == getActivity().RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            Log.e(TAG, "Received recoverable error code from Google auth.");
            //getUserAccount();
        }
    } */

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (GoogleApplication.ParseAccountCreationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ParseAccountCreationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
