package com.kraigs.android.micamp.Mentor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.Chat.ChatActivity;
import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class MentorProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference notificationRef;

    private TextView nameTv,branchTv,yearTv,courseTv,qualityTv,summaryTv,specialityTv,skillsTv,achievementsTv,experienceTv,contactTv,personalDetailsTv;

    private CircleImageView profilePic;

    private ImageView editBasicInfo;
    private Button connectButton, cancelButton;
    private String currentState, senderUserId,recieverUserID;
    CollectionReference userCol,reqCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_profile);

        recieverUserID = getIntent().getStringExtra("mentor_id");

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        userCol = FirebaseFirestore.getInstance().collection("User");
        reqCol = userCol;

        notificationRef.keepSynced(true);

        initializeFields();

        currentState = "new";

        getSupportActionBar().setTitle("Mentor Profile");

        editBasicInfo.setVisibility(View.INVISIBLE);
        retrieveMentorInfo();
    }

    private void retrieveMentorInfo() {

        userCol.document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {

                if (dataSnapshot.exists()) {

                    String retrieveName = dataSnapshot.get("name").toString();
                    String retrieveBranch = dataSnapshot.get("branch").toString();
                    String retrieveYear = dataSnapshot.get("year").toString();
                    String retrieveCourse = dataSnapshot.get("course").toString();
                    String retrieveQuality = dataSnapshot.get("quality").toString();
                    String retrieveSummary = dataSnapshot.get("summary").toString();

                    nameTv.setText(retrieveName);
                    branchTv.setText(retrieveBranch);
                    yearTv.setText(retrieveYear);
                    courseTv.setText(retrieveCourse);
                    qualityTv.setText(retrieveQuality);
                    summaryTv.setText(retrieveSummary);

                    ManageChatRequest();


                    if (dataSnapshot.contains("speciality")) {
                        String retrieveSpeciality = dataSnapshot.get("speciality").toString();
                        specialityTv.setText(retrieveSpeciality);
                    }
                    if (dataSnapshot.contains("skills")) {
                        String retrieveSkills = dataSnapshot.get("skills").toString();
                        skillsTv.setText(retrieveSkills);
                    }
                    if (dataSnapshot.contains("achievements")) {
                        String retrieveAchievements = dataSnapshot.get("achievements").toString();
                        achievementsTv.setText(retrieveAchievements);
                    }
                    if (dataSnapshot.contains("experience")) {
                        String retrieveExperience = dataSnapshot.get("experience").toString();
                        experienceTv.setText(retrieveExperience);
                    }
                    if (dataSnapshot.contains("contact")) {
                        String retrieveContact = dataSnapshot.get("contact").toString();
                        contactTv.setText(retrieveContact);
                    }
                    if (dataSnapshot.contains("personal")) {
                        String retrievePersonal = dataSnapshot.get("personal").toString();
                        personalDetailsTv.setText(retrievePersonal);
                    }
                    if (dataSnapshot.contains("image")) {
                        String retrieveProfleImage = dataSnapshot.get("image").toString();
                        Picasso.get().load(retrieveProfleImage)
                                .into(profilePic);
                    }
                }


            }
        });
    }

    private void ManageChatRequest() {

        reqCol.document(senderUserId).collection("Requests").document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if(snapshot.exists()){
                    String requestType = snapshot.get("request_type").toString();
                    if (requestType.equals("sent")) {
                        currentState= "request_sent";
                        connectButton.setText("CANCEL");
                        cancelButton.setText("PENDING");
                        cancelButton.setEnabled(false);

                    } else {
                        currentState= "request_recieved";
                        connectButton.setText("ACCEPT");
                        cancelButton.setVisibility(View.VISIBLE);
                        cancelButton.setText("CANCEL");
                        cancelButton.setEnabled(true);
                    }
                }
            }
        });

        userCol.document(senderUserId).collection("Friends").document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if(snapshot.exists()){
                    currentState= "friends";
                    connectButton.setText("REMOVE");
                    cancelButton.setText("Message");
                    cancelButton.setEnabled(true);
                    connectButton.setEnabled(true);
                }
            }
        });

        if (!senderUserId.equals(recieverUserID)) {
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (currentState.equals("new")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MentorProfileActivity.this);
                        alertDialog.setTitle("Send Request").setMessage("Do you really want to send a connect request?").setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendChatRequest();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }

                    if (currentState.equals("request_sent")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MentorProfileActivity.this);
                        alertDialog.setTitle("Delete Request").setMessage("Do you really want to delete a send request?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelChatRequest();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }

                    if (currentState.equals("request_recieved")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MentorProfileActivity.this);
                        alertDialog.setTitle("Accept Request").setMessage("Do you really want to accept a connect request?").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AcceptChatRequest();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }

                    if (currentState.equals("friends")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MentorProfileActivity.this);
                        alertDialog.setTitle("Remove Mentor").setMessage("Do you really want to remove a connected mentor?").setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RemoveSpecificMentor();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

                    }

                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (currentState.equals("new")) {
                        dismiss();
                    }

                    if (currentState.equals("request_recieved")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MentorProfileActivity.this);
                        alertDialog.setTitle("Confirm").setMessage("Do you really want to delete a connect request?").setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelChatRequest();

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }

                    if (currentState.equals("friends")) {

                        messageMentor();
                    }
                }

            });

        } else {

            connectButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        }
    }

    private void sendChatRequest() {

        HashMap<String,String> map = new HashMap<>();
        map.put("request_type","sent");

        HashMap<String,String> map2 = new HashMap<>();
        map2.put("request_type","recieved");

        reqCol.document(senderUserId).collection("Requests").document(recieverUserID)
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reqCol.document(recieverUserID).collection("Requests").document(senderUserId).set(map2)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserId);
                                                chatNotificationMap.put("type", "request");

                                                notificationRef.child(recieverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    connectButton.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    connectButton.setText("CANCEL");

                                                                    cancelButton.setText("PENDING");
                                                                    cancelButton.setEnabled(false);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        reqCol.document(senderUserId).collection("Requests").document(recieverUserID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reqCol.document(recieverUserID).collection("Requests").document(senderUserId).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                connectButton.setEnabled(true);
                                                currentState = "new";
                                                connectButton.setText("CONNECT");

                                                cancelButton.setVisibility(View.VISIBLE);
                                                cancelButton.setText("DISMISS");
                                                cancelButton.setEnabled(true);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {

        userCol.document(recieverUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot1) {
                String requestImage = null;
                if (snapshot1.contains("image")){
                    requestImage = snapshot1.get("image").toString();
                }

                final String requestName = snapshot1.get("name").toString();
                final String quality = snapshot1.get("quality").toString();

                String finalRequestImage = requestImage;
                userCol.document(senderUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot2) {

                        String userimage = null;
                        if (snapshot2.contains("image")){
                            userimage = snapshot2.get("image").toString();
                        }

                        final String username = snapshot2.get("name").toString();
                        final String userquality = snapshot2.get("quality").toString();


                        HashMap<String,String> map = new HashMap<>();
                        map.put("name",requestName);
                        map.put("quality",quality);
                        map.put("key",recieverUserID);
                        if(finalRequestImage !=null && !TextUtils.isEmpty(finalRequestImage)){
                            map.put("image", finalRequestImage);
                        }

                        HashMap<String,String> usermap = new HashMap<>();
                        usermap.put("name",username);
                        usermap.put("quality",userquality);
                        usermap.put("key",senderUserId);
                        if(userimage !=null && !TextUtils.isEmpty(userimage)){
                            usermap.put("image",userimage);
                        }

                        userCol.document(recieverUserID).collection("Friends").document(senderUserId)
                                .set(usermap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            userCol.document(senderUserId).collection("Friends").document(recieverUserID)
                                                    .set(map)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                reqCol.document(senderUserId).collection("Requests").document(recieverUserID).delete()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    reqCol.document(recieverUserID).collection("Requests").document(senderUserId).delete()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                                                                        chatNotificationMap.put("from", senderUserId);
                                                                                                        chatNotificationMap.put("type", "accept");
                                                                                                        notificationRef.child(recieverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    connectButton.setEnabled(true);
                                                                                                                    currentState = "friends";
                                                                                                                    connectButton.setText("REMOVE");
                                                                                                                    cancelButton.setText("Message");
                                                                                                                    cancelButton.setEnabled(true);
                                                                                                                } else {
                                                                                                                    Toast.makeText(MentorProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            }
                                                                                                        });

                                                                                                    } else {
                                                                                                        Toast.makeText(MentorProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                } else {
                                                                                    Toast.makeText(MentorProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                            } else {
                                                                Toast.makeText(MentorProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(MentorProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                });

            }
        });
    }

    private void RemoveSpecificMentor() {
        userCol.document(senderUserId).collection("Friends").document(recieverUserID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    userCol.document(recieverUserID).collection("Friends").document(senderUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                connectButton.setEnabled(true);
                                currentState = "new";
                                connectButton.setText("CONNECT");

                                cancelButton.setText("DISMISS");
                                cancelButton.setEnabled(true);

                            }
                        }
                    });
                }
            }
        });
    }

    private void messageMentor() {
        userCol.document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                if(dataSnapshot.exists()){
                    final String profileImage[] = {"default_image"};
                    if (dataSnapshot.contains("image")) {
                        profileImage[0] = dataSnapshot.get("image").toString();
                    }

                    final String profileName = dataSnapshot.get("name").toString();

                    Intent intent = new Intent(MentorProfileActivity.this, ChatActivity.class);
                    intent.putExtra("visit_user_id", recieverUserID);
                    intent.putExtra("visit_user_name", profileName);
                    intent.putExtra("visit_user_image", profileImage[0]);
                    startActivity(intent);

                }
            }
        });
    }

    private void dismiss() {

        Toast.makeText(this, "Choose another", Toast.LENGTH_SHORT).show();
    }

    private void initializeFields() {

        editBasicInfo = (ImageView) findViewById(R.id.userEditBasicInfo);

        specialityTv = (TextView) findViewById(R.id.mentorSpicialityTv);
        skillsTv = (TextView) findViewById(R.id.mentorSkillsTv);
        achievementsTv = (TextView) findViewById(R.id.mentorAchievementsTv);
        experienceTv = (TextView) findViewById(R.id.mentorExperienceTv);
        personalDetailsTv = (TextView) findViewById(R.id.mentorPersonalDetailsTv);
        contactTv = (TextView) findViewById(R.id.mentorContactsTv);

        nameTv = (TextView) findViewById(R.id.userNameTv);
        branchTv = (TextView) findViewById(R.id.userBranchTv);
        yearTv = (TextView) findViewById(R.id.userYearTv);
        courseTv = (TextView) findViewById(R.id.userCourseTv);
        qualityTv = (TextView) findViewById(R.id.userQualityTv);
        summaryTv = (TextView) findViewById(R.id.userSummaryTv);

        connectButton = (Button) findViewById(R.id.messageMentorBt);
        cancelButton = (Button) findViewById(R.id.mentorConnectBt);

        profilePic = (CircleImageView) findViewById(R.id.userProfilePic);
    }
}





















