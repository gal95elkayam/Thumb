package com.example.thumb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thumb.client.ClientActivity;
import com.example.thumb.volunteer.VolunteerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    TextView btn;
    private EditText inputUserName,inputPassword,inputEmail,inputConformPassword,getCheckedRadioButtonId;
    Button btnRegister;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btn=findViewById(R.id.alreadyHaveAccount);
        inputUserName=findViewById(R.id.inputUsername);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        getCheckedRadioButtonId=findViewById(R.id.getCheckedRadioButtonId);
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.clearCheck();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    Toast.makeText(RegisterActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        inputConformPassword=findViewById(R.id.inputConformPassword);
        mAuth=FirebaseAuth.getInstance();
        mLoadingBar=new ProgressDialog(RegisterActivity.this);
        btnRegister=findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.checkCredentials();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

    }
    private void checkCredentials(){
        String username=inputUserName.getText().toString();
        String email=inputEmail.getText().toString();
        String password=inputPassword.getText().toString();
        String conformpassword=inputConformPassword.getText().toString();

        final RadioButton rb = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());

        if(username.isEmpty() ){
            showError(inputUserName,"Your username is not valid");
        }
        else if (email.isEmpty() || !email.contains("@")){
            showError(inputEmail,"Email is not valid!");
        }
        else if(password.isEmpty() || password.length()<6){
            showError(inputPassword,"Password should be at least 6 characters");
        }
        else if(conformpassword.isEmpty() || !conformpassword.equals(password)){
            showError(inputConformPassword,"Password not much!");
        }
        // no radio buttons are checked
        else if(radioGroup.getCheckedRadioButtonId() == -1){
            showError(getCheckedRadioButtonId, "select button- volunteer/need help");
        }
        else {
            mLoadingBar.setTitle("Registration");
            mLoadingBar.setMessage("Please wait, while check your credentials");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this,"Successfully Registration",Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                        Intent intent;
                        if(rb.getText().equals("volunteer")){
                            intent=new Intent(RegisterActivity.this, VolunteerActivity.class);
                        }
                        else{
                            intent=new Intent(RegisterActivity.this, ClientActivity.class);
                        }
                        //, this flag will cause any existing task that would be associated with the activity to be cleared before the activity is started
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this,"problem",Toast.LENGTH_SHORT).show();
                        Toast.makeText(RegisterActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    private void showError(EditText input, String s){
        input.setError(s);
        input.requestFocus();
    }
}