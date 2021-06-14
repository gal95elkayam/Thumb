package com.example.thumb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class RegisterActivityGoogelAndFacebook extends AppCompatActivity {

    Button btnRegisterClient;
    Button btnRegisterVoulenteer;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_for_google);

        btnRegisterClient=findViewById(R.id.buttonClient);
        btnRegisterVoulenteer= findViewById(R.id.buttonVoulenteer);
        final Intent[] intent = new Intent[1];

        btnRegisterClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent[0] =new Intent(RegisterActivityGoogelAndFacebook.this, ClientActivity.class);
                startActivity(intent[0]);
                finish();
            }
        });

        btnRegisterVoulenteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent[0] =new Intent(RegisterActivityGoogelAndFacebook.this, VolunteerActivity.class);
                startActivity(intent[0]);


            }
        });

    }
























}
