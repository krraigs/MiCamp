package com.kraigs.android.micamp.Academics.BranchPac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

public class LaboratoriesActivity extends AppCompatActivity {

    private RecyclerView labrv;
    static String branch;
    private CollectionReference labref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laboratories);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        branch = getIntent().getStringExtra("branch");

        getSupportActionBar().setTitle(branch);

        labrv = (RecyclerView) findViewById(R.id.labRv);
        labrv.setLayoutManager(new GridLayoutManager(this,2));
        labrv.setHasFixedSize(true);
        labref = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Academics").collection("Branch").document(branch)
                .collection("labs");

    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = labref;
        FirestoreRecyclerOptions<Lab> options = new FirestoreRecyclerOptions.Builder<Lab>()
                .setQuery(query,Lab.class)
                .build();
        FirestoreRecyclerAdapter<Lab,LabViewHolder> adapter = new FirestoreRecyclerAdapter<Lab,LabViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final LabViewHolder holder, int position, @NonNull final Lab model) {

                DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                final String labName = snapshot.getId();
                holder.name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.image);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(),LabActivity.class);
                        intent.putExtra("labName",labName);
                        intent.putExtra("branch",branch);
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public LabViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_laboratories,viewGroup,false);
                return new LabViewHolder(v);
            }
        };
        labrv.setAdapter(adapter);
        adapter.startListening();
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
}
