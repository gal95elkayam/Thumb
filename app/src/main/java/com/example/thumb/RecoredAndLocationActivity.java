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
import java.util.List;
import java.util.Locale;

public class RecoredAndLocationActivity extends AppCompatActivity {
    ///////
    private static final int RECORDER_BPP = 16;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION =200 ;
    private static int RECORDER_SAMPLERATE = 8000;
    private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private StorageReference mStorageRef;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    ////
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
    ///
    // Create a storage reference from our app
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        lokTextView = findViewById(R.id.okTextView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        sendLocationSMS();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //////


        // Get the minimum buffer size required for the successful creation of an AudioRecord object.
        int bufferSizeInBytes = AudioRecord.getMinBufferSize( RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING
        );
        // Initialize Audio Recorder.
        AudioRecord audioRecorder = new AudioRecord( MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSizeInBytes
        );
        // Start Recording.
        audioRecorder.startRecording();

        int numberOfReadBytes   = 0;
        byte audioBuffer[]      = new  byte[bufferSizeInBytes];
        boolean recording       = false;
        float tempFloatBuffer[] = new float[3];
        int tempIndex           = 0;
        int totalReadBytes      = 0;
        byte totalByteBuffer[]  = new byte[60 * 44100 * 2];


        // While data come from microphone.
        while( true )
        {
            float totalAbsValue = 0.0f;
            short sample        = 0;

            numberOfReadBytes = audioRecorder.read( audioBuffer, 0, bufferSizeInBytes );

            // Analyze Sound.
            for( int i=0; i<bufferSizeInBytes; i+=2 )
            {
                sample = (short)( (audioBuffer[i]) | audioBuffer[i + 1] << 8 );
                totalAbsValue += Math.abs( sample ) / (numberOfReadBytes/2);
            }

            // Analyze temp buffer.
            tempFloatBuffer[tempIndex%3] = totalAbsValue;
            float temp                   = 0.0f;
            for( int i=0; i<3; ++i )
                temp += tempFloatBuffer[i];

            if( (temp >=0 && temp <= 350) && recording == false )
            {
                Log.i("TAG", "1");
                tempIndex++;
                continue;
            }

            if( temp > 350 && recording == false )
            {
                Log.i("TAG", "2");
                recording = true;
            }

            if( (temp >= 0 && temp <= 350) && recording == true )
            {
                Log.i("TAG", "Save audio to file.");

                // Save audio to file.
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filepath,"AudioRecorder");
                if( !file.exists() )
                    file.mkdirs();

                String fn = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav";

                long totalAudioLen  = 0;
                long totalDataLen   = totalAudioLen + 36;
                long longSampleRate = RECORDER_SAMPLERATE;
                int channels        = 1;
                long byteRate       = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
                totalAudioLen       = totalReadBytes;
                totalDataLen        = totalAudioLen + 36;
                byte finalBuffer[]  = new byte[totalReadBytes + 44];

                finalBuffer[0] = 'R';  // RIFF/WAVE header
                finalBuffer[1] = 'I';
                finalBuffer[2] = 'F';
                finalBuffer[3] = 'F';
                finalBuffer[4] = (byte) (totalDataLen & 0xff);
                finalBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
                finalBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
                finalBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
                finalBuffer[8] = 'W';
                finalBuffer[9] = 'A';
                finalBuffer[10] = 'V';
                finalBuffer[11] = 'E';
                finalBuffer[12] = 'f';  // 'fmt ' chunk
                finalBuffer[13] = 'm';
                finalBuffer[14] = 't';
                finalBuffer[15] = ' ';
                finalBuffer[16] = 16;  // 4 bytes: size of 'fmt ' chunk
                finalBuffer[17] = 0;
                finalBuffer[18] = 0;
                finalBuffer[19] = 0;
                finalBuffer[20] = 1;  // format = 1
                finalBuffer[21] = 0;
                finalBuffer[22] = (byte) channels;
                finalBuffer[23] = 0;
                finalBuffer[24] = (byte) (longSampleRate & 0xff);
                finalBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
                finalBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
                finalBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
                finalBuffer[28] = (byte) (byteRate & 0xff);
                finalBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
                finalBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
                finalBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
                finalBuffer[32] = (byte) (2 * 16 / 8);  // block align
                finalBuffer[33] = 0;
                finalBuffer[34] = RECORDER_BPP;  // bits per sample
                finalBuffer[35] = 0;
                finalBuffer[36] = 'd';
                finalBuffer[37] = 'a';
                finalBuffer[38] = 't';
                finalBuffer[39] = 'a';
                finalBuffer[40] = (byte) (totalAudioLen & 0xff);
                finalBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
                finalBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
                finalBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

                for( int i=0; i<totalReadBytes; ++i )
                    finalBuffer[44+i] = totalByteBuffer[i];

                FileOutputStream out;
                try {
                    out = new FileOutputStream(fn);
                    try {
                        out.write(finalBuffer);
                        out.close();

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                //*/
                tempIndex++;
                break;
            }

            // -> Recording sound here.
            Log.i( "TAG", "Recording Sound." );
            for( int i=0; i<numberOfReadBytes; i++ )
                totalByteBuffer[totalReadBytes + i] = audioBuffer[i];
            totalReadBytes += numberOfReadBytes;
            //*/

            tempIndex++;

        }
        



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
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
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

