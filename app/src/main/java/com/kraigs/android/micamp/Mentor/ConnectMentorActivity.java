package com.kraigs.android.micamp.Mentor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import de.hdodenhof.circleimageview.CircleImageView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.Chat.ChatActivity;
import com.kraigs.android.micamp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class ConnectMentorActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserId;
    ArrayList<ConnectMentor> list;
    DatabaseReference rootRef,notiRef;
    RecyclerView recyclerView;
    CollectionReference userCol,reqCol;
    SwipeRefreshLayout mSwipeRefreshLayout;
    FirestorePagingAdapter<ConnectMentor,ConnectViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_mentor);
        getSupportActionBar().setTitle("Connect");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        notiRef = rootRef.child("Notifications");
        userCol = FirebaseFirestore.getInstance().collection("User");
        reqCol = userCol;

        recyclerView = findViewById(R.id.connect_mentor_rv);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.refresh();
            }
        });

        setupAdapter(userCol.orderBy("name"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

         getMenuInflater().inflate(R.menu.menu_user_search,menu);
         MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
         SearchView searchView = (SearchView)myActionMenuItem.getActionView();
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(ConnectMentorActivity.this, "Search : " + query, Toast.LENGTH_SHORT).show();

                if (!searchView.isIconified()){
                    searchView.setIconified(true);
                }

                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
         });

         return true;

    }

    private void search(String newText) {

        if (newText.length() > 0){
            Query query = userCol.orderBy("name").startAt(newText.substring(0,1).toUpperCase() + newText.substring(1)).endAt(newText.substring(0,1).toUpperCase() + newText.substring(1) + "\uf8ff");
            setupAdapter(query);
        } else{
            Query query = userCol.orderBy("name");
            setupAdapter(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

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
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
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
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
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
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectMentorActivity.this);
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
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectMentorActivity.this);
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
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectMentorActivity.this);
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
                                                                                                                                                Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });

                                                                                                                                } else {
                                                                                                                                    Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            } else {
                                                                                                                Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        } else {
                                                                                            Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
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
                            Intent intent = new Intent(ConnectMentorActivity.this, ChatActivity.class);
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
                        Toast.makeText(getApplicationContext(), "Error Occurred!", Toast.LENGTH_SHORT
                        ).show();

                        mSwipeRefreshLayout.setRefreshing(false);
                        break;

                    case FINISHED:
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;

                }
            }
        };

        recyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        userCol.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()){
//                    list = new ArrayList<>();
//
//                    for (QueryDocumentSnapshot snap : task.getResult()) {
//                        ConnectMentor model = snap.toObject(ConnectMentor.class);
//                        if (!TextUtils.isEmpty(model.getName().trim()) && !TextUtils.isEmpty(model.getBranch().trim()) && !TextUtils.isEmpty(model.getYear().trim()) && !TextUtils.isEmpty(model.getQuality().trim())) {
//                            list.add(model);
//                        }
//                    }
//
//                    Collections.sort(list, new SortBtName());
//
//                    MentorAdapter adapter = new MentorAdapter(list);
//                    recyclerView.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });
//
//    }
//
//    class MentorAdapter extends RecyclerView.Adapter<MentorAdapter.ConnectViewHolder>{
//
//        ArrayList<ConnectMentor> list;
//
//        public MentorAdapter(ArrayList<ConnectMentor> list){
//            this.list = list;
//        }
//
//        @NonNull
//        @Override
//        public ConnectViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
//            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_user,
//                    viewGroup, false);
//            return new ConnectViewHolder(v);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ConnectViewHolder holder, int position) {
//            String mentorId = list.get(position).getKey();
//            holder.userName.setText(list.get(position).getName());
//
//            holder.userStatus.setText(list.get(position).getQuality());
//
//            Picasso.get().load(list.get(position).getImage())
//                    .placeholder(R.drawable.user_profile_image)
//                    .into(holder.profileImage);
//
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(holder.itemView.getContext(), MentorProfileActivity.class);
//                    intent.putExtra("mentor_id",mentorId);
//                    holder.itemView.getContext().startActivity(intent);
//                }
//            });
//
//            ConnectMentor model = list.get(position);
//
//            String recieverUserID = model.getKey();
//            final String[] currentState = {"new"};
//            if (!recieverUserID.equals(currentUserId)){
//                holder.connectTv.setVisibility(View.VISIBLE);
//            } else{
//                holder.connectTv.setVisibility(View.GONE);
//            }
//
//            holder.branchTv.setText(model.getYear() + " " + model.getBranch());
//
//            reqCol.document(currentUserId).collection("Requests").document(recieverUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
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
//                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
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
//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectMentorActivity.this);
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
//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectMentorActivity.this);
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
//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectMentorActivity.this);
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
//                                                                                                                                            Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                                                                                                                        }
//                                                                                                                                    }
//                                                                                                                                });
//
//                                                                                                                            } else {
//                                                                                                                                Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                                                                                                            }
//                                                                                                                        }
//                                                                                                                    });
//                                                                                                        } else {
//                                                                                                            Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                                                                                        }
//                                                                                                    }
//                                                                                                });
//                                                                                    } else {
//                                                                                        Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                                                                                    }
//                                                                                }
//                                                                            });
//                                                                } else {
//                                                                    Toast.makeText(ConnectMentorActivity.this, "Error", Toast.LENGTH_SHORT).show();
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
//                        Intent intent = new Intent(ConnectMentorActivity.this, ChatActivity.class);
//                        intent.putExtra("visit_user_id", recieverUserID);
//                        startActivity(intent);
//                    }
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return list.size();
//        }
//
//
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

//    class SortBtName implements Comparator<ConnectMentor>{
//        public int compare(ConnectMentor a, ConnectMentor b){
//            return a.getName().compareTo(b.getName());
//        }
//    }

}
