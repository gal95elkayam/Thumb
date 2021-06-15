package com.example.thumb.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thumb.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class firstScreenChat extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button startBtn;
    private Button subscribe;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);
        startBtn = findViewById(R.id.startbutton);
        subscribe= findViewById(R.id.subscribe);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
             //   myRef.child("chat").child(user.getUid()).setValue(user.getDisplayName());
                Intent intent=new Intent(firstScreenChat.this, ChatActivity.class);
                startActivity(intent);
                finish();

            }
        });
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(firstScreenChat.this, SubscribeActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }

}
