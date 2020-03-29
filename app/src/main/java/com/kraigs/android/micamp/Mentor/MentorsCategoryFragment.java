package com.kraigs.android.micamp.Mentor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
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
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.Chat.ChatActivity;
import com.kraigs.android.micamp.R;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */

public class MentorsCategoryFragment extends Fragment {

    View v;
    private RecyclerView androidRv;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ArrayList<ConnectMentor> mentorList;
    CollectionReference userCol;
    Query userQuery;
    String category;
    DatabaseReference rootRef,notiRef;
    CollectionReference reqCol;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FirestorePagingAdapter<ConnectMentor,ConnectViewHolder> mAdapter;

    public MentorsCategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int position = FragmentPagerItem.getPosition(getArguments());

        if (position == 0){
            category = "Android Developer";
        }else if (position ==1){
            category = "Website Developer";
        } else if (position ==2){
            category = "Machine Learning";
        } else{
            category = "Default";
        }

        initializeFields();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        notiRef = rootRef.child("Notifications");
        userCol = FirebaseFirestore.getInstance().collection("User");
        reqCol = userCol;
        userCol = FirebaseFirestore.getInstance().collection("User");
        userQuery = userCol.orderBy("name");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.refresh();
            }
        });

        setupAdapter(userCol.orderBy("quality").startAt(category).endAt(category + "\uf8ff"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_mentors_category, container, false);
        return v;

    }

    private void initializeFields() {
        androidRv = (RecyclerView)v.findViewById(R.id.mentorRv);
        mSwipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);

        androidRv.setLayoutManager(new LinearLayoutManager(getContext()));
        androidRv.setHasFixedSize(true);

    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        userQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
//                mentorList = new ArrayList<>();
//
//                for(ConnectMentor mentor : snapshots.toObjects(ConnectMentor.class)){
//                    if(mentor.getQuality()!=null){
//
//                        if(mentor.getQuality().equals(category)){
//                            mentorList.add(mentor);
//                        }
//                    }
//                }
//
//                QualityAdapter adapterAndroid = new QualityAdapter(mentorList);
//
//                androidRv.setAdapter(adapterAndroid);
//                adapterAndroid.notifyDataSetChanged();
//            }
//        });
//    }

    private void setupAdapter(Query query) {

        // Init Paging Configuration
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(2)
                .setPageSize(20)
                .build();

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<ConnectMentor>()
                .setLifecycleOwner(this)
                .setQuery(query, config, ConnectMentor.class)
                .build();

        mAdapter = new FirestorePagingAdapter<ConnectMentor, ConnectViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull ConnectViewHolder holder, int position, @NonNull ConnectMentor model) {
                String mentorId = model.getKey();
                holder.userName.setText(model.getName());

                holder.userStatus.setText(model.getQuality());

                Picasso.get().load(model.getImage())
                        .placeholder(R.drawable.user_profile_image)
                        .into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), MentorProfileActivity.class);
                        intent.putExtra("mentor_id",mentorId);
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

                String recieverUserID = model.getKey();
                final String[] currentState = {"new"};
                if (!recieverUserID.equals(currentUserId)){
                    holder.connectTv.setVisibility(View.VISIBLE);
                } else{
                    holder.connectTv.setVisibility(View.GONE);
                }

                holder.branchTv.setText(model.getYear() + " " + model.getBranch());

                reqCol.document(currentUserId).collection("Requests").document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(snapshot.exists()){
                            String requestType = snapshot.get("request_type").toString();
                            if (requestType.equals("sent")) {
                                currentState[0] = "request_sent";
                                holder.connectTv.setText("Request Sent");
                                holder.connectTv.setEnabled(true);

                            } else {
                                currentState[0] = "request_recieved";
                                holder.connectTv.setText("Confirm");
                                holder.connectTv.setEnabled(true);
                            }
                        }
                    }
                });

                userCol.document(currentUserId).collection("Friends").document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(snapshot.exists()){
                            currentState[0] = "friends";
                            holder.connectTv.setText("Friends");
                            holder.connectTv.setEnabled(true);
                        }
                    }
                });

                holder.connectTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentState[0].equals("new")) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                            alertDialog.setTitle("Send Request").setMessage("Do you really want to send a connect request?").setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    HashMap<String,String> map = new HashMap<>();
                                    map.put("request_type","sent");

                                    HashMap<String,String> map2 = new HashMap<>();
                                    map2.put("request_type","recieved");

                                    reqCol.document(currentUserId).collection("Requests").document(recieverUserID)
                                            .set(map)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        reqCol.document(recieverUserID).collection("Requests").document(currentUserId).set(map2)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                                            chatNotificationMap.put("from", currentUserId);
                                                                            chatNotificationMap.put("type", "request");
                                                                            notiRef.child(recieverUserID).push()
                                                                                    .setValue(chatNotificationMap)
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                holder.connectTv.setEnabled(true);
                                                                                                currentState[0] = "request_sent";
                                                                                                holder.connectTv.setText("Cancel Request");
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
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }

                        if (currentState[0].equals("request_sent")) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                            alertDialog.setTitle("Delete Request").setMessage("Do you really want to delete a send request?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    reqCol.document(currentUserId).collection("Requests").document(recieverUserID).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        reqCol.document(recieverUserID).collection("Requests").document(currentUserId).delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            holder.connectTv.setEnabled(true);
                                                                            currentState[0] = "new";
                                                                            holder.connectTv.setText("Connect");
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }

                        if (currentState[0].equals("request_recieved")) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                            alertDialog.setTitle("Accept Request").setMessage("Do you really want to accept a connect request?").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    userCol.document(recieverUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                                    usermap.put("key",currentUserId);
                                                    if(userimage !=null && !TextUtils.isEmpty(userimage)){
                                                        usermap.put("image",userimage);
                                                    }

                                                    userCol.document(recieverUserID).collection("Friends").document(currentUserId)
                                                            .set(usermap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        userCol.document(currentUserId).collection("Friends").document(recieverUserID)
                                                                                .set(map)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            reqCol.document(currentUserId).collection("Requests").document(recieverUserID).delete()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                reqCol.document(recieverUserID).collection("Requests").document(currentUserId).delete()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                                                                                                    chatNotificationMap.put("from", currentUserId);
                                                                                                                                    chatNotificationMap.put("type", "accept");
                                                                                                                                    notiRef.child(recieverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                            if (task.isSuccessful()) {
                                                                                                                                                holder.connectTv.setEnabled(true);
                                                                                                                                                currentState[0] = "friends";
                                                                                                                                                holder.connectTv.setText("Message");
                                                                                                                                            } else {
                                                                                                                                                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });

                                                                                                                                } else {
                                                                                                                                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            } else {
                                                                                                                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        } else {
                                                                                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });

                                                }
                                            });

                                        }
                                    });
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }

                        if (currentState[0].equals("friends")) {
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("visit_user_id", recieverUserID);
                            startActivity(intent);
                        }
                    }
                });
            }


            @NonNull
            @Override
            public ConnectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_user,
                        parent, false);
                return new ConnectViewHolder(v);
            }


            @Override
            protected void onError(@NonNull Exception e) {
                super.onError(e);
                Log.e("MainActivity", e.getMessage());
            }


            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                super.onLoadingStateChanged(state);
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        mSwipeRefreshLayout.setRefreshing(true);
                        break;

                    case LOADED:
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;

                    case ERROR:
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;

                    case FINISHED:
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;

                }
            }
        };

        androidRv.setAdapter(mAdapter);
        mAdapter.startListening();

    }

//    class QualityAdapter extends RecyclerView.Adapter<QualityAdapter.ViewHolder>{
//
//        ArrayList<ConnectMentor> list;
//
//        public QualityAdapter(ArrayList<ConnectMentor> list){
//            this.list = list;
//        }
//
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_user,parent,false);
//            return new ViewHolder(v);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//
//            ConnectMentor model = list.get(position);
//            final String currentMentor = model.getKey();
//
//            holder.itemView.setVisibility(View.VISIBLE);
//            Picasso.get().load(model.getImage()).placeholder(R.drawable.user_profile_image).into(holder.profileImage);
//            holder.userName.setText(model.getName());
//            holder.branchTv.setText(model.getBranch());
//            holder.userStatus.setText(model.getQuality());
//
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getActivity(),MentorProfileActivity.class);
//                    intent.putExtra("mentor_id",currentMentor);
//                    startActivity(intent);
//                }
//            });
//
//            String recieverUserID = model.getKey();
//            final String[] currentState = {"new"};
//            if (!recieverUserID.equals(currentUserId)){
//                holder.connectTv.setVisibility(View.VISIBLE);
//            } else{
//                holder.connectTv.setVisibility(View.GONE);
//            }
//
//            reqCol.document(currentUserId).collection("Requests").document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
//                    if(snapshot.exists()){
//                        String requestType = snapshot.get("request_type").toString();
//                        if (requestType.equals("sent")) {
//                            currentState[0] = "request_sent";
//                            holder.connectTv.setText("Request Sent");
//                            holder.connectTv.setEnabled(true);
//
//                        } else {
//                            currentState[0] = "request_recieved";
//                            holder.connectTv.setText("Confirm");
//                            holder.connectTv.setEnabled(true);
//                        }
//                    }
//                }
//            });
//
//            userCol.document(currentUserId).collection("Friends").document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
//                    if(snapshot.exists()){
//                        currentState[0] = "friends";
//                        holder.connectTv.setText("Friends");
//                        holder.connectTv.setEnabled(true);
//                    }
//                }
//            });
//
//            holder.connectTv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (currentState[0].equals("new")) {
//                        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
//                        alertDialog.setTitle("Send Request").setMessage("Do you really want to send a connect request?").setPositiveButton("SEND", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                HashMap<String,String> map = new HashMap<>();
//                                map.put("request_type","sent");
//
//                                HashMap<String,String> map2 = new HashMap<>();
//                                map2.put("request_type","recieved");
//
//                                reqCol.document(currentUserId).collection("Requests").document(recieverUserID)
//                                        .set(map)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//                                                    reqCol.document(recieverUserID).collection("Requests").document(currentUserId).set(map2)
//                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                @Override
//                                                                public void onComplete(@NonNull Task<Void> task) {
//                                                                    if (task.isSuccessful()) {
//                                                                        HashMap<String, String> chatNotificationMap = new HashMap<>();
//                                                                        chatNotificationMap.put("from", currentUserId);
//                                                                        chatNotificationMap.put("type", "request");
//                                                                        notiRef.child(recieverUserID).push()
//                                                                                .setValue(chatNotificationMap)
//                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                    @Override
//                                                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                                                        if (task.isSuccessful()) {
//                                                                                            holder.connectTv.setEnabled(true);
//                                                                                            currentState[0] = "request_sent";
//                                                                                            holder.connectTv.setText("Cancel Request");
//                                                                                        }
//                                                                                    }
//                                                                                });
//                                                                    }
//                                                                }
//                                                            });
//                                                }
//                                            }
//                                        });
//                            }
//                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        }).show();
//                    }
//
//                    if (currentState[0].equals("request_sent")) {
//                        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
//                        alertDialog.setTitle("Delete Request").setMessage("Do you really want to delete a send request?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                reqCol.document(currentUserId).collection("Requests").document(recieverUserID).delete()
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//                                                    reqCol.document(recieverUserID).collection("Requests").document(currentUserId).delete()
//                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                @Override
//                                                                public void onComplete(@NonNull Task<Void> task) {
//                                                                    if (task.isSuccessful()) {
//                                                                        holder.connectTv.setEnabled(true);
//                                                                        currentState[0] = "new";
//                                                                        holder.connectTv.setText("Connect");
//                                                                    }
//                                                                }
//                                                            });
//                                                }
//                                            }
//                                        });
//                            }
//                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        }).show();
//                    }
//
//                    if (currentState[0].equals("request_recieved")) {
//                        androidx.appcompat.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
//                        alertDialog.setTitle("Accept Request").setMessage("Do you really want to accept a connect request?").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                userCol.document(recieverUserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot snapshot1) {
//                                        String requestImage = null;
//                                        if (snapshot1.contains("image")){
//                                            requestImage = snapshot1.get("image").toString();
//                                            Picasso.get().load(requestImage).into(holder.profileImage);
//                                        }
//
//                                        final String requestName = snapshot1.get("name").toString();
//                                        final String quality = snapshot1.get("quality").toString();
//
//                                        String finalRequestImage = requestImage;
//                                        userCol.document(currentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onSuccess(DocumentSnapshot snapshot2) {
//
//                                                String userimage = null;
//                                                if (snapshot2.contains("image")){
//                                                    userimage = snapshot2.get("image").toString();
//                                                    Picasso.get().load(userimage).into(holder.profileImage);
//                                                }
//
//                                                final String username = snapshot2.get("name").toString();
//                                                final String userquality = snapshot2.get("quality").toString();
//
//
//                                                HashMap<String,String> map = new HashMap<>();
//                                                map.put("name",requestName);
//                                                map.put("quality",quality);
//                                                map.put("key",recieverUserID);
//                                                if(finalRequestImage !=null && !TextUtils.isEmpty(finalRequestImage)){
//                                                    map.put("image", finalRequestImage);
//                                                }
//
//                                                HashMap<String,String> usermap = new HashMap<>();
//                                                usermap.put("name",username);
//                                                usermap.put("quality",userquality);
//                                                usermap.put("key",currentUserId);
//                                                if(userimage !=null && !TextUtils.isEmpty(userimage)){
//                                                    usermap.put("image",userimage);
//                                                }
//
//                                                userCol.document(recieverUserID).collection("Friends").document(currentUserId)
//                                                        .set(usermap)
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                if (task.isSuccessful()) {
//
//                                                                    userCol.document(currentUserId).collection("Friends").document(recieverUserID)
//                                                                            .set(map)
//                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                @Override
//                                                                                public void onComplete(@NonNull Task<Void> task) {
//                                                                                    if (task.isSuccessful()) {
//                                                                                        reqCol.document(currentUserId).collection("Requests").document(recieverUserID).delete()
//                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                                    @Override
//                                                                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                                                                        if (task.isSuccessful()) {
//                                                                                                            reqCol.document(recieverUserID).collection("Requests").document(currentUserId).delete()
//                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                                                        @Override
//                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                                                                                            if (task.isSuccessful()) {
//                                                                                                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
//                                                                                                                                chatNotificationMap.put("from", currentUserId);
//                                                                                                                                chatNotificationMap.put("type", "accept");
//                                                                                                                                notiRef.child(recieverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                                                                    @Override
//                                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                                                                                                        if (task.isSuccessful()) {
//                                                                                                                                            holder.connectTv.setEnabled(true);
//                                                                                                                                            currentState[0] = "friends";
//                                                                                                                                            holder.connectTv.setText("Message");
//                                                                                                                                        } else {
//                                                                                                                                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
//                                                                                                                                        }
//                                                                                                                                    }
//                                                                                                                                });
//
//                                                                                                                            } else {
//                                                                                                                                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
//                                                                                                                            }
//                                                                                                                        }
//                                                                                                                    });
//                                                                                                        } else {
//                                                                                                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
//                                                                                                        }
//                                                                                                    }
//                                                                                                });
//                                                                                    } else {
//                                                                                        Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
//                                                                                    }
//                                                                                }
//                                                                            });
//                                                                } else {
//                                                                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
//                                                                }
//                                                            }
//                                                        });
//
//                                            }
//                                        });
//
//                                    }
//                                });
//                            }
//                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        }).show();
//                    }
//
//                    if (currentState[0].equals("friends")) {
//                        Intent intent = new Intent(getActivity(), ChatActivity.class);
//                        intent.putExtra("visit_user_id", recieverUserID);
//                        startActivity(intent);
//                    }
//                }
//            });
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return list.size();
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//
//            TextView userName, userStatus;
//            CircleImageView profileImage;
//            Button connectTv;
//            TextView branchTv;
//
//            public ViewHolder(@NonNull View itemView) {
//                super(itemView);
//
//                userName = itemView.findViewById(R.id.user_profile_name);
//                userStatus = itemView.findViewById(R.id.user_status);
//                profileImage = itemView.findViewById(R.id.users_profile_image);
//                connectTv = itemView.findViewById(R.id.connectBt);
//                branchTv = itemView.findViewById(R.id.branchTv);
//            }
//        }
//    }

    public class ConnectViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button connectTv;
        TextView branchTv;

        public ConnectViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            connectTv = itemView.findViewById(R.id.connectBt);
            branchTv = itemView.findViewById(R.id.branchTv);

        }
    }

}
