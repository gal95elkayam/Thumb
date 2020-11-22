package com.example.thumb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SolidierActivity extends AppCompatActivity {
    EditText inputName,inputLastName,inputId,inputReleseDate,inputPersonalNumber;
    private StorageReference mStorageRef;
    private ProgressDialog mLoadingBar;
    ImageView hoger;
    Uri imageuri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soldier);
        inputName=findViewById(R.id.name);
        inputLastName=findViewById(R.id.lastname);
        inputId=findViewById(R.id.id);
        inputPersonalNumber=findViewById(R.id.personNumber);
        inputReleseDate=findViewById(R.id.releaseDate);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        hoger=(ImageView)findViewById(R.id.hoger);
        mLoadingBar=new ProgressDialog(this);
        mLoadingBar.setTitle("Uploading..");
        //open image gallery
        hoger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageuri=data.getData();
        try{
            //set image in imageView
            InputStream is=getContentResolver().openInputStream(imageuri);
            final Bitmap selected_image= BitmapFactory.decodeStream(is);
            hoger.setImageBitmap(selected_image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
