package com.contact;

import android.content.Context;
import android.util.Log;

import com.contact.models.ContactInfo;
import com.contact.models.Request;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class GoogleApplication extends com.activeandroid.app.Application {
    private static Context context;
    private static final String TAG = "GoogleApplication";

    public interface ParseLoginListener {
        void onLoginResponse(boolean success);
    }

    public interface ParseAccountCreationListener {
        void onAccountCreationResponse(boolean success);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GoogleApplication.context = this;

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // register classes extending ParseObject.
        ParseObject.registerSubclass(Request.class);
        ParseObject.registerSubclass(ContactInfo.class);

        // Add your initialization code here
        Parse.initialize(this, getResources().getString(R.string.parse_app_id), getResources().getString(R.string.parse_client_key));

        ParseUser.enableAutomaticUser();

        //ParseACL defaultACL = new ParseACL();

        // Optionally enable public read access.
        //defaultACL.setPublicReadAccess(true);
        //ParseACL.setDefaultACL(defaultACL, true);

    }

    private static void subscribeToPush(){
        // register device for push notifications by subscribing to a particular channel
        ParsePush.subscribeInBackground("ContactUpdates", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }

    public static void signIntoParse(String userName, String password, final ParseLoginListener listener){
        Log.d(TAG, "trying to sign into parse with username: " + userName + " and password: " + password);
        ParseUser.logInInBackground(userName, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    Log.d(TAG, "Login successful");
                    listener.onLoginResponse(true);
                    subscribeToPush();
                } else {
                    Log.d(TAG, "Login failed: " + e.getMessage());
                    listener.onLoginResponse(false);
                }
            }
        });
    }

    public static void signUpWithParse(String userName, String password,
                                String email, final ParseAccountCreationListener listener){
        Log.d(TAG, "trying to sign up with parse, username: " + userName + " and password: " + password + " and email: " + email);
        ParseUser user = new ParseUser();
        user.setUsername(userName);
        user.setPassword(password);
        user.setEmail(email);
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "SignUp successful");
                    listener.onAccountCreationResponse(true);
                    subscribeToPush();
                } else {
                    Log.d(TAG, "SignUp failed: " + e.getMessage());
                    listener.onAccountCreationResponse(false);
                }
            }
        });
    }
}
