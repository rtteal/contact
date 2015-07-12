package com.contact.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.contact.R;
import com.contact.models.ContactInfo;
import com.contact.tasks.PhotoReader;
import com.contact.util.ImageUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melnykov.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateProfileFragment extends Fragment {
    private static final String TAG = "CreateProfileFragment";

    private static final int SELECT_PICTURE_REQUEST_CODE = 3237;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;

    public final static int PICK_PHOTO_CODE = 1046;

    private Uri photoUri;

    public static final String OBJECT_ID = "objectId";

    enum ProfileMode{
        EDIT,
        VIEW
    }

    private ProfileMode profileMode;

    private ParseUser user;
    private ContactInfo contactInfo;
    private SupportMapFragment mapFragment;

    @Bind(R.id.ivProfileImage) ImageView ivProfileImage;
    @Bind(R.id.tvFirstName) TextView tvFirstName;
    @Bind(R.id.etFirstName) EditText etFirstName;
    @Bind(R.id.tvMiddleName) TextView tvMiddleName;
    @Bind(R.id.etMiddleName) EditText etMiddleName;
    @Bind(R.id.tvLastName) TextView tvLastName;
    @Bind(R.id.etLastName) EditText etLastName;
    @Bind(R.id.tvCompany) TextView tvCompany;
    @Bind(R.id.etCompany) EditText etCompany;
    @Bind(R.id.tvPhoneType) TextView tvPhoneType;
    @Bind(R.id.spPhoneType) Spinner spPhoneType;
    @Bind(R.id.tvPhone) TextView tvPhone;
    @Bind(R.id.etPhone) EditText etPhone;
    @Bind(R.id.tvEmailType) TextView tvEmailType;
    @Bind(R.id.spEmailType) Spinner spEmailType;
    @Bind(R.id.tvEmail) TextView tvEmail;
    @Bind(R.id.etEmail) EditText etEmail;
    @Bind(R.id.tvAddressType) TextView tvAddressType;
    @Bind(R.id.spAddressType) Spinner spAddressType;
    @Bind(R.id.tvAddress) TextView tvAddress;
    @Bind(R.id.etAddress) EditText etAddress;
    @Bind(R.id.tvSocialProfileType) TextView tvSocialProfileType;
    @Bind(R.id.spSocialProfileType) Spinner spSocialProfileType;
    @Bind(R.id.tvSocialProfile) TextView tvSocialProfile;
    @Bind(R.id.etSocialProfile) EditText etSocialProfile;
    @Bind(R.id.tvMapTitle) TextView tvMapTitle;
    @Bind(R.id.svProfile) ScrollView mainScrollView;
    @Bind(R.id.fabEditDone) FloatingActionButton fabEditDone;

    @Bind({R.id.tvAddress, R.id.tvAddressType, R.id.tvCompany, R.id.tvEmail, R.id.tvEmailType,
            R.id.tvFirstName, R.id.tvLastName, R.id.tvMiddleName, R.id.tvPhone, R.id.tvPhoneType,
            R.id.tvSocialProfile, R.id.tvSocialProfileType})
    List<View> readOnlyViews;

    @Bind({R.id.etAddress, R.id.etCompany, R.id.etEmail, R.id.etFirstName, R.id.etLastName,
            R.id.etMiddleName, R.id.etPhone, R.id.etSocialProfile, R.id.spAddressType, R.id.spEmailType,
            R.id.spPhoneType, R.id.spSocialProfileType})
    List<View> editViews;

    static final ButterKnife.Action<View> DISABLE = new ButterKnife.Action<View>() {
        @Override public void apply(View view, int index) {
            view.setVisibility(View.INVISIBLE);
        }
    };

    static final ButterKnife.Action<View> ENABLE = new ButterKnife.Action<View>() {
        @Override public void apply(View view, int index) {
            view.setVisibility(View.VISIBLE);
        }
    };

    /**
     * id for the parse User object for the user whose profile is being shown
     */
    private String objectId;


    public static CreateProfileFragment newInstance(String objectId) {
        CreateProfileFragment fragment = new CreateProfileFragment();
        Bundle args = new Bundle();
        args.putString(OBJECT_ID, objectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        objectId = getArguments().getString(OBJECT_ID);
        Log.d(TAG, "onCreate received objectId=" + (objectId == null ? "NULL" : objectId));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        long start = System.currentTimeMillis();
        View v = inflater.inflate(R.layout.fragment_create_profile, container, false);
        ButterKnife.bind(this, v);
        profileMode = ProfileMode.VIEW;
        setUpViews(v);

        long end = System.currentTimeMillis();
        long elapsed = end - start;
        Log.d(TAG, "onCreateView took: " + elapsed);
        return v;
    }

    private void setUpViews(View v){
        // see http://stackoverflow.com/a/17315956/2544629 for the reason for this
        // it allows the map to be interacted with in a scrollview
        final View transparentView = v.findViewById(R.id.vInvisibleView);

        transparentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        showTextViews();

        fabEditDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileMode == ProfileMode.VIEW) {
                    showEditTexts();
                } else {
                    save();
                    showTextViews();
                }
            }
        });

        long start = System.currentTimeMillis();
        fetchUser();
        long elapsed = System.currentTimeMillis() - start;
        Log.d(TAG, "fetchUser took: " + elapsed);
    }

    @OnClick(R.id.tvPhone)
    public void makeCall() {
        String phoneNumber = tvPhone.getText().toString().trim();
        if (phoneNumber != null && phoneNumber.length() > 0){
            phoneNumber = "tel:" + phoneNumber;
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(phoneNumber));
            startActivity(intent);
        }
    }

    @OnClick(R.id.tvEmail)
    public void sendEmail() {
        String email = etEmail.getText().toString().trim();
        if (email != null && email.length() > 0){
            String[] emails = new String[]{email};
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_EMAIL, emails);
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }

    @OnClick(R.id.ivProfileImage)
    public void editProfileImage() {
        if(profileMode == ProfileMode.EDIT){
            openImageIntent();
        }
    }

    private void showEditTexts(){
        fabEditDone.setImageResource(R.drawable.ic_save_profile);
        profileMode = ProfileMode.EDIT;

        ButterKnife.apply(editViews, ENABLE);
        ButterKnife.apply(readOnlyViews, DISABLE);
    }

    private void showTextViews(){
        fabEditDone.setImageResource(android.R.drawable.ic_menu_edit);
        profileMode = ProfileMode.VIEW;

        ButterKnife.apply(editViews, DISABLE);
        ButterKnife.apply(readOnlyViews, ENABLE);
    }

    private void save(){
        contactInfo.setFirstName(etFirstName.getText().toString());
        contactInfo.setMiddleName(etMiddleName.getText().toString());
        contactInfo.setLastName(etLastName.getText().toString());
        contactInfo.setCompany(etCompany.getText().toString());

        if (etPhone.getText().toString() != null
                && etPhone.getText().toString().trim().length() > 0){
            contactInfo.setPhoneType(spPhoneType.getSelectedItem().toString());
            contactInfo.setPhone(etPhone.getText().toString());
        }

        if (etEmail.getText().toString() != null
                && etEmail.getText().toString().trim().length() > 0){
            contactInfo.setEmailType(spEmailType.getSelectedItem().toString());
            contactInfo.setEmail(etEmail.getText().toString());
        }

        if (etAddress.getText().toString() != null
                && etAddress.getText().toString().trim().length() > 0){
            contactInfo.setAddressType(spAddressType.getSelectedItem().toString());
            contactInfo.setAddress(etAddress.getText().toString());
        }

        if (etSocialProfile.getText().toString() != null
                && etSocialProfile.getText().toString().trim().length() > 0){
            contactInfo.setSocialProfileType(spSocialProfileType.getSelectedItem().toString());
            contactInfo.setSocialProfile(etSocialProfile.getText().toString());
        }

        //contactInfo.put("userId", ParseUser.getCurrentUser().getObjectId());
        contactInfo.setParseUser(ParseUser.getCurrentUser());

        if (!contactInfo.isDirty()) {
            // TODO this doesn't actually work, but it would be cool if it did
            Log.d(TAG, "None of the user's data has changed, so nothing is being saved to Parse.");
            getActivity().finish();
            return;
        }

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etPhone.getWindowToken(), 0);

        mainScrollView.fullScroll(ScrollView.FOCUS_UP);

        contactInfo.saveInBackground(new SaveCallback(){
            @Override
            public void done(ParseException e){
                if(e == null){
                    Toast.makeText(getActivity(), "Save successful", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Save successful!");
                    setCurrentValues();
                }else{
                    Log.e(TAG, "Save failed! " + e.getMessage());
                    Toast.makeText(getActivity(), "Save failed", Toast.LENGTH_SHORT).show();
                    setCurrentValues();
                }
            }
        });
    }

    private void fetchUser(){
        if (objectId == null){
            user = ParseUser.getCurrentUser();
            if (user.isNew()){
                user.fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        getCurrentUser();
                        showEditTexts();
                    }
                });
            } else {
                getCurrentUser();
            }
        } else {
            ContactInfo.getContactInfo(objectId, new ContactInfo.OnContactReturnedListener() {
                @Override
                public void receiveContact(ContactInfo contactInfo) {
                    user = (ParseUser) contactInfo.get("User");
                    CreateProfileFragment.this.contactInfo = contactInfo;
                    setCurrentValues();
                    setUpMapIfNeeded();
                }
            });
        }
    }

    private void getCurrentUser(){
        contactInfo = (ContactInfo) user.get(ContactInfo.CONTACT_INFO_TABLE_NAME);
        if (contactInfo == null){ // new user
            Toast.makeText(getActivity(), "Error creating Contact Info", Toast.LENGTH_SHORT).show();
            return;
        }
        contactInfo.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    setCurrentValues();
                    setUpMapIfNeeded();
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void setCurrentValues(){
        String imageFileUrl = contactInfo.getProfileImage();
        if (imageFileUrl != null){
            Picasso.with(getActivity()).load(imageFileUrl).into(ivProfileImage);
        }

        tvFirstName.setText(contactInfo.getFirstName());
        etFirstName.setText(contactInfo.getFirstName());

        tvMiddleName.setText(contactInfo.getMiddleName());
        etMiddleName.setText(contactInfo.getMiddleName());

        tvLastName.setText(contactInfo.getLastName());
        etLastName.setText(contactInfo.getLastName());

        tvCompany.setText(contactInfo.getCompany());
        etCompany.setText(contactInfo.getCompany());

        if (contactInfo.getPhone() != null
                && contactInfo.getPhone().trim().length() > 0){
            tvPhoneType.setText(contactInfo.getPhoneType());
            spPhoneType.setSelection(((ArrayAdapter)spPhoneType.getAdapter()).getPosition(contactInfo.getPhoneType()));

            tvPhone.setText(contactInfo.getPhone());
            etPhone.setText(contactInfo.getPhone());
        }

        if (contactInfo.getEmail() != null
                && contactInfo.getEmail().trim().length() > 0){
            tvEmailType.setText(contactInfo.getEmailType());
            spEmailType.setSelection(((ArrayAdapter)spEmailType.getAdapter()).getPosition(contactInfo.getEmailType()));

            tvEmail.setText(contactInfo.getEmail());
            etEmail.setText(contactInfo.getEmail());
        }

        if (contactInfo.getAddress() != null
                && contactInfo.getAddress().trim().length() > 0){
            tvAddressType.setText(contactInfo.getAddressType());
            spAddressType.setSelection(((ArrayAdapter)spAddressType.getAdapter()).getPosition(contactInfo.getAddressType()));

            tvAddress.setText(contactInfo.getAddress());
            etAddress.setText(contactInfo.getAddress());
        }

        if (contactInfo.getSocialProfile() != null
                && contactInfo.getSocialProfile().trim().length() > 0){
            tvSocialProfileType.setText(contactInfo.getSocialProfileType());
            spSocialProfileType.setSelection(((ArrayAdapter)spSocialProfileType.getAdapter())
                    .getPosition(contactInfo.getSocialProfileType()));

            tvSocialProfile.setText(contactInfo.getSocialProfile());
            etSocialProfile.setText(contactInfo.getSocialProfile());
        }
    }

    private void openImageIntent() {
        photoUri = ImageUtil.createPhotoUri("img_" + System.currentTimeMillis() + ".jpg");
        Intent chooserIntent = ImageUtil.createImageChooserIntent(getActivity(), photoUri);
        startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // camera
        if (requestCode == SELECT_PICTURE_REQUEST_CODE && data.getData() == null) {
            if (resultCode == getActivity().RESULT_OK) {
                setProfileImage(photoUri);
            } else {
                Log.d(TAG, "Picture wasn't taken!");
            }
        }

        // photo
        if (requestCode == SELECT_PICTURE_REQUEST_CODE && data.getData() != null) {
            if (resultCode == getActivity().RESULT_OK) {
                setProfileImage(data.getData());
            }
        }
    }

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mapFragment == null){
            mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        }

        // Check if we were successful in obtaining the map.
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        }
    }

    // The Map is verified. It is now safe to manipulate the map.
    protected void loadMap(final GoogleMap googleMap) {
        if (googleMap != null) {
            if (user != null) {
                user.fetchIfNeededInBackground(new GetCallback<ParseObject>(){
                    @Override
                    public void done(ParseObject parseObject, ParseException e){
                        if (e == null) {
                            ParseGeoPoint geoPoint = user.getParseGeoPoint("lastLocation");
                            if(geoPoint != null){
                                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                                googleMap.animateCamera(cameraUpdate);
                                shouldShowMap(true);

                                // Define color of marker icon
                                BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

                                // Creates and adds marker to the map
                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .icon(defaultMarker));

                                Log.d(TAG, "found lat/long and updated map with location:" + latLng.toString());
                                return;
                            } else {
                                Log.e(TAG, "could not find lat/long to update map.");
                                shouldShowMap(false);
                            }
                        } else {
                            Log.e(TAG, "error fetching user data.", e);
                            shouldShowMap(false);
                        }
                    }
                });

                return;

            } else {
                Log.e(TAG, "could not find user to update map.");
            }
        } else {
            Log.e(TAG, "could not find map to update map.");
        }
        shouldShowMap(false);
    }

    private void shouldShowMap(boolean shouldShow){
        if(shouldShow){
            this.tvMapTitle.setVisibility(View.VISIBLE);
            this.mapFragment.getView().setVisibility(View.VISIBLE);
        }else{
            this.tvMapTitle.setVisibility(View.GONE);
            this.mapFragment.getView().setVisibility(View.GONE);
        }
    }

    private void setProfileImage(Uri photoUri){
        if (photoUri != null){
            PhotoReader reader = new PhotoReader(getActivity(), new PhotoReader.PhotoReadCompletionListener() {
                @Override
                public void onPhotoReadCompletion(byte[] photo) {
                    if (photo != null && photo.length > 0){
                        contactInfo.setProfileImage(photo);
                    } else {
                        Log.e(TAG, "Photo is null or zero size. Cannot save to Parse.");
                    }
                }

                @Override
                public void onPhotoReadCompletion(Bitmap photo) {
                    if (photo != null && photo.getByteCount() > 0){
                        ivProfileImage.setImageBitmap(photo);
                    } else {
                        Log.e(TAG, "Photo is null or zero size. Cannot set profile image.");
                    }
                }
            });
            reader.execute(photoUri);
        }
    }
}
