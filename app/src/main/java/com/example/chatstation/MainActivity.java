package com.example.chatstation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ViewPager myviewPager;
    private TabLayout mytabLayout;
    private TabAccessorAdapter mytabAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        RootRef=FirebaseDatabase.getInstance().getReference();


        mtoolbar=(Toolbar)findViewById(R.id.main_page_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Chat Station");


        myviewPager=(ViewPager)findViewById(R.id.main_view_pager);
        TabAccessorAdapter mytabAccessorAdapter =new TabAccessorAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(mytabAccessorAdapter);

        mytabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myviewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser==null){
            sendUserToLoginActivity();
        }
        else{
            verifyUserExistance();
        }
    }

    private void verifyUserExistance() {
        String currentUserId=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

        if (item.getItemId()==R.id.main_logout_options){
            mAuth.signOut();
            sendUserToLoginActivity();

        }
        if (item.getItemId()==R.id.main_settings_options){
            sendUserToSettingsActivity();

        }
        if (item.getItemId()==R.id.main_find_friends_options){
            SendUserToFindFriendsActivity();

        }

        if(item.getItemId()==R.id.main_create_group_options){
            RequestNewGroup();
        }
        return true;

    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,
                R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("BrotherZzZ Zone");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String groupName=groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Enter Group Name", Toast.LENGTH_SHORT).show();
                }
                else {
                    createNewGroup(groupName);

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();

    }

    private void createNewGroup(final String groupName) {
        RootRef.child("Group").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName +"Group is created successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void sendUserToSettingsActivity(){
        Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);

    }

    private void SendUserToFindFriendsActivity()
    {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

}
