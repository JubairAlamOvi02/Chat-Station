package com.example.chatstation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    private CircleImageView userProfileImage;
    private Button userAccountSettings;
    private EditText userName,userStatus;







    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingbar;
    private SignInButton.ButtonSize;
    private String userid;

    public static int galaryPick=1;
   //  private Uri imageUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();
        userName.setVisibility(View.INVISIBLE);



        userAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });
        RetriveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galaryIntent=new Intent();
                galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galaryIntent.setType("image/*");
                startActivityForResult(galaryIntent,galaryPick);
            }
        });




    }




    private void InitializeFields() {
        userAccountSettings=findViewById(R.id.update_settings_button);
        userName=findViewById(R.id.set_User_Name);
        userStatus=findViewById(R.id.set_profile_status);
        userProfileImage=findViewById(R.id.set_profile_image);
        loadingbar=new ProgressDialog(this);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==galaryPick && resultCode==RESULT_OK && data!=null){

            Uri ImageUri =data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode ==RESULT_OK){
                loadingbar.setTitle("Set profile image");
                loadingbar.setMessage("Please wait until your image is updating");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();

                final Uri imageUri=result.getUri();

                final StorageReference filePath=UserProfileImageRef.child(currentUserId +"image.jpg");
                Task uploadTask =filePath.putFile(imageUri);

    private void UpdateSettings() {
        String SetUserName=userName.getText().toString();
        String SetUserStatus=userStatus.getText().toString();

        if (TextUtils.isEmpty(SetUserName)){
            Toast.makeText(this, "Please Write your UserName", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(SetUserStatus)){
            Toast.makeText(this, "Please write your Status", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String,String> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",SetUserName);
            profileMap.put("status",SetUserStatus);
            //profileMap.put("image",userProfileImage);
            RootRef.child("Users").child(currentUserId).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendUsertoMainActivity();
                                Toast.makeText(SettingsActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();


                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void RetriveUserInfo() {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") &&
                                (dataSnapshot.hasChild("image")))){
                            String retriveUsername=dataSnapshot.child("name").getValue().toString();
                            String retriveStatus=dataSnapshot.child("status").getValue().toString();
                            String retriveProfileImage=dataSnapshot.child("image").getValue().toString();

                            userName.setText(retriveUsername);
                            userStatus.setText(retriveStatus);
                            Picasso.get().load(retriveProfileImage).into(userProfileImage);


                        }
                        else if((dataSnapshot.hasChild("name"))&& (dataSnapshot.exists())){
                            String retriveUserName=dataSnapshot.child("name").getValue().toString();
                            String retriveStatus=dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);

                        }
                        else {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Update your Profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }


    private void sendUsertoMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
