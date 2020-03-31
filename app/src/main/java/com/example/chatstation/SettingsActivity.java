package com.example.chatstation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    private CircleImageView userProfileImage;
    private Button UpdateAccountSettings, photoChange;
    private EditText userName, userStatus;
    private ImageView  userImage;


    private String currentUserId, saveCurrentDate, saveCurrentTime, userNameP, userStatusP;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingbar;
   // private Uri ImageUri;
    private String ProductRandomKey;
    private String imageRandomKey, downloadImageUrl;
    //private ProgressDialog loadingBar;


    private static final int GalleryPick = 1;
    //private android.widget.Toast Toast;
    private Uri ImageUri;
    // private Uri ImageUri;
    //  private Uri imageUri;

    private void InitializeFields() {
        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_User_Name);
        userStatus = findViewById(R.id.set_profile_status);
        //userProfileImage = findViewById(R.id.set_profile_image);
        loadingbar = new ProgressDialog(this);
        userImage = findViewById(R.id.set_user_image);
        photoChange = findViewById(R.id.pic_change_button);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference().child("Images");
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();
        userName.setVisibility(View.INVISIBLE);


       /* UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // UpdateSettings();

                //StoreProductInformation();
            }
        });
        //RetriveUserInfo();
        *//*SaveProductInfoToDatabase();*//*
*/
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();

            }
        });

        photoChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateImageData();
            }
        });


    }






    private void openGallery() {
        Intent galaryIntent = new  Intent();
        galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galaryIntent.setType("image/*");
        startActivityForResult(galaryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
           ImageUri = data.getData();
            userImage.setImageURI(ImageUri);
        }
    }

    private void ValidateImageData() {

        if (ImageUri == null)
        {
            //openGallery();
            Toast.makeText(this, " image is mandatory...", Toast.LENGTH_SHORT).show();
        }


        else
        {
            StoreImageInformation();
        }
    }

    private void StoreImageInformation() {
        loadingbar.setTitle("Add New Image");
        loadingbar.setMessage("Dear Admin, please wait while we are adding the new Image.");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();


        Calendar calendar = Calendar.getInstance();

        //SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        imageRandomKey = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = UserProfileImageRef.child(ImageUri.getLastPathSegment() + imageRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingbar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SettingsActivity.this, "Product Image uploaded Successfully...", Toast.LENGTH_SHORT).show();
                Task<Uri> uriTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful())
                        {
                            downloadImageUrl = task.getResult().toString();

                            Toast.makeText(SettingsActivity.this, "got the Product image Url Successfully...", Toast.LENGTH_SHORT).show();

                            SaveImageInfoToDatabase();
                        }

                    }
                });

            }
        });

    }
    private void SaveImageInfoToDatabase() {
        HashMap<String, Object> imageMap = new HashMap<>();
        imageMap.put("pid", imageRandomKey);
        imageMap.put("date", saveCurrentDate);
        imageMap.put("time", saveCurrentTime);
        imageMap.put("image", downloadImageUrl);

        RootRef.child(imageRandomKey).updateChildren(imageMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                            startActivity(intent);

                            loadingbar.dismiss();
                            Toast.makeText(SettingsActivity.this, "Image is added successfully.", Toast.LENGTH_SHORT).show();

                           //Toast.makeText(SettingsActivity.this, "Image is added successfully..", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            loadingbar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

}

       /* UserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opengallery();
                ValidateProductData();
            }
        });




    }

    private void opengallery() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            ImageUri = data.getData();
            UserImage.setImageURI(ImageUri);
        }
    }

    private void ValidateProductData()
    {

        if (ImageUri == null)
        {
            Toast.makeText(this, "Product image is mandatory...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            StoreProductInformation();
        }
    }

    private void StoreProductInformation()
    {
        loadingbar.setTitle("Set profile image");
        loadingbar.setMessage("Please wait until your image is updating");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = UserProfileImageRef.child(ImageUri.getLastPathSegment() + productRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);


        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String message = e.toString();
                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Toast.makeText(SettingsActivity.this, "Product Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            downloadImageUrl = task.getResult().toString();

                            Toast.makeText(SettingsActivity.this, "got the Product image Url Successfully...", Toast.LENGTH_SHORT).show();

                            SaveProductInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void SaveProductInfoToDatabase()
    {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", Description);
        productMap.put("image", downloadImageUrl);
        productMap.put("category", CategoryName);
        productMap.put("price", Price);
        productMap.put("pname", Pname);

        HashMap<String,Object> profileMap=new  HashMap<>();
        profileMap.put("uid",currentUserId);
        profileMap.put("name",userNameP);
        profileMap.put("status",userStatusP);
        profileMap.put("image", downloadImageUrl);

        RootRef.child(productRandomKey).updateChildren(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                            startActivity(intent);

                            loadingBar.dismiss();
                            Toast.makeText(SettingsActivity.this, "Product is added successfully..", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}





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
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                })
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    Uri downloadUri=(Uri)task.getResult();
                                    String download=downloadUri.toString();

                                    RootRef.child("Users").child(currentUserId).child("image")
                                            .setValue(download)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SettingsActivity.this, "Image save in database successfully", Toast.LENGTH_SHORT).show();

                                                    } else {

                                                        String message = task.getException().toString();
                                                        Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                                        loadingbar.dismiss();

                                                    }
                                                }
                                            });
                                }

                            }
                        });

            }

        }





    }




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
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }





*/