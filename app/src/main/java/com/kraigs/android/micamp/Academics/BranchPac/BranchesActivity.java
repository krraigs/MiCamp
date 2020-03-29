package com.kraigs.android.micamp.Academics.BranchPac;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

import com.kraigs.android.micamp.Academics.Member;
import com.kraigs.android.micamp.Home.Nit;
import com.kraigs.android.micamp.Home.NitAdapter;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.kraigs.android.micamp.extras.WebViewClass;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class BranchesActivity extends AppCompatActivity {

    private static String branch;
    private CardView websiteCv;

    private NitAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView seeAllLabs;

    private RecyclerView labrv;
    private CollectionReference labref;
    String webUrl;
    ImageView seeAllArrowLabs;
    final Query[] queryCards = new Query[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branches);

        branch = getIntent().getStringExtra("branch");

        getSupportActionBar().setTitle(branch);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        branch = getIntent().getStringExtra("branch");
        webUrl = getIntent().getStringExtra("webUrl");


        seeAllArrowLabs = findViewById(R.id.seeAllArrow);


        seeAllLabs = findViewById(R.id.seeAllLabs);
        websiteCv = findViewById(R.id.branch_website_card);
//        placementCv = findViewById(R.id.branch_placement_card);

        if (webUrl == null){
            websiteCv.setVisibility(View.GONE);
        }

        websiteCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent inent = new Intent(BranchesActivity.this, WebViewClass.class);
                inent.putExtra("webUrl",webUrl);
                startActivity(inent);

            }
        });

        seeAllLabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BranchesActivity.this,LaboratoriesActivity.class);
                intent.putExtra("branch",branch);
                startActivity(intent);
            }
        });

        labrv = (RecyclerView) findViewById(R.id.labsBranchRv);
        labrv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        labrv.setHasFixedSize(true);
        branch = getIntent().getStringExtra("branch");
        labref = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Academics").collection("Branch").document(branch)
                .collection("labs");

        DocumentReference docRef = db.collection(SharedPrefs.getCollege()).document("Academics").collection("Branch").document(branch);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    if (!documentSnapshot.contains("cards")){
                        setUpRecyclerAdapterCards(db.collection(SharedPrefs.getCollege()).document("Academics").collection("Branch").document(branch).collection("cards").orderBy("timestamp"));
//                        queryCards[0] = db.collection("Nit").document("Academics").collection("Branch").document(branch).collection("cards").orderBy("timestamp");

                    }else{
                        setUpRecyclerAdapterCards(db.collection(SharedPrefs.getCollege()).document("Home").collection("homeCards"));

//                        queryCards[0] = db.collection("Nit").document("Home").collection("homeCards");
                    }
                }
            }
        });

        setUpRecyclerAdapterFaculty();

    }

    private void setUpRecyclerAdapterCards(Query query) {
        FirestoreRecyclerOptions<Nit> options = new FirestoreRecyclerOptions.Builder<Nit>()
                .setQuery(query, Nit.class)
                .build();
        adapter = new NitAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.branch_recycler_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void setUpRecyclerAdapterFaculty() {

        Query query = db.collection(SharedPrefs.getCollege()).document("Academics").collection("Branch").document(branch).collection("Faculty").orderBy("timestamp");
        FirestoreRecyclerOptions<Member> options = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query,Member.class)
                .build();

        FirestoreRecyclerAdapter<Member,FacultyHolder> adapter = new FirestoreRecyclerAdapter<Member, FacultyHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FacultyHolder holder, int position, @NonNull Member model) {
                String id = getSnapshots().getSnapshot(position).getId();
                holder.name.setText(model.getName());

                Picasso.get()
                        .load(model.getImage())
                        .placeholder(R.drawable.user_profile_image)
                        .into(holder.image);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(holder.itemView.getContext(), WebViewClass.class);
                        intent.putExtra("id",id);
                        intent.putExtra("webUrl",model.getProfileLink());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FacultyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_faculty_rv,parent,false);
                return new FacultyHolder(v);
            }
        };

        RecyclerView recyclerView = findViewById(R.id.faculty_recycler_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    public void onStart() {
        super.onStart();

        DocumentReference docRef = db.collection(SharedPrefs.getCollege()).document("Academics").collection("Branch").document(branch);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("labs")){
                        setUpRecyclerLabsImages();

                        seeAllLabs.setVisibility(View.VISIBLE);
                        seeAllArrowLabs.setVisibility(View.VISIBLE);
                        labrv.setVisibility(View.VISIBLE);

                    } else{
                        seeAllLabs.setVisibility(View.GONE);
                        seeAllArrowLabs.setVisibility(View.GONE);
                        labrv.setVisibility(View.GONE);

                    }
                }
            }
        });
    }

    private void setUpRecyclerLabsImages() {
        Query query = labref;
        FirestoreRecyclerOptions<Lab> options = new FirestoreRecyclerOptions.Builder<Lab>()
                .setQuery(query,Lab.class)
                .build();

        FirestoreRecyclerAdapter<Lab,LabViewHolder> adapter = new FirestoreRecyclerAdapter<Lab,LabViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final LabViewHolder holder, int position, @NonNull final Lab model) {

                DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                final String labID = snapshot.getId();
                holder.name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.image);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(),LabActivity.class);
                        intent.putExtra("labName",labID);
                        intent.putExtra("branch",branch);
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public LabViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_branch_labs,viewGroup,false);
                return new LabViewHolder(v);
            }
        };
        labrv.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public class LabViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        public LabViewHolder(@NonNull View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.lab_image);
            name = (TextView) itemView.findViewById(R.id.lab_name);
        }
    }

    public class FacultyHolder extends RecyclerView.ViewHolder {
        TextView name;
        CircleImageView image;
        public FacultyHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.faculty_name);
            image = (CircleImageView) itemView.findViewById(R.id.faculty_image);
        }
    }
}
