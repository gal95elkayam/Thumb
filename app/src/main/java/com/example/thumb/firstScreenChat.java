package com.example.thumb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class firstScreenChat extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button startBtn;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trytwo);
        startBtn = findViewById(R.id.startbutton);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                myRef.child("chat").child(user.getUid()).setValue(user.getDisplayName());
                Intent intent=new Intent(firstScreenChat.this,message.class);
                startActivity(intent);
                finish();

            }
        });

    }

}
