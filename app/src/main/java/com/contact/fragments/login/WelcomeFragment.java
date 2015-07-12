package com.contact.fragments.login;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.contact.GoogleApplication;
import com.contact.R;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WelcomeFragment extends Fragment {
    private static final String TAG = "WelcomeFragment";
    private static final String CONTACT_PREFERENCES = "ContactPreferences";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private InitialAppStartupListener listener;

    @Bind(R.id.pbLoading) ProgressBar pbLoading;

    public interface InitialAppStartupListener{
        /**
         * A method to communicate to the LoginActivity whether
         * this user exists (true), or doesn't (false).  If the
         * user exists, the LoginActivity takes the user to the
         * LandingActivity.  Otherwise it takes the user through
         * the CreateAccount process.
         * @param success true if the user exists, false if not.
         */
        void onWelcomeFragmentFinishedLoading(boolean success);
    }

    public static WelcomeFragment newInstance(){
        return new WelcomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome, container, false);
        ButterKnife.bind(this, v);
        pbLoading.setVisibility(ProgressBar.VISIBLE);
        checkForExistingAccount();
        return v;
    }

    private void checkForExistingAccount() {
        SharedPreferences prefs = getActivity().getSharedPreferences(CONTACT_PREFERENCES, getActivity().MODE_PRIVATE);
        String userName = prefs.getString(USERNAME, null);
        String password = prefs.getString(PASSWORD, null);

        // if these don't exist, the the user must be new to our app
        if (userName == null || password == null) {
            pbLoading.setVisibility(ProgressBar.INVISIBLE);
            listener.onWelcomeFragmentFinishedLoading(false); // tell Activity to direct user to create account
            return;
        }

        ParseUser user = ParseUser.getCurrentUser();
        if (user.isAuthenticated()){
            pbLoading.setVisibility(ProgressBar.INVISIBLE);
            listener.onWelcomeFragmentFinishedLoading(true);
        } else {
            Log.d(TAG, "user is not authenticated");
            Log.d(TAG, "Found user/pw from shared prefs. usr: " + userName + ", pw: " + password);
            Toast.makeText(getActivity(), "Signing you in: " + userName, Toast.LENGTH_SHORT).show();
            GoogleApplication.signIntoParse(userName, password, new GoogleApplication.ParseLoginListener() {
                @Override
                public void onLoginResponse(boolean success) {
                    pbLoading.setVisibility(ProgressBar.INVISIBLE);
                    listener.onWelcomeFragmentFinishedLoading(success);
                }
            });
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof InitialAppStartupListener)){
            throw new ClassCastException("Activity must implement InitialAppStartupListener");
        }
        listener = (InitialAppStartupListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


}
