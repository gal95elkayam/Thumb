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
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RecoredAndLocationActivity extends AppCompatActivity {
    // initializing
    // FusedLocationProviderClient
    // object
    FusedLocationProviderClient mFusedLocationClient;
    double latitude;
    double longtitude;
    String country_name;
    String locality;
    String address_name;
    int PERMISSION_ID = 44;
    TextView lokTextView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        lokTextView = findViewById(R.id.okTextView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
    }
    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            sendLocationSMS();
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

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    public void sendLocationSMS() {
        if (checkPermissions()) {
            Toast.makeText(getApplicationContext(), "checkPermissions", Toast.LENGTH_SHORT).show();
            // check if location is enabled
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(RecoredAndLocationActivity.this, Locale.getDefault());
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
                                lokTextView.setText("OK-SEND SMS");
                                String[] TO = {"gal95elkayam@gmail.com"};
                                String[] CC = {"gal95elkayam@gmail.com"};
                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.setData(Uri.parse("mailto:"));
                                emailIntent.setType("text/plain");
                                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                                emailIntent.putExtra(Intent.EXTRA_CC, CC);
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Location");
                                emailIntent.putExtra(Intent.EXTRA_TEXT, "http://maps.google.com?q="+latitude+","+longtitude);
                                try {
                                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                                    finish();
                                    Toast.makeText(getApplicationContext(), "Finished sending email...", Toast.LENGTH_SHORT).show();
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(RecoredAndLocationActivity.this,
                                            "There is no email client installed.", Toast.LENGTH_SHORT).show();
                                }
//                                SmsManager smsManager = SmsManager.getDefault();
//                                StringBuffer smsBody = new StringBuffer();
//                                smsBody.append("http://maps.google.com?q=");
//                                smsBody.append(latitude);
//                                smsBody.append(",");
//                                smsBody.append(longtitude);
//                                String phoneNumber = "0526240628";
//                                smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), null, null);
//                                Toast.makeText(getApplicationContext(), "success send message", Toast.LENGTH_SHORT).show();
//                                lokTextView.setText("OK-SEND SMS");

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
            lokTextView.setText("OK-SEND SMS");
            String[] TO = {"gal95elkayam@gmail.com"};
            String[] CC = {"gal95elkayam@gmail.com"};
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_CC, CC);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Location");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "http://maps.google.com?q="+latitude+","+longtitude);
            try {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                finish();
                Toast.makeText(getApplicationContext(), "Finished sending email...", Toast.LENGTH_SHORT).show();
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(RecoredAndLocationActivity.this,
                        "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }
        }
    };


    }

