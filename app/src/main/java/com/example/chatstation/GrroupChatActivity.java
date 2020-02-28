package com.example.chatstation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ScrollingView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GrroupChatActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef,groupNameRef,groupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grroup_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GrroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();



        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupName);





        InitializeFields();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void InitializeFields() {

        mtoolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton=findViewById(R.id.send_msg_button);
        userMessageInput=findViewById(R.id.input_group_msg);
        mScrollView=findViewById(R.id.my_scroll_view);
        displayTextMessages=findViewById(R.id.group_chat_text_display);


    }

    private void GetUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageInfoToDatabase() {
        String message=userMessageInput.getText().toString();
        String messageKey=groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please Write something", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd,yyyy");
            currentDate=currentDateFormat.format(calForDate.getTime());

            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormate=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormate.format(calForTime.getTime());

            HashMap<String,Object> groupMessageKey=new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);
            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String ,Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);
        }

    }

    private void displayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator=dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatDate=(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessages=(String)((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName +":\n  " + chatMessages +": \n"+
                    chatDate +": \n" + chatTime +"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }
}
