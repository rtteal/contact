package com.contact.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateProfileFragment extends Fragment {
    private static final String TAG = "CreateProfileFragment";

    private static final int SELECT_PICTURE_REQUEST_CODE = 3237;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;

    public final static int PICK_PHOTO_CODE = 1046;

    public String photoFileName;

    public static final String OBJECT_ID = "objectId";
    //private OnFragmentInteractionListener mListener;

    private FloatingActionButton fabEditDone;

    enum ProfileMode{
        EDIT,
        VIEW
    }

    private ProfileMode profileMode;

    private ImageView ivProfileImage;

    private TextView tvFirstName;
    private EditText etFirstName;

    private TextView tvMiddleName;
    private EditText etMiddleName;

    private TextView tvLastName;
    private EditText etLastName;

    private TextView tvCompany;
    private EditText etCompany;

    private TextView tvPhoneType;
    private Spinner spPhoneType;

    private TextView tvPhone;
    private EditText etPhone;

    private TextView tvEmailType;
    private Spinner spEmailType;

    private TextView tvEmail;
    private EditText etEmail;

    private TextView tvAddressType;
    private Spinner spAddressType;

    private TextView tvAddress;
    private EditText etAddress;

    private TextView tvSocialProfileType;
    private Spinner spSocialProfileType;

    private TextView tvSocialProfile;
    private EditText etSocialProfile;

    private SupportMapFragment mapFragment;
    private TextView tvMapTitle;

    private ParseUser user;
    private ContactInfo contactInfo;

    private Uri outputFileUri;

    private ScrollView mainScrollView;

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
        profileMode = ProfileMode.VIEW;
        setUpViews(v);

        // see http://stackoverflow.com/a/17315956/2544629 for the reason for this
        // it allows the map to be interacted with in a scrollview
        mainScrollView = (ScrollView) v.findViewById(R.id.svProfile);
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

        long end = System.currentTimeMillis();
        long elapsed = end - start;
        Log.d(TAG, "onCreateView took: " + elapsed);
        return v;
    }

    private void setUpViews(View v){
        ivProfileImage = (ImageView) v.findViewById(R.id.ivProfileImage);

        fabEditDone = (FloatingActionButton) v.findViewById(R.id.fabEditDone);

        tvFirstName = (TextView) v.findViewById(R.id.tvFirstName);
        etFirstName = (EditText) v.findViewById(R.id.etFirstName);

        tvMiddleName = (TextView) v.findViewById(R.id.tvMiddleName);
        etMiddleName = (EditText) v.findViewById(R.id.etMiddleName);

        tvLastName = (TextView) v.findViewById(R.id.tvLastName);
        etLastName = (EditText) v.findViewById(R.id.etLastName);

        tvCompany = (TextView) v.findViewById(R.id.tvCompany);
        etCompany = (EditText) v.findViewById(R.id.etCompany);

        tvPhoneType = (TextView) v.findViewById(R.id.tvPhoneType);
        spPhoneType = (Spinner) v.findViewById(R.id.spPhoneType);

        tvPhone = (TextView) v.findViewById(R.id.tvPhone);
        etPhone = (EditText) v.findViewById(R.id.etPhone);

        tvEmailType = (TextView) v.findViewById(R.id.tvEmailType);
        spEmailType = (Spinner) v.findViewById(R.id.spEmailType);

        tvEmail = (TextView) v.findViewById(R.id.tvEmail);
        etEmail = (EditText) v.findViewById(R.id.etEmail);

        tvAddressType = (TextView) v.findViewById(R.id.tvAddressType);
        spAddressType = (Spinner) v.findViewById(R.id.spAddressType);

        tvAddress = (TextView) v.findViewById(R.id.tvAddress);
        etAddress = (EditText) v.findViewById(R.id.etAddress);

        tvSocialProfileType = (TextView) v.findViewById(R.id.tvSocialProfileType);
        spSocialProfileType = (Spinner) v.findViewById(R.id.spSocialProfileType);

        tvSocialProfile = (TextView) v.findViewById(R.id.tvSocialProfile);
        etSocialProfile = (EditText) v.findViewById(R.id.etSocialProfile);

        tvMapTitle = (TextView) v.findViewById(R.id.tvMapTitle);

        showTextViews();

        fabEditDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profileMode == ProfileMode.VIEW) {
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

    private void setUpEmailAndPhoneOnClick(){
        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = tvPhone.getText().toString().trim();
                if (phoneNumber != null && phoneNumber.length() > 0){
                    phoneNumber = "tel:" + phoneNumber;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(phoneNumber));
                    startActivity(intent);
                }
            }
        });

        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (email != null && email.length() > 0){
                    String[] emails = new String[]{email};
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_EMAIL, emails);
                    startActivity(Intent.createChooser(intent, "Send Email"));
                }
            }
        });

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profileMode == ProfileMode.EDIT){
                    openImageIntent();
                } else {
                    Log.d(TAG, "profileMode: " + profileMode);
                }
            }
        });
    }

    private void showEditTexts(){
        fabEditDone.setImageResource(R.drawable.ic_save_profile);
        profileMode = ProfileMode.EDIT;

        etFirstName.setVisibility(View.VISIBLE);
        tvFirstName.setVisibility(View.INVISIBLE);

        tvMiddleName.setVisibility(View.INVISIBLE);
        etMiddleName.setVisibility(View.VISIBLE);

        tvLastName.setVisibility(View.INVISIBLE);
        etLastName.setVisibility(View.VISIBLE);

        tvCompany.setVisibility(View.INVISIBLE);
        etCompany.setVisibility(View.VISIBLE);

        tvPhoneType.setVisibility(View.INVISIBLE);
        spPhoneType.setVisibility(View.VISIBLE);

        tvPhone.setVisibility(View.INVISIBLE);
        etPhone.setVisibility(View.VISIBLE);

        tvEmailType.setVisibility(View.INVISIBLE);
        spEmailType.setVisibility(View.VISIBLE);

        tvEmail.setVisibility(View.INVISIBLE);
        etEmail.setVisibility(View.VISIBLE);

        tvEmail.setVisibility(View.INVISIBLE);
        etEmail.setVisibility(View.VISIBLE);

        tvAddressType.setVisibility(View.INVISIBLE);
        spAddressType.setVisibility(View.VISIBLE);

        tvAddress.setVisibility(View.INVISIBLE);
        etAddress.setVisibility(View.VISIBLE);

        tvSocialProfileType.setVisibility(View.INVISIBLE);
        spSocialProfileType.setVisibility(View.VISIBLE);

        tvSocialProfile.setVisibility(View.INVISIBLE);
        etSocialProfile.setVisibility(View.VISIBLE);
    }

    private void showTextViews(){
        fabEditDone.setImageResource(android.R.drawable.ic_menu_edit);
        profileMode = ProfileMode.VIEW;

        etFirstName.setVisibility(View.INVISIBLE);
        tvFirstName.setVisibility(View.VISIBLE);

        tvMiddleName.setVisibility(View.VISIBLE);
        etMiddleName.setVisibility(View.INVISIBLE);

        tvLastName.setVisibility(View.VISIBLE);
        etLastName.setVisibility(View.INVISIBLE);

        tvCompany.setVisibility(View.VISIBLE);
        etCompany.setVisibility(View.INVISIBLE);

        tvPhoneType.setVisibility(View.VISIBLE);
        spPhoneType.setVisibility(View.INVISIBLE);

        tvPhone.setVisibility(View.VISIBLE);
        etPhone.setVisibility(View.INVISIBLE);

        tvEmailType.setVisibility(View.VISIBLE);
        spEmailType.setVisibility(View.INVISIBLE);

        tvEmail.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.INVISIBLE);

        tvEmail.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.INVISIBLE);

        tvAddressType.setVisibility(View.VISIBLE);
        spAddressType.setVisibility(View.INVISIBLE);

        tvAddress.setVisibility(View.VISIBLE);
        etAddress.setVisibility(View.INVISIBLE);

        tvSocialProfileType.setVisibility(View.VISIBLE);
        spSocialProfileType.setVisibility(View.INVISIBLE);

        tvSocialProfile.setVisibility(View.VISIBLE);
        etSocialProfile.setVisibility(View.INVISIBLE);
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

        if (!contactInfo.isDirty()){
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
                    setUpEmailAndPhoneOnClick();
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
                    setUpEmailAndPhoneOnClick();
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

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
    }

    private void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + TAG + File.separator);
        root.mkdirs();
        photoFileName = "img_" + System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, photoFileName);
        //outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
            cameraIntents.add(intent);
        }

        // photos
        Intent photoPicker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(photoPicker, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // camera
        if (requestCode == SELECT_PICTURE_REQUEST_CODE && data == null) {
            if (resultCode == getActivity().RESULT_OK) {
                setProfileImage(getPhotoFileUri(photoFileName));
            } else { // Result was a failure
                Log.d(TAG, "Picture wasn't taken!");
            }
        }

        // photo
        if (requestCode == SELECT_PICTURE_REQUEST_CODE && data != null) {
            if (resultCode == getActivity().RESULT_OK) {
                setProfileImage(data.getData());
            }
        }
    }

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mapFragment == null){
            mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)); //  getActivity().getSupportFragmentManager().findFragmentById(R.id.map));
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
            if(user != null){
                user.fetchIfNeededInBackground(new GetCallback<ParseObject>(){
                    @Override
                    public void done(ParseObject parseObject, ParseException e){
                        if(e == null){
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
                            }else{
                                Log.e(TAG, "could not find lat/long to update map.");
                                shouldShowMap(false);
                            }
                        }else{
                            Log.e(TAG, "error fetching user data.", e);
                            shouldShowMap(false);
                        }
                    }
                });

                return;

            }else{
                Log.e(TAG, "could not find user to update map.");
            }
        }else{
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
