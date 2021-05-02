package com.example.thumb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {

    // initializing
    // FusedLocationProviderClient
    // object
    private static final int PERMISSION_RQST_SEND = 0;
    FusedLocationProviderClient mFusedLocationClient;
    double latitude;
    double longtitude;
    String country_name;
    String locality;
    String address_name;
    int PERMISSION_ID = 44;
    TextView lokTextView;
    ///
    // Create a storage reference from our app
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        lokTextView = findViewById(R.id.okTextView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Toast.makeText(getApplicationContext(), "on create", Toast.LENGTH_LONG).show();
        sendLocationSMS();
    }
    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }
    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendLocationSMS();
            }
        }
        if(requestCode == PERMISSION_RQST_SEND){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                SmsManager smsManager = SmsManager.getDefault();
                StringBuffer smsBody = new StringBuffer();
                String message = "http://maps.google.com/?q=" + Arrays.toString(new double[]{latitude}) + "," + Arrays.toString(new double[]{longtitude});
                //smsBody.append("I am in trouble.Please pick me from this location  " + "https://www.google.co.id/maps/@" + "Latitude=").append(latitude).append("Longitude=").append(longtitude);
                smsBody.append("I am in trouble.Please pick me from this location  " + message);
                //smsBody.append("http://maps.google.com?q="+latitude+","+longtitude);
               // smsBody.append("http://maps.google.com?q=");
               // smsBody.append(latitude);
               // smsBody.append(",");
               // smsBody.append(longtitude);
                String phoneNumber = "+9720523609597";
                smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), null, null);
            } else {
                Toast.makeText(getApplicationContext(), "SMS failed, you may try again later.", Toast.LENGTH_LONG).show();
                return;
            }

        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            Toast.makeText(getApplicationContext(), "on ressume: check permission", Toast.LENGTH_LONG).show();
            sendLocationSMS();
        }
        else{
            Toast.makeText(getApplicationContext(), "on ressume", Toast.LENGTH_LONG).show();
        }
    }
    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    @SuppressLint("MissingPermission")
    public void sendLocationSMS() {
        if (checkPermissions()) {
            Toast.makeText(getApplicationContext(), "sendLocatione: checkPermission", Toast.LENGTH_LONG).show();
            // check if location is enabled
            if (isLocationEnabled()) {
                Toast.makeText(getApplicationContext(), "sendLocatione: isLocationEnabled", Toast.LENGTH_LONG).show();
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(LocationActivity.this, Locale.getDefault());
                            //initialize address list
                            try {
                                List<Address> address = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1
                                );
                                //set lat
                                latitude = address.get(0).getLatitude();
                                //set lon
                                longtitude = address.get(0).getLongitude();
                                //set country name
                                country_name = address.get(0).getCountryName();
                                //set locality
                                locality = address.get(0).getLocality();
                                //set address
                                address_name = address.get(0).getAddressLine(0);


                                ///////////////////////
                                Toast.makeText(getApplicationContext(), "sendLocatione: checkPremissionForSendSms", Toast.LENGTH_LONG).show();
                                checkPremissionForSendSms();

                                ///////////////////////

//                                lokTextView.setText("OK-SEND SMS");
//                                String[] TO = {"gal95elkayam@gmail.com"};
//                                String[] CC = {"gal95elkayam@gmail.com"};
//                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
//                                emailIntent.setData(Uri.parse("mailto:"));
//                                emailIntent.setType("text/plain");
//                                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
//                                emailIntent.putExtra(Intent.EXTRA_CC, CC);
//                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Location");
//                                emailIntent.putExtra(Intent.EXTRA_TEXT, "http://maps.google.com?q="+latitude+","+longtitude);
//
//                                try {
//                                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
//                                    finish();
//                                    Toast.makeText(getApplicationContext(), "Finished sending email...", Toast.LENGTH_SHORT).show();
//                                } catch (android.content.ActivityNotFoundException ex) {
//                                    Toast.makeText(LocationActivity.this,
//                                            "There is no email client installed.", Toast.LENGTH_SHORT).show();
//                                }
//

                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "problem", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "location is null", Toast.LENGTH_SHORT).show();
                            requestNewLocationData();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "sendLocation", Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            Toast.makeText(getApplicationContext(), "permissions aren't available", Toast.LENGTH_SHORT).show();
            requestPermissions();
        }
    }



    private void checkPremissionForSendSms() {
        //We’ll check the permission is granted or not . If not we’ll change
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)) {
                SmsManager smsManager = SmsManager.getDefault();
                StringBuffer smsBody = new StringBuffer();
                String message = "http://maps.google.com?q="+latitude+","+longtitude ;
                smsBody.append("I am in trouble.Please pick me from this location  " + message);
                String phoneNumber = "+9720523609597";
                smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), null, null);

            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_RQST_SEND);
                Toast.makeText(getApplicationContext(), "checkPremissionForSendSms: else- ActivityCompat", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "checkPremissionForSendSms: else", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
         mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);
        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            longtitude=mLastLocation.getLongitude();
            latitude=mLastLocation.getLatitude();
            checkPremissionForSendSms();
            SmsManager smsManager = SmsManager.getDefault();
            StringBuffer smsBody = new StringBuffer();
            String message = "http://maps.google.com/?q=" + Arrays.toString(new double[]{latitude}) + "," + Arrays.toString(new double[]{longtitude});
            smsBody.append("I am in trouble.Please pick me from this location  " + message);
            String phoneNumber = "+9720523609597";
            smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), null, null);
        }
    };


    }

