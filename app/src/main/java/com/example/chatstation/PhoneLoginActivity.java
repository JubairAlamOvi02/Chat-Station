package com.example.chatstation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.app.ProgressDialog;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationCodeButton, verifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationCodeButton = findViewById(R.id.send_ver_button);
        verifyButton = findViewById(R.id.verify_button);
        InputPhoneNumber = findViewById(R.id.phone_number_input);
        InputVerificationCode = findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String phoneNumber = InputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Type phone number", Toast.LENGTH_SHORT).show();
                } else {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please Wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks

                    verifyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                            InputPhoneNumber.setVisibility(View.INVISIBLE);

                            String verificationCode = InputVerificationCode.getText().toString();
                            if (TextUtils.isEmpty(verificationCode)) {
                                Toast.makeText(PhoneLoginActivity.this, "Enter Code", Toast.LENGTH_SHORT).show();
                            } else {

                                loadingBar.setTitle("Code Verification");
                                loadingBar.setMessage("Please Wait...while we are Verifying");
                                loadingBar.setCanceledOnTouchOutside(false);
                                loadingBar.show();

                                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                                signInWithPhoneAuthCredential(credential);
                            }
                        }
                    });

                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }


            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Invalid " +
                        "Phone Number,Enter Correct code", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }


            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                loadingBar.dismiss();


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code sent",
                        Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);


            }

        };


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();


                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();

                        }
                    }


                });
    }

    private void sendUserToMainActivity() {

        Intent mainintent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}


