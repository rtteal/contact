package com.contact.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.contact.R;
import com.contact.fragments.CreateProfileFragment;

public class ProfileActivity extends ActionBarActivity {
    private static final String TAG = "ProfileActivity";
    public static final String DETAILS_BUNDLE = "details bundle";
    private String objectId;
    private Bundle detailsBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Contact);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        objectId = getIntent().getExtras().getString(CreateProfileFragment.OBJECT_ID);
        detailsBundle = getIntent().getExtras().getBundle(DETAILS_BUNDLE);
        if (objectId == null){
            startCreateProfileFragment();
        } else {
            // start detail frag...
        }

    }

    private void startDetailFragment(){

    }

    private void startCreateProfileFragment(){
        CreateProfileFragment createProfileFragment = CreateProfileFragment.newInstance(objectId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flCreateProfile, createProfileFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(TAG, "calling supportFinishAfterTransition");
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
