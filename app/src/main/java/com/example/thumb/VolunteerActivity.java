package com.example.thumb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import id.privy.livenessfirebasesdk.LivenessApp;
import id.privy.livenessfirebasesdk.entity.LivenessItem;
import id.privy.livenessfirebasesdk.listener.PrivyCameraLivenessCallBackListener;

public class VolunteerActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 101;
    private static final String $SUCCESSTEXT ="Correct" ;
    private static final String $INSTRUCTIONS ="INSTRUCTIONS" ;
    private static final String $LEFT_MOTION_INSTRUCTION = "Left";
    private static final String $RIGHT_MOTION_INSTRUCTION ="Right" ;
    private static final String TAG ="VolunteerActivity" ;
    //check if the user upload selfie
    boolean firstpic_self=false;
    //check if the user upload identity
    boolean firstpic_identity =false;
    boolean firstpic_certificate =false;
    EditText inputName, inputLastName, inputId, inputPhoneNumber, forPicIdentity,forSelfie,forCertificate;
    private StorageReference mStorageRef;
    private ProgressDialog mLoadingBar;
    ImageView identity;
    ImageView certificate;
    ImageView selfie;
    Uri imageuri;
    Uri imageuri_certificate;
    DatabaseReference myRef;
    Button btnRegisterSolidier;
    Bitmap selected_image;
    Bitmap selected_image_certificate;
    Bitmap selcted_selfie;
    LivenessApp livenessApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);
        myRef = FirebaseDatabase.getInstance().getReference();
        inputName = findViewById(R.id.name);
        inputLastName = findViewById(R.id.lastName);
        inputId = findViewById(R.id.id);
        inputPhoneNumber = findViewById(R.id.phone_number);
        // Create a storage reference from our app
        mStorageRef = FirebaseStorage.getInstance().getReference();
        identity = (ImageView) findViewById(R.id.identity);
        certificate = (ImageView) findViewById(R.id.certificate);
        selfie=(ImageView) findViewById(R.id.selfie);
        forPicIdentity =findViewById(R.id.for_add_pic_identity);
        forCertificate=findViewById(R.id.for_add_pic_certificate);
        forSelfie=findViewById(R.id.for_add_pic2);
        mLoadingBar = new ProgressDialog(this);
        mLoadingBar.setTitle("Uploading..");
        //open image gallery
        identity.setOnClickListener(new View.OnClickListener() {
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
        certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });
        btnRegisterSolidier=findViewById(R.id.btnRegisterClient);
        btnRegisterSolidier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolunteerActivity.this.goto_navigation();
            }
        });
        //for selfie
        selfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                livenessApp = new LivenessApp.Builder(VolunteerActivity.this)
                        .setDebugMode(false) //to enable face landmark detection
                        .setSuccessText($SUCCESSTEXT)
                        .setInstructions($INSTRUCTIONS)
                        .setMotionInstruction($LEFT_MOTION_INSTRUCTION,$RIGHT_MOTION_INSTRUCTION).build();
                livenessApp.start(new PrivyCameraLivenessCallBackListener() {
                    @Override
                    public void success(LivenessItem livenessItem) {
                        //set image in imageView
                        selfie.setImageBitmap(getCroppedBitmap(livenessItem.getImageBitmap()));
                    }
                    @Override
                    public void failed(Throwable t) {
                        t.printStackTrace();

                    }
                });
            }
        });
    }
    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1) {
            imageuri = data.getData();
            try {
                //set image in imageView
                InputStream is = getContentResolver().openInputStream(imageuri);
                selected_image = BitmapFactory.decodeStream(is);
                identity.setImageBitmap(selected_image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (requestCode==2){
            imageuri_certificate = data.getData();
            try {
                //set image in imageView
                InputStream is1 = getContentResolver().openInputStream(imageuri_certificate);
                selected_image_certificate = BitmapFactory.decodeStream(is1);
                certificate.setImageBitmap(selected_image_certificate);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    public void goto_navigation() {
        String name = inputName.getText().toString();
        String lastName = inputLastName.getText().toString();
        String id = inputId.getText().toString();
        String phoneNumber = inputPhoneNumber.getText().toString();
        if (name.isEmpty()) {
            showError(inputName, "Your name is not valid!");
        } else if (lastName.isEmpty()) {
            showError(inputLastName, "Last Name is not valid!");
        } else if (id.isEmpty() || id.length() < 9) {
            showError(inputId, "Id must be 9 character");
        } else if (phoneNumber.isEmpty() || phoneNumber.length() <10) {
            showError(inputPhoneNumber, "phone Number not valid!");
        }  else if(selected_image==null){
            showError(forPicIdentity, "put identity pic!");
        }
        else if(selected_image_certificate==null) {
            showError(forCertificate, "put certificate pic!");
        }
        else if(selfie==null){
            showError(forSelfie, "put selfie!");
        }
        else {
            UserInformation userInformation = new UserInformation(name,lastName,id,phoneNumber,"volunteer");
            FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
            myRef.child("users").child(user.getUid()).setValue(userInformation);
            StorageReference ref_child=mStorageRef.child(user.getUid() + "/" + "IdCard.jpg");
            StorageReference certificateImage=mStorageRef.child(user.getUid() + "/" + "dischargeCertificate.jpg");
            StorageReference profileImage=mStorageRef.child(user.getUid() + "/" + "profileVolunteer.jpg");
            Bitmap bitImage = ((BitmapDrawable)selfie.getDrawable()).getBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] byteArray = bos.toByteArray();
            UploadTask uploadTask = profileImage.putBytes(byteArray);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(VolunteerActivity.this,"failure",Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(VolunteerActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                    //check if the user upload hoger
                    firstpic_self=true;
                }
            });

            ref_child.putFile(imageuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(VolunteerActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                            //check if the user upload identity
                            firstpic_identity =true;

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(VolunteerActivity.this,"FailureL",Toast.LENGTH_SHORT).show();
                        }
                    });
            certificateImage.putFile(imageuri_certificate)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(VolunteerActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                            //check if the user upload hoger
                            firstpic_certificate =true;
                            if(firstpic_identity && firstpic_self){
                                Intent intent=new Intent(VolunteerActivity.this,firstScreenChat.class);
                                startActivity(intent);
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(VolunteerActivity.this,"FailureL",Toast.LENGTH_SHORT).show();
                        }
                    });



        }

    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

}