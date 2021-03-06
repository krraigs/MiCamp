package com.kraigs.android.micamp.Chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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
import com.kraigs.android.micamp.Mentor.ConnectedMentorsActivity;
import com.kraigs.android.micamp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    View v;
    private RecyclerView chatsList;
    private DatabaseReference chatRef,friendsRef,channelRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    FloatingActionButton chatFb;
    LinearLayoutManager linearLayoutManager;
    CollectionReference userCol;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_chat, container, false);

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Message").child(currentUserId);
        channelRef = FirebaseDatabase.getInstance().getReference().child("ChatChannel").child(currentUserId);
        chatsList = (RecyclerView) v.findViewById(R.id.chats_list);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        chatsList.setLayoutManager(linearLayoutManager);
        chatFb = v.findViewById(R.id.chatFb);
        userCol = FirebaseFirestore.getInstance().collection("User");

        channelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    v.findViewById(R.id.noChat).setVisibility(View.GONE);
                    chatsList.setVisibility(View.VISIBLE);

                } else{
                    v.findViewById(R.id.noChat).setVisibility(View.VISIBLE);
                    chatsList.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);

        chatFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ConnectedMentorsActivity.class);
                startActivity(intent);
            }
        });

        return  v;

    }

    @Override
    public void onStart() {
        super.onStart();

        Query channelQuery = FirebaseDatabase.getInstance().getReference().child("ChatChannel").child(currentUserId).orderByChild("timestamp");
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(channelQuery,User.class)
                .build();

        FirebaseRecyclerAdapter<User,ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<User, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull User model) {
                final String userIDs = getRef(position).getKey();
                final String profileImage[] = {"default_image"};
                holder.onlineStatus.setVisibility(View.GONE);

                Query lastQuery = chatRef.child(userIDs).orderByKey().limitToLast(1);
                lastQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        String message = dataSnapshot.child("message").getValue().toString();
//                        holder.userStatus.setText(message);

                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            String message = child.child("message").getValue().toString();
                            String seen = child.child("seen").getValue().toString();
                            String type = child.child("type").getValue().toString();
                            if (type.equals("image")){
                                holder.userStatus.setText("New Image");
                            } else  if (type.equals("pdf")){
                                holder.userStatus.setText("New File");
                            } else{
                                holder.userStatus.setText(message);
                            }

                            if (seen.equals("false")){
                                holder.userStatus.setTypeface(null, Typeface.BOLD);
                                holder.userStatus.setTextColor(Color.BLACK);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Handle possible errors.
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog.setTitle("Delete").setMessage("Do you want to delete this chat?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                notifyItemRemoved(position);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();

                        return true;
                    }
                });

                userCol.document(userIDs).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.contains("image")){
                                profileImage[0] = dataSnapshot.get("image").toString();
                                Picasso.get().load(profileImage[0])
                                        .placeholder(R.drawable.user_profile_image)
                                        .into(holder.profileImage);
                            }

                            if(dataSnapshot.contains("online")){
                                String onlineStatus = dataSnapshot.get("online").toString();
                                if (onlineStatus.equals("true")){
                                    holder.onlineStatus.setVisibility(View.VISIBLE);
                                } else{
                                    holder.onlineStatus.setVisibility(View.GONE);
                                }
                            }

                            if(dataSnapshot.contains("name")){
                                final String profileName = dataSnapshot.get("name").toString();
                                holder.userName.setText(profileName);
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("visit_user_id",userIDs);
                                    startActivity(intent);

                                }
                            });
                        }

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_chat,viewGroup,false);
                return new ChatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        linearLayoutManager.smoothScrollToPosition(chatsList, null, 0);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                chatsList.smoothScrollToPosition(positionStart + 1);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
            }
        });

        adapter.startListening();
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName , userStatus;
        CircleImageView profileImage;
        ImageView onlineStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
        }
    }
}