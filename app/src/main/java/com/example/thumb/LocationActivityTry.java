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
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationActivityTry extends AppCompatActivity {

    FusedLocationProviderClient mFusedLocationClient;
    double latitude;
    double longtitude;
    private static final int PERMISSION_RQST_SEND = 0;
    private static final int PERMISSION_RQST_Location = 1;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (hasLocationPermission() && hasSendSmsPermission() ) {
            //Start location services...
            sendLocationSMS();
        }
    }

    boolean hasLocationPermission () {
        //Check if the user has not granted permission...
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Prompt the user to grant permission...
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_RQST_Location);
            return false;
        }
        //Return true if the permission is already granted...
        return true;
    }


    boolean hasSendSmsPermission() {
        //Check if the user has not granted permission...
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            //Prompt the user to grant permission...
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_RQST_SEND);
            return false;
        }
        //Return true if the permission is already granted...
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check the users response...
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Start location services
            sendLocationSMS();
        }
    }

    @SuppressLint("MissingPermission")
    private void sendLocationSMS() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    Geocoder geocoder = new Geocoder(LocationActivityTry.this, Locale.getDefault());
                    //initialize address list
                    try {
                        List<Address> address = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        //set lat
                        latitude = address.get(0).getLatitude();
                        //set lon
                        longtitude = address.get(0).getLongitude();
                        sendSms();

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
    }


    private void sendSms() {

                SmsManager smsManager = SmsManager.getDefault();
                StringBuffer smsBody = new StringBuffer();
                String message = "http://maps.google.com?q="+latitude+","+longtitude ;
                smsBody.append("I am in trouble.Please pick me from this location  " + message);
                String phoneNumber = "+9720523609597";
                smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), null, null);

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
            sendSms();
        }
    };



}
