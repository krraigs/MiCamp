package com.kraigs.android.micamp.UserProfile;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.Mentor.ConnectMentor;
import com.kraigs.android.micamp.Mentor.MentorProfileActivity;
import com.kraigs.android.micamp.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    View v ;
    private RecyclerView requestListRv;
    private FirebaseAuth mAuth;
    private String currentUserId;
    CollectionReference userCol,reqCol;
    DatabaseReference notiRef;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_requests, container, false);


        notiRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userCol = FirebaseFirestore.getInstance().collection("User");
        reqCol = userCol;

        requestListRv = (RecyclerView) v.findViewById(R.id.chat_request_list);
        requestListRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        return  v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirestoreRecyclerOptions<ConnectMentor> options = new FirestoreRecyclerOptions.Builder<ConnectMentor>()
                .setQuery(reqCol.document(currentUserId).collection("Requests"),ConnectMentor.class)
                .build();

        FirestoreRecyclerAdapter<ConnectMentor,RequestViewHolder> adapter = new FirestoreRecyclerAdapter<ConnectMentor, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull ConnectMentor model) {

                final String list_user_id = getSnapshots().getSnapshot(position).getId();

                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setVisibility(View.VISIBLE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), MentorProfileActivity.class);
                        intent.putExtra("mentor_id",list_user_id);
                        startActivity(intent);
                    }
                });

                String type = getSnapshots().getSnapshot(position).get("request_type").toString();

                holder.acceptButton.setEnabled(true);
                holder.cancelButton.setEnabled(true);

                if (type.equals("recieved")){

                    userCol.document(list_user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot1) {
                            String requestImage = null;
                            if (snapshot1.contains("image")){
                                requestImage = snapshot1.get("image").toString();
                                Picasso.get().load(requestImage).into(holder.profileImage);
                            }

                            final String requestName = snapshot1.get("name").toString();
                            final String quality = snapshot1.get("quality").toString();

                            String finalRequestImage = requestImage;
                            userCol.document(currentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot snapshot2) {

                                    String userimage = null;
                                    if (snapshot2.contains("image")){
                                        userimage = snapshot2.get("image").toString();
                                        Picasso.get().load(userimage).into(holder.profileImage);
                                    }

                                    final String username = snapshot2.get("name").toString();
                                    final String userquality = snapshot2.get("quality").toString();

                                    holder.userName.setText(requestName);
                                    holder.userStatus.setText(quality);

                                    String finalUserimage = userimage;
                                    holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                            alertDialog.setTitle("Cancel").setMessage("Do you really want to accept a connect request?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    HashMap<String,String> map = new HashMap<>();
                                                    map.put("name",requestName);
                                                    map.put("quality",quality);
                                                    map.put("key",list_user_id);
                                                    if(finalRequestImage !=null && !TextUtils.isEmpty(finalRequestImage)){
                                                        map.put("image",finalRequestImage);
                                                    }

                                                    Log.d("request",username + " " + userquality );

                                                    userCol.document(currentUserId).collection("Friends").document(list_user_id)
                                                            .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                HashMap<String,String> usermap = new HashMap<>();
                                                                usermap.put("name",username);
                                                                usermap.put("quality",userquality);
                                                                usermap.put("key",currentUserId);
                                                                if(finalUserimage !=null && !TextUtils.isEmpty(finalUserimage)){
                                                                    usermap.put("image",finalUserimage);
                                                                }

                                                                userCol.document(list_user_id).collection("Friends").document(currentUserId)
                                                                        .set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            reqCol.document(currentUserId).collection("Requests").document(list_user_id).delete()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()){
                                                                                                reqCol.document(list_user_id).collection("Requests").document(currentUserId).delete()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()){
                                                                                                                    HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                                                                                    chatNotificationMap.put("from", currentUserId);
                                                                                                                    chatNotificationMap.put("type", "accept");
                                                                                                                    notiRef.child(list_user_id).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if (task.isSuccessful()) {
                                                                                                                                Toast.makeText(getActivity(), "New Mentor Saved ", Toast.LENGTH_SHORT).show();

                                                                                                                            } else {
                                                                                                                                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        } else{
                                                                            Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                        }

                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();

                                            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                                    alertDialog.setTitle("Cancel").setMessage("Do you really want to cancel a connect request?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            reqCol.document(currentUserId).collection("Requests").document(list_user_id).delete()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                reqCol.document(list_user_id).collection("Requests").document(currentUserId).delete()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getActivity(), "Request Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    }).show();

                                                }
                                            });
                                        }
                                    });


                                }
                            });


                        }
                    });
                }

                else if (type.equals("sent")){

                    holder.acceptButton.setEnabled(false);
                    holder.acceptButton.setVisibility(View.GONE);
                    holder.cancelButton.setEnabled(true);

                    userCol.document(list_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.contains("image")){
                                    final String requestImage = dataSnapshot.get("image").toString();

                                    Picasso.get().load(requestImage).into(holder.profileImage);
                                }

                                final String requestName = dataSnapshot.get("name").toString();
                                String quality = dataSnapshot.get("quality").toString();

                                holder.userName.setText(requestName);
                                holder.userStatus.setText(quality);

                                holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                        alertDialog.setTitle("Cancel request").setMessage("Do you really want to cancel a connect request?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                reqCol.document(currentUserId).collection("Requests").document(list_user_id).delete()                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            reqCol.document(list_user_id).collection("Requests").document(currentUserId).delete()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                notifyItemRemoved(position);
                                                                                Toast.makeText(getActivity(), "You have cancelled the request to connect!", Toast.LENGTH_SHORT).show();
                                                                            } else{
                                                                                Toast.makeText(getActivity(), "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }else{
                                                            Toast.makeText(getActivity(), "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();

                                    }
                                });
                            }

                        }
                    });
                }

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_request,viewGroup,false);
                return  new RequestViewHolder(view);
            }
        };

        requestListRv.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView userName , userStatus;
        CircleImageView profileImage;
        ImageView acceptButton,cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            cancelButton = itemView.findViewById(R.id.request_cancel_btn);

        }
    }

}
