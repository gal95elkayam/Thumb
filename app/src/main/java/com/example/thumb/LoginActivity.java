package com.example.thumb;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private static final int RC_SIGN_IN_facebook = 100;
    private static final String TAG = "LoginActivity";
    private Button btnGoogle;
    private Button btnFacebook;
    private DatabaseReference mDatabase;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    TextView signUp;
    TextView forgotPassword;
    EditText inputEmail,inputPassword;
    Button btnLogin;
    private ProgressDialog mLoadingBar;
    //Facebook Declaration
    CallbackManager callbackManager;
    LoginManager loginManager;
    private FirebaseFirestore firebaseFirestore;
    int couner_premission=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        printKeyHash();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        forgotPassword=findViewById(R.id.forgotPassword);
        signUp =findViewById(R.id.textViewSignUp);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        btnLogin=findViewById(R.id.btnlogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.checkCrededentials();
            }
        });
        mLoadingBar=new ProgressDialog(LoginActivity.this);
        btnGoogle=findViewById(R.id.btnGoogle);
        mAuth=FirebaseAuth.getInstance();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputEmail.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginActivity.this, "please enter your email and then click Forgot Password", Toast.LENGTH_LONG).show();
                }
                else {
                    mAuth.sendPasswordResetEmail(inputEmail.getText().toString().trim())
                            .addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_LONG).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();

            }
        });
        btnFacebook=findViewById(R.id.btnFacebook);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "facebook:onclick:");
                signInFacebook();

            }
        });

    }

    private void signInFacebook(){
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email","public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("TAG=onSuccess"+TAG);
                Log.i("LoginActivity", "onSuccess");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            boolean isNew = Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser();
                            if(!isNew) {
                                updateUI(user,"Facebook");
                            }
                            else {
                                Intent intent=new  Intent(LoginActivity.this, RegisterActivityGoogelAndFacebook.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null,"Facebook");
                        }

                    }
                });
    }


    private void printKeyHash() {
        try {
            PackageInfo info=getPackageManager().getPackageInfo("com.example.thumb", PackageManager.GET_SIGNATURES);
             for(Signature signature:info.signatures)
             {
                 MessageDigest messageDigest=MessageDigest.getInstance("SHA");
                 messageDigest.update(signature.toByteArray());
                 Log.e("KeyHash", Base64.encodeToString(messageDigest.digest(),Base64.DEFAULT));

             }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void  checkCrededentials(){
        String email=inputEmail.getText().toString();
        String password=inputPassword.getText().toString();
        if(email.isEmpty() || !email.contains("@")){
            showError(inputEmail,"Email is not valid!");
        }
        else if (password.isEmpty() || password.length()<6){
            showError(inputPassword,"Password must be 6 character");
        }
        else {
            mLoadingBar.setTitle("Login");
            mLoadingBar.setMessage("Please wait, while check your credentials");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mLoadingBar.dismiss();
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user,"user");
                    }
                    else{
                        mLoadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "your password or email wrong, please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }
    private void showError(EditText input, String s){
        input.setError(s);
        input.requestFocus();
    }

    private void signIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
                // ...
            }
        }
        else {
            if(callbackManager.onActivityResult(requestCode, resultCode, data)){
                return;
            }

        }
    }
    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this,user.getEmail()+user.getDisplayName(),Toast.LENGTH_SHORT).show();
                            //to check if it's the first time user logs in, yes->need to register'no->have a count
                            boolean isNew = Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser();
                            if(!isNew) {
                               updateUI(user,"Google");
                            }
                            else{
                                Intent intent=new  Intent(LoginActivity.this, RegisterActivityGoogelAndFacebook.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }



    //where we send the user
    private void updateUI(FirebaseUser user, final String typeUser) {

        final Intent[] intent = new Intent[1];

        //check where we need to send each user--volunteer/need help
        FirebaseDatabase myRef = FirebaseDatabase.getInstance();
        DatabaseReference tripsRef = myRef.getReference().child("users").child(user.getUid());
        tripsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(TAG, "onDataChange: ");

                if (dataSnapshot.exists()) {
                    UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                    Log.d(TAG, "User name: " + userInformation.getName() + ", type: " + userInformation.getTypeUser());
                    if(userInformation.getTypeUser().equals("volunteer")){
                         intent[0] =new Intent(LoginActivity.this, firstScreenChat.class);
                    }
                    else{
                        intent[0] =new Intent(LoginActivity.this, PermissionActivity.class);

                    }
                    startActivity(intent[0]);
                    finish();

                }
                else {
                    switch (typeUser){
                        case "user":
                            deleteUser();
                            break;
                        case "Google":
                            deleteGoogleUser();
                            break;
                        case "Facebook":
                            deleteFacebookUser();
                            break;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }



    //delete user for reregister because he didnt finish the all progress of register
    public void deleteUser(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(inputEmail.getText().toString().trim(), inputPassword.getText().toString().trim());
        firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted!");
                            Toast.makeText(LoginActivity.this,"Please re-register, The registration process has stopped ",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }
    //delete user for reregister because he didnt finish the all progress of register
    public void deleteGoogleUser() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        try {
            AuthCredential credentialg = GoogleAuthProvider.getCredential(FirebaseAuth.getInstance().getCurrentUser().getUid(), null);
            // Prompt the user to re-provide their sign-in credentials
            if (firebaseUser != null) {
                firebaseUser.reauthenticate(credentialg)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                firebaseUser.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("Tag", "User account deleted.");
                                                    Toast.makeText(LoginActivity.this,"Please re-register, The registration process has stopped ",Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });

                            }
                        });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void deleteFacebookUser() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        try {
            AuthCredential credentialg = FacebookAuthProvider.getCredential(FirebaseAuth.getInstance().getCurrentUser().getProviderId());
            // Prompt the user to re-provide their sign-in credentials
            if (firebaseUser != null) {
                firebaseUser.reauthenticate(credentialg)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                firebaseUser.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("Tag", "User account deleted.");
                                                    Toast.makeText(LoginActivity.this,"Please re-register, The registration process has stopped ",Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });

                            }
                        });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }




    }

}