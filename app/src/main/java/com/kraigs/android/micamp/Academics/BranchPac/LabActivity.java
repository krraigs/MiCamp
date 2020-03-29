package com.kraigs.android.micamp.Academics.BranchPac;

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

import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.kraigs.android.micamp.extras.UserTask;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class LabActivity extends AppCompatActivity {

    String labName,branch;
    private TextView labtv,labInfoTv;
    private ImageView labImage;
    private LabAdapter adapter;
    private DocumentReference labRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab);
        getSupportActionBar().setTitle("Laboratories");

        labName = getIntent().getStringExtra("labName");
        branch = getIntent().getStringExtra("branch");

        labtv = (TextView) findViewById(R.id.labNameTv);
        labInfoTv = (TextView) findViewById(R.id.labInfo);
        labImage = (ImageView) findViewById(R.id.labImage);

        setUpRecyclerView();
        
//        DatabaseReference extraRef = FirebaseDatabase.getInstance().getReference().child("appExtras").child("labs").child(branch);
//        extraRef.keepSynced(true);
//        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        extraRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    if (dataSnapshot.hasChild("id")){
//                        String id = dataSnapshot.child("id").getValue().toString();
//
//                        if (dataSnapshot.hasChild("id2")){
//                            String id2 = dataSnapshot.child("id2").getValue().toString();
//                            if (currentUserID.equals(id) || currentUserID.equals(id2)){
//                                addDataLl.setVisibility(View.VISIBLE);
//                                detailBt.setEnabled(true);
//                                equpimentBt.setEnabled(true);
//                                addDataLl.setEnabled(true);
//
//                            } else{
//                                addDataLl.setVisibility(View.GONE);
//                                detailBt.setEnabled(false);
//                                equpimentBt.setEnabled(false);
//                                addDataLl.setEnabled(false);
//
//                            }
//                        } else{
//                            if (currentUserID.equals(id)){
//                                addDataLl.setVisibility(View.VISIBLE);
//                                detailBt.setEnabled(true);
//                                equpimentBt.setEnabled(true);
//                                addDataLl.setEnabled(true);
//
//                            } else{
//                                addDataLl.setVisibility(View.GONE);
//                                detailBt.setEnabled(false);
//                                equpimentBt.setEnabled(false);
//                                addDataLl.setEnabled(false);
//
//                            }
//                        }
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//        detailBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(LabActivity.this, AddClubMember.class);
//                intent.putExtra("type","detail");
//                intent.putExtra("labName",labName);
//                intent.putExtra("branch",branch);
//                startActivity(intent);
//            }
//        });
//
//        equpimentBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(LabActivity.this, AddClubMember.class);
//                intent.putExtra("type","labEquip");
//                intent.putExtra("labName",labName);
//                intent.putExtra("branch",branch);
//                startActivity(intent);
//            }
//        });

        labRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Academics").collection("Branch")
                .document(branch).collection("labs").document(labName);
        labRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.get("name").toString();
                labtv.setText(name);

                if (documentSnapshot.contains("labInfo")){
                    String labInfo = documentSnapshot.get("labInfo").toString();
                    labInfoTv.setText(labInfo);
                }
                if (documentSnapshot.contains("image")){

                    String image = documentSnapshot.get("image").toString();
                    Picasso.get().load(image).into(labImage);

                }
            }
        });

    }

    private void setUpRecyclerView() {
        Query query = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Academics").collection("Branch").document(branch)
                .collection("labs").document(labName).collection("equipments");
        FirestoreRecyclerOptions<UserTask> options = new FirestoreRecyclerOptions.Builder<UserTask>()
                .setQuery(query,UserTask.class)
                .build();

        adapter = new LabAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.labEqpRv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public class LabAdapter extends FirestoreRecyclerAdapter<UserTask, LabAdapter.LabHolder> {

        public LabAdapter(@NonNull FirestoreRecyclerOptions<UserTask> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull LabHolder holder, int position, @NonNull final UserTask model) {
            holder.taskName.setText(model.getTaskName());
        }

        @Override
        public int getItemCount() {
            return super.getItemCount();
        }

        @NonNull
        @Override
        public LabHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile_tasks_list,
                    parent, false);
            return new LabHolder(v);
        }

        class LabHolder extends RecyclerView.ViewHolder {
            TextView taskName;

            public LabHolder(@NonNull View itemView) {
                super(itemView);
                taskName = itemView.findViewById(R.id.user_profile_task_name);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LabActivity.this, BranchesActivity.class);
        intent.putExtra("branch",branch);
        startActivity(intent);
        finish();
    }
}
