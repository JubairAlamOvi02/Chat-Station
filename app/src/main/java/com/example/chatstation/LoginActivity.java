package com.example.chatstation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    //private FirebaseUser currentUser;
    private Button LoginButton,phoneLoginButton;
    private EditText UserEmail,UserPassword;
   private TextView NeedNewAccountLink,ForgetPasswordLink;

   private FirebaseAuth mAuth;
   private ProgressDialog lodingBar;





   @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        UserEmail=findViewById(R.id.login_email);
        UserPassword=findViewById(R.id.login_password);
        NeedNewAccountLink=findViewById(R.id.need_new_account_link);
        ForgetPasswordLink=findViewById(R.id.forget_password_link);

        lodingBar=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        //currentUser=mAuth.getCurrentUser();

        InitializeFields();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendUserToRegisterActivity();

            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });


    }

    private void AllowUserToLogin() {
       String email=UserEmail.getText().toString();
       String password=UserPassword.getText().toString();
        
       if (TextUtils.isEmpty(email)){
           Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
       }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }
        else {
            lodingBar.setTitle("Sing In ");
            lodingBar.setMessage("Please Wait ....");
            lodingBar.setCanceledOnTouchOutside(true);
            lodingBar.show();


            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "Logged in Successfuly ", Toast.LENGTH_SHORT).show();
                        lodingBar.dismiss();
                    }
                    else {
                        String message=task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }

    private void InitializeFields() {

       LoginButton=findViewById(R.id.login_button);
       phoneLoginButton=findViewById(R.id.phone_login_button);



    }

    /*@Override
    protected void onStart() {
        super.onStart();
        if (currentUser!=null){
            sendUserToMainActivity();

        }
    }*/

   /* private void sendUserToMainActivity() {
        Intent loginIntent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(loginIntent);
    }*/
   private void sendUserToMainActivity() {
       Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
       mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
       startActivity(mainIntent);
       finish();
   }

    private void sendUserToRegisterActivity(){
       Intent RegisterIntent=new Intent(LoginActivity.this,RegisterActivity.class);
       startActivity(RegisterIntent);
    }
}
