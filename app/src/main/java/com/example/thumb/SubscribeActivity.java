package com.example.thumb;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SubscribeActivity extends AppCompatActivity {
    private static final String TAG ="notificationActivity" ;
    private static final String FCM_CHANNEL_ID="FCM_CHANNEL_ID";
    private Button mBtnSubscribe;
    private Button mUnBtnSubscribe;
    static TextView mOutput;
    private ImageView backToChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribe);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel fcmChannel = new NotificationChannel(
                    FCM_CHANNEL_ID, "FC_Channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(fcmChannel);
        }
        mBtnSubscribe = findViewById(R.id.buttonSubscribe);
        mUnBtnSubscribe = findViewById(R.id.buttonUnSubscribe);
        backToChat=findViewById(R.id.back);
        backToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SubscribeActivity.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        });


        mOutput =  (TextView) findViewById(R.id.textView2);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                Log.d(TAG, "ON COMPLITE" + task.getResult().getToken());
            }
        });
        if (getIntent() != null && getIntent().hasExtra("key")) {
            mOutput.setText("");
            for (String key : getIntent().getExtras().keySet()) {
                Log.d(TAG, "ONCREATE:KEY:   " + key + "DATA:   " + getIntent().getExtras().getString(key));
                mOutput.append(getIntent().getExtras().getString(key)+"\n");
            }
        }


        mBtnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseMessaging.getInstance().subscribeToTopic("power-topic").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Topic Subscribed", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "Failed Topic Subscribed", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        mUnBtnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseMessaging.getInstance().unsubscribeFromTopic("power-topic").
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(getApplicationContext(), "Topic UnSubscribed", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Failed Topic UnSubscribed", Toast.LENGTH_SHORT).show();
                            }

                        });

            }
        });



    }




}