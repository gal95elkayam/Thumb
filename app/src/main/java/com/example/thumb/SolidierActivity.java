package com.example.thumb;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SolidierActivity extends AppCompatActivity {
    EditText inputName, inputLastName, inputId, inputReleseDate, inputPersonalNumber,forPic;
    private StorageReference mStorageRef;
    private ProgressDialog mLoadingBar;
    ImageView hoger;
    Uri imageuri;
    DatabaseReference myRef;
    Button btnRegisterSolidier;
    Bitmap selected_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soldier);
        myRef = FirebaseDatabase.getInstance().getReference();
        inputName = findViewById(R.id.name);
        inputLastName = findViewById(R.id.lastname);
        inputId = findViewById(R.id.id);
        inputPersonalNumber = findViewById(R.id.personNumber);
        inputReleseDate = findViewById(R.id.releaseDate);
        // Create a storage reference from our app
        mStorageRef = FirebaseStorage.getInstance().getReference();
        hoger = (ImageView) findViewById(R.id.hoger);
        forPic=findViewById(R.id.for_add_pic);
        mLoadingBar = new ProgressDialog(this);
        mLoadingBar.setTitle("Uploading..");
        //open image gallery
        hoger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                //For example,we may start an activity that lets the user pick data from a list; when it ends, it returns the selected data to the first activity.
                // To do this, you call the startActivityForResult(Intent, int) version with a second integer parameter identifying the call
                startActivityForResult(intent, 1);
            }
        });
        btnRegisterSolidier=findViewById(R.id.btnRegisterSolider);

        btnRegisterSolidier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SolidierActivity.this.goto_navigation();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageuri = data.getData();
        try {
            //set image in imageView
            InputStream is = getContentResolver().openInputStream(imageuri);
            selected_image = BitmapFactory.decodeStream(is);
            hoger.setImageBitmap(selected_image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void goto_navigation() {
        String name = inputName.getText().toString();
        String lastName = inputLastName.getText().toString();
        String id = inputId.getText().toString();
        String personalNumber = inputPersonalNumber.getText().toString();
        String releseDate = inputReleseDate.getText().toString();
       // Bitmap emptyBitmap = Bitmap.createBitmap(selected_image.getWidth(), selected_image.getHeight(), selected_image.getConfig());
        if (name.isEmpty()) {
            showError(inputName, "Your name is not valid!");
        } else if (lastName.isEmpty()) {
            showError(inputLastName, "Last Name is not valid!");
        } else if (id.isEmpty() || id.length() < 9) {
            showError(inputId, "Id must be 9 character");
        } else if (personalNumber.isEmpty() || personalNumber.length() < 7) {
            showError(inputPersonalNumber, "Personal Number not much!");
        } else if (releseDate.isEmpty() || releseDate.length() < 10) {
            showError(inputPersonalNumber, "Relese Date not much!");
        } else if(selected_image==null){
            showError(forPic, "put pic!");
        }
        else {
            UserInformation userInformation = new UserInformation(name,lastName,id,personalNumber,releseDate);
            FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
            myRef.child("users").child(user.getUid()).setValue(userInformation);
            //myRef.child(user.getUid()).setValue(userInformation);
           // Toast.makeText(SolidierActivity.this,"Your Profile update Successfully",Toast.LENGTH_SHORT).show();
            StorageReference ref_child=mStorageRef.child(user.getUid() + "/" + "profilepic.jpg");
            ref_child.putFile(imageuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(SolidierActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(SolidierActivity.this,MainActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SolidierActivity.this,"FailureL",Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

}