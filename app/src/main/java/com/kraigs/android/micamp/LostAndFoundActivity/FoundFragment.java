package com.kraigs.android.micamp.LostAndFoundActivity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.Chat.ChatActivity;
import com.kraigs.android.micamp.Mentor.MentorProfileActivity;
import com.kraigs.android.micamp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class FoundFragment extends Fragment {

    View v;
    RecyclerView foundRv;
    CollectionReference deleteRef;
    String userName,userImage;
    FirebaseAuth mAuth;
    String currentUserId;
    FloatingActionButton foundFb;
    CollectionReference userCol;

    public FoundFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_found, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        foundRv = v.findViewById(R.id.foundRv);
        foundFb = v.findViewById(R.id.foundFb);
        foundRv.setLayoutManager(new GridLayoutManager(getActivity(),2));

        foundFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),AddLostFoundActivity.class);
                intent.putExtra("missing_type","Found");
                startActivity(intent);
            }
        });

        userCol = FirebaseFirestore.getInstance().collection("User");
        deleteRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("LostFound").collection("Found");


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = deleteRef.orderBy("timestamp");

        FirestoreRecyclerOptions<LostFound> options = new FirestoreRecyclerOptions.Builder<LostFound>()
                .setQuery(query,LostFound.class)
                .build();
        FirestoreRecyclerAdapter<LostFound,LostHolder> adapter = new FirestoreRecyclerAdapter<LostFound, LostHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LostHolder lostHolder, int i, @NonNull LostFound model) {
                Picasso.get().load(model.getImage()).into(lostHolder.image);
                lostHolder.item.setText(model.getName());
                lostHolder.place.setText(model.getPlace());
                String currentItem = getSnapshots().getSnapshot(i).getId();

                if (currentUserId.equals(model.getUid())){
                    lostHolder.delete.setVisibility(View.VISIBLE);

                    lostHolder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Delete").setMessage("Do you want to delete this item?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    deleteRef.document(currentItem).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                notifyItemRemoved(i);
                                                notifyDataSetChanged();

                                                Toast.makeText(getActivity(), "Item Removed Successfully!", Toast.LENGTH_SHORT).show();

                                            } else{
                                                String message = task.getException().toString();
                                                Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
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
                    });
                } else{
                    lostHolder.delete.setVisibility(View.GONE);
                }

                lostHolder.chat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        userCol.document(model.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                                if(dataSnapshot.exists()){

                                    userName = dataSnapshot.get("name").toString();
                                    if (dataSnapshot.contains("image")){
                                        userImage = dataSnapshot.get("image").toString();
                                    } else{
                                        userImage = "default_image";
                                    }

                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("visit_user_id",model.getUid());
                                    intent.putExtra("visit_user_image",userImage);
                                    intent.putExtra("visit_user_name",userName);
                                    startActivity(intent);

                                }
                            }
                        });

                    }
                });

                lostHolder.profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), MentorProfileActivity.class);
                        intent.putExtra("mentor_id",model.getUid());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public LostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_lost_item,parent,false);
                return new LostHolder(v);
            }
        };

        foundRv.setAdapter(adapter);
        adapter.startListening();
    }

    private class LostHolder extends RecyclerView.ViewHolder {
        ImageView image,delete;
        TextView item,place;
        Button chat,profile;
        public LostHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.lost_image);
            item = itemView.findViewById(R.id.lost_name);
            place = itemView.findViewById(R.id.lost_place);
            profile = itemView.findViewById(R.id.lostProfileUserBt);
            delete = itemView.findViewById(R.id.deleteLost);
            chat = itemView.findViewById(R.id.lostChatUserBt);
        }
    }

}
