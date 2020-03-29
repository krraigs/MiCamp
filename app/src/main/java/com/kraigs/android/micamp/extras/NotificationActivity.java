package com.kraigs.android.micamp.extras;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView notiRv;
    DatabaseReference notiRef;
    String currentUserId;
    CollectionReference userRef;
    LinearLayoutManager linearLayoutManager;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        type = getIntent().getStringExtra("type");

        notiRv = findViewById(R.id.notiRv);
        linearLayoutManager = new LinearLayoutManager(this);
        notiRv.setLayoutManager(linearLayoutManager);

        if (type.equals("main")){
            notiRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(currentUserId);
        } else if (type.equals("hostel")){

            notiRef = FirebaseDatabase.getInstance().getReference().child("NotificationsHostel").child(currentUserId);
        } else{
            notiRef = FirebaseDatabase.getInstance().getReference().child("NotificationsUserOrder").child(currentUserId);
        }

        userRef = FirebaseFirestore.getInstance().collection("User");

        RelativeLayout emptyRl = findViewById(R.id.emptyRl);

        notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){

                    emptyRl.setVisibility(View.VISIBLE);
                    notiRv.setVisibility(View.GONE);

                } else {
                    emptyRl.setVisibility(View.GONE);
                    notiRv.setVisibility(View.VISIBLE);
                    setUpRv();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpRv() {

        Query query = notiRef;


        FirebaseRecyclerOptions<Notification> options = new FirebaseRecyclerOptions.Builder<Notification>()
                .setQuery(query,Notification.class)
                .build();

        FirebaseRecyclerAdapter<Notification,NotiHolder> adapter = new FirebaseRecyclerAdapter<Notification, NotiHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotiHolder holder, int i, @NonNull Notification notification) {

                String from = notification.getFrom();
                if (type.equals("main")){
                    holder.star.setVisibility(View.GONE);
                    holder.userPic.setVisibility(View.VISIBLE);

                    userRef.document(from).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                            if (snapshot.exists()){

                                String name = snapshot.get("name").toString();

                                if (snapshot.contains("image")){
                                    String image = snapshot.get("image").toString();
                                    Picasso.get().load(image).placeholder(R.drawable.user_profile_image).into(holder.userPic);
                                }

                                if (notification.getType().equals("request")){
                                    holder.notiTv.setText("You have a new connect request from " + name);
                                } else if (notification.getType().equals("accept")){
                                    holder.notiTv.setText(name + " has accpted your connect request!");
                                }

                            }
                        }
                    });
                } else if (type.equals("snackers")){
                    holder.star.setVisibility(View.VISIBLE);
                    holder.userPic.setVisibility(View.GONE);

                    if (notification.getType().equals("Cancel")){
                        holder.notiTv.setText("Your order has been cancelled due to some resons.");
                    } else if (notification.getType().equals("Prepared")){

                        holder.notiTv.setText("Collect your order before it gets cold. OTP : " + notification.getOtp());

                    }else if (notification.getType().equals("Delivered")){
                        holder.notiTv.setText("Great! Your order has been delivered. Enjoy!");
                    }else if (notification.getType().equals("Preparing")){

                        holder.notiTv.setText("Great! We are preparing your order. Enjoy!");
                    }

                } else if(type.equals("hostel")){
                    holder.star.setVisibility(View.VISIBLE);
                    holder.userPic.setVisibility(View.GONE);

                    if (notification.getType().equals("verify")){
                        holder.notiTv.setText("Great! Your details are correctly verified! Enjoy the hostel features.");
                    } else if (notification.getType().equals("notVerified")){

                        holder.notiTv.setText("Uff! Your details did not match with hostel details correctly. Please try again.");

                    }else if (notification.getType().equals("messOffCancelEarly")){
                        holder.notiTv.setText("Your request for mess off has been declined. Please try again.");
                    }else if (notification.getType().equals("messOffCancelLater")){

                        holder.notiTv.setText("Your request for mess off has been declined due to unavaoidable circumstances.");
                    }else if (notification.getType().equals("messDone")){

                        holder.notiTv.setText("You request for mess off has been approved. Enjoy!");
                    }else if (notification.getType().equals("leaveDone")){

                        holder.notiTv.setText("Great! You request for gate pass has been approved!");
                    }

                }

            }

            @NonNull
            @Override
            public NotiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_noti,parent,false);
                return  new NotiHolder(v);
            }
        };

        notiRv.setAdapter(adapter);
        adapter.startListening();
        linearLayoutManager.smoothScrollToPosition(notiRv, null, 0);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                notiRv.smoothScrollToPosition(positionStart + 1);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
            }
        });
    }

    private class NotiHolder extends RecyclerView.ViewHolder {

        TextView notiTv;
        CircleImageView userPic;
        ImageView star;

        public NotiHolder(@NonNull View itemView) {
            super(itemView);

            notiTv = itemView.findViewById(R.id.notiTv);
            userPic = itemView.findViewById(R.id.userPic);
            star = itemView.findViewById(R.id.star);

        }
    }
}
