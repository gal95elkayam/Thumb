package com.example.thumb;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;
import android.content.pm.Signature;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
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
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private static final int RC_SIGN_IN_facebook = 100;
    private static final String TAG = "LoginActivity";
    private Button btnGoogle;
    private Button btnFacebook;
    private DatabaseReference mDatabase;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    TextView btn;
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
        btn=findViewById(R.id.textViewSignUp);
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
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(inputEmail.toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

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
                System.out.println("TAG=onclick"+TAG);
                Log.d(TAG, "facebook:onclick:");
                signInFacebook();

            }
        });

    }

    private void signInFacebook(){
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email","public_profile"));
        System.out.println("TAG=signInFacebook"+TAG);
        System.out.println("facebook:callbackManager:"+callbackManager);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("TAG=onSuccess"+TAG);
                Log.i("LoginActivity", "@@@onSuccess");
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
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
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
        else if (password.isEmpty() || password.length()<7){
            showError(inputPassword,"Password must be 7 character");
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
                        updateUI(user);
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
                firebaseAuthWithGoogle(account);
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
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this,user.getEmail()+user.getDisplayName(),Toast.LENGTH_SHORT).show();
                            //////////////////////////////////////////////////////////////////////////////////////////////////
                            //to check if it's the first time user logs in, yes->need to register'no->have a count
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            ///////////////////////////////////////////////////////////////////////////////
                            if(!isNew) {
                                updateUI(user);
                            }
                            else{
                                Intent intent=new  Intent(LoginActivity.this, RegisterActivityGoogel.class);
                                startActivity(intent);
                                finish();;

                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                           // updateUI(null);
                        }

                        // ...
                    }
                });
    }
    //where we send the user
    private void updateUI(FirebaseUser user) {

        final Intent[] intent = new Intent[1];

        //check where we need to send each user--volunteer/need help
       // DatabaseReference myRef= FirebaseDatabase.getInstance().getReference();
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
                        intent[0] =new Intent(LoginActivity.this, perm.class);

                    }
                    startActivity(intent[0]);
                    finish();
                }
                else {
                    Toast.makeText(LoginActivity.this,"dataSnapshot not exists ",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

}