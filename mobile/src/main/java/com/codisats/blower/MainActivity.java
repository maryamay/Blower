package com.codisats.blower;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener {

    private static final String TAG = "AppCompactActivity";

    private static final long UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);
    private static final long FASTEST_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);


    // Id to identify Location permission request.
    private static final int REQUEST_GPS_PERMISSION = 1;

    // Id to identify SMS permission request.
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    private Calendar mCalendar;

    private TextView locationName;
    private ImageView mGpsPermissionImageView;
    private Button mReport;
    private TextView mGpsIssueTextView;
    private ImageView mCustomMessage;
    private Button mSent;
    private View root;

    private String mGpsPermissionNeededMessage;
    private String mAcquiringGpsMessage;


    private boolean mGpsPermissionApproved;

    private boolean mSMSPermissionApproved;

    private boolean mWaitingForGpsSignal;

    private GoogleApiClient mGoogleApiClient;

    private Location mLocation;
    private String mLocDetails;

    private Handler mHandler = new Handler();
    private Message mMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        mSMSPermissionApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        setContentView(R.layout.activity_main);

        mCalendar = Calendar.getInstance();

        // Enables app to handle 23+ (M+) style permissions.
        mGpsPermissionApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        mGpsPermissionNeededMessage = getString(R.string.permission_rationale);
        mAcquiringGpsMessage = getString(R.string.acquiring_gps);

        /*
         * If this hardware doesn't support GPS, we warn the user. Note that when such device is
         * connected to a phone with GPS capabilities, the framework automatically routes the
         * location requests from the phone. However, if the phone becomes disconnected and the
         * wearable doesn't support GPS, no location is recorded until the phone is reconnected.
         */
        if (!hasGps()) {
            Log.w(TAG, "This hardware doesn't have GPS, so we warn user.");
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.gps_not_available))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialog.cancel();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        }


        setupViews();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if ((mGoogleApiClient != null) && (mGoogleApiClient.isConnected()) &&
                (mGoogleApiClient.isConnecting())) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    private void setupViews() {
        mReport = findViewById(R.id.report);
        mSent = findViewById(R.id.sent_messages);
        mCustomMessage = findViewById(R.id.custom_message);

        mGpsPermissionImageView = findViewById(R.id.gps_permission);
        mGpsIssueTextView = findViewById(R.id.gps_issue_text);
        locationName = findViewById(R.id.location_name);
        root = mGpsIssueTextView.getRootView();
        updateActivityViewsBasedOnLocationPermissions();
    }


    public void onGpsPermissionClick(View view) {

        if (!mGpsPermissionApproved) {

            Log.i(TAG, "Location permission has NOT been granted. Requesting permission.");

            // On 23+ (M+) devices, GPS permission not granted. Request permission.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_GPS_PERMISSION);
        }
    }

    /**
     * Adjusts the visibility of views based on location permissions.
     */
    private void updateActivityViewsBasedOnLocationPermissions() {

        /*
         * If the user has approved location but we don't have a signal yet, we let the user know
         * we are waiting on the GPS signal (this sometimes takes a little while). Otherwise, the
         * user might think something is wrong.
         */
        if (mGpsPermissionApproved && mWaitingForGpsSignal) {

            // We are getting a GPS signal w/ user permission.
            mGpsIssueTextView.setText(mAcquiringGpsMessage);
            mGpsIssueTextView.setVisibility(View.VISIBLE);
            mGpsPermissionImageView.setImageResource(R.drawable.ic_gps_saving_grey600_96dp);


        } else if (mGpsPermissionApproved) {

            mGpsIssueTextView.setVisibility(View.GONE);

            mGpsPermissionImageView.setImageResource(R.drawable.ic_gps_saving_grey600_96dp);

        } else {

            // User needs to enable location for the app to work.
            mGpsIssueTextView.setVisibility(View.VISIBLE);
            mGpsIssueTextView.setText(mGpsPermissionNeededMessage);
            mGpsPermissionImageView.setImageResource(R.drawable.ic_gps_not_saving_grey600_96dp);

            //mSpeedTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.d(TAG, "onConnected()");
        requestLocation();


    }

    private void requestLocation() {
        Log.d(TAG, "requestLocation()");

        /*
         * mGpsPermissionApproved covers 23+ (M+) style permissions. If that is already approved or
         * the device is pre-23, the app uses mSaveGpsLocation to save the user's location
         * preference.
         */
        if (mGpsPermissionApproved) {

            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL_MS)
                    .setFastestInterval(FASTEST_INTERVAL_MS);

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.getStatus().isSuccess()) {
                                if (Log.isLoggable(TAG, Log.DEBUG)) {
                                    Log.d(TAG, "Successfully requested location updates");
                                }
                            } else {
                                Log.e(TAG,
                                        "Failed in requesting location updates, "
                                                + "status code: "
                                                + status.getStatusCode() + ", message: " + status
                                                .getStatusMessage());
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged() : " + location);

        mLocation = location;

        if (mWaitingForGpsSignal) {
            mWaitingForGpsSignal = false;
            updateActivityViewsBasedOnLocationPermissions();
        }


        addLocationEntry(location.getLatitude(), location.getLongitude());
        locationName.setText(location.toString());
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String cityName = addresses.get(0).getAddressLine(0);
        String stateName = addresses.get(0).getAddressLine(1);
        String countryName = addresses.get(0).getAddressLine(2);
        String locDetails = cityName + " " + stateName + " " + countryName;
        mLocDetails = locDetails;
        locationName.setText(cityName + " " + stateName + " " + countryName);

    }


    /*
     * Adds a data item to the data Layer storage.
     */
    private void addLocationEntry(double latitude, double longitude) {
        if (!mGpsPermissionApproved || !mGoogleApiClient.isConnected()) {
            return;
        }
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        LocationEntry entry = new LocationEntry(mCalendar, latitude, longitude);
        String path = Constants.PATH + "/" + mCalendar.getTimeInMillis();
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        putDataMapRequest.getDataMap().putDouble(Constants.KEY_LATITUDE, entry.latitude);
        putDataMapRequest.getDataMap().putDouble(Constants.KEY_LONGITUDE, entry.longitude);
        putDataMapRequest.getDataMap()
                .putLong(Constants.KEY_TIME, entry.calendar.getTimeInMillis());
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();

    }

    /**
     * Handles user choices for both speed limit and location permissions (GPS tracking).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult(): " + permissions);


        if (requestCode == REQUEST_GPS_PERMISSION) {
            Log.i(TAG, "Received response for GPS permission request.");

            if ((grantResults.length == 1)
                    && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.i(TAG, "GPS permission granted.");
                mGpsPermissionApproved = true;

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    requestLocation();
                }

            } else {
                Log.i(TAG, "GPS permission NOT granted.");
                mGpsPermissionApproved = false;
            }

            updateActivityViewsBasedOnLocationPermissions();

        }
    }

    /**
     * Returns {@code true} if this device has the GPS capabilities.
     */
    private boolean hasGps() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }


    public void onSendCustom(View view) {
        displaySpeechScreen();
    }

    private void displaySpeechScreen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What is the title for the message?");

        startActivityForResult(intent, 1001);
    }

    public void onSentClick(View view) {
        Intent intent = new Intent(MainActivity.this, MessagesList.class);
        startActivity(intent);
    }

    public void onReport(View view) {
        mReport.setEnabled(false);
        mReport.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                locationName.setText("Sending SMS in: " + millisUntilFinished / 1000);

            }

            public void onFinish() {
                if (mLocation != null) {
                    try {
                        String currLatt = Double.toString(mLocation.getLatitude());
                        String currLong = Double.toString(mLocation.getLongitude());
                        String message = "This victim is being kidnapped and is currently at ";

                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage("08099449423", null, message, null, null);
                        Toast toast = Toast.makeText(getApplicationContext(), "Sent!",
                                Toast.LENGTH_LONG);
                        toast.show();

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "SMS can't be sent!",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
                this.start();
            }
        }.start();

    }


    public void onExit(View view) {
        this.finishAffinity();
    }
}