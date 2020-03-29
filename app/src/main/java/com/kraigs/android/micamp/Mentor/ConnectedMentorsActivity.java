package com.kraigs.android.micamp.Mentor;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.Chat.ChatActivity;
import com.kraigs.android.micamp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConnectedMentorsActivity extends AppCompatActivity {

    private RecyclerView connectedMentorsRv;
    private FirebaseAuth mAuth;
    private String currentUserId;
    ArrayList<ConnectMentor> mentorList;
    CollectionReference userCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_mentors);

        getSupportActionBar().setTitle("Mentors");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        connectedMentorsRv = (RecyclerView) findViewById(R.id.connected_mentors_rv);
        connectedMentorsRv.setLayoutManager(new LinearLayoutManager(this));

        userCol = FirebaseFirestore.getInstance().collection("User");

    }

    @Override
    protected void onStart() {
        super.onStart();

        userCol.document(currentUserId).collection("Friends").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                mentorList = new ArrayList<>();

                mentorList.addAll(snapshots.toObjects(ConnectMentor.class));

                Collections.sort(mentorList,new SortBtName());
                FriendsAdapter adapter = new FriendsAdapter(mentorList);
                connectedMentorsRv.setAdapter(adapter);

            }
        });
    }

    class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendHolder>{

        ArrayList<ConnectMentor> list;

        FriendsAdapter(ArrayList<ConnectMentor> list){
            this.list = list;
        }

        @NonNull
        @Override
        public FriendsAdapter.FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_request,parent,false);
            return  new FriendHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsAdapter.FriendHolder holder, int position) {
            ConnectMentor model = list.get(position);
            final String userIDs = model.getKey();
            final String profileImage[] = {"default_image"};

            holder.cancelBt.setVisibility(View.GONE);
            holder.acceptBt.setVisibility(View.GONE);

            userCol.document(userIDs).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.contains("image")){
                            profileImage[0] = dataSnapshot.get("image").toString();
                            Picasso.get().load(profileImage[0]).into(holder.profileImage);
                        }

                        final String profileName = dataSnapshot.get("name").toString();
                        String profileStatus = dataSnapshot.get("quality").toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(profileStatus);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ConnectedMentorsActivity.this, ChatActivity.class);
                                intent.putExtra("visit_user_id",userIDs);
                                intent.putExtra("visit_user_name",profileName);
                                intent.putExtra("visit_user_image",profileImage[0]);
                                startActivity(intent);
                            }
                        });
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class FriendHolder extends RecyclerView.ViewHolder {


            TextView userName, userStatus;
            CircleImageView profileImage;
            ImageView acceptBt,cancelBt;

            public FriendHolder(@NonNull View itemView) {
                super(itemView);

                userName = itemView.findViewById(R.id.user_profile_name);
                userStatus = itemView.findViewById(R.id.user_status);
                profileImage = itemView.findViewById(R.id.users_profile_image);
                acceptBt = itemView.findViewById(R.id.request_accept_btn);
                cancelBt = itemView.findViewById(R.id.request_cancel_btn);
            }
        }
    }

    class SortBtName implements Comparator<ConnectMentor> {
        public int compare(ConnectMentor a, ConnectMentor b){
            return a.getName().compareTo(b.getName());
        }
    }
}





















