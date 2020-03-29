package com.kraigs.android.micamp.Home.Updates;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.extras.SharedPrefs;

public class UpdatesActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UpdateAdapter adapterStudent,adapterNews,adapterDownload,adapterScholar;
    private RecyclerView recyclerUpdateStudent;
    LinearLayoutManager llm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);
        getSupportActionBar().setTitle("Downloads");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerUpdateStudent = findViewById(R.id.updateRecyclerView);
        recyclerUpdateStudent.setHasFixedSize(true);
        llm  = new LinearLayoutManager(this);
        recyclerUpdateStudent.setLayoutManager(llm);

        studentList(recyclerUpdateStudent);
        newsList(recyclerUpdateStudent);
        downloadList(recyclerUpdateStudent);
        scholarList(recyclerUpdateStudent);

    }

    public void studentList(View view) {
        Query query = db.collection(SharedPrefs.getCollege()).document("Downloads").collection("Student").orderBy("timestamp");
        FirestoreRecyclerOptions<Update> options = new FirestoreRecyclerOptions.Builder<Update>()
                .setQuery(query,Update.class)
                .build();
        adapterStudent = new UpdateAdapter(options,getApplicationContext());
        adapterStudent.notifyDataSetChanged();
        recyclerUpdateStudent.setAdapter(adapterStudent);
        llm.smoothScrollToPosition(recyclerUpdateStudent,null,0);
        adapterStudent.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerUpdateStudent.smoothScrollToPosition(positionStart+1);
                llm.setReverseLayout(true);
                llm.setStackFromEnd(true);
            }
        });
        adapterStudent.startListening();

    }

    public void newsList(View view) {
//        setUpRecyclerAdapter("News");
        Query query = db.collection(SharedPrefs.getCollege()).document("Downloads").collection("News").orderBy("timestamp");
        FirestoreRecyclerOptions<Update> options = new FirestoreRecyclerOptions.Builder<Update>()
                .setQuery(query,Update.class)
                .build();
        adapterNews = new UpdateAdapter(options,getApplicationContext());
        adapterNews.notifyDataSetChanged();
        recyclerUpdateStudent.setAdapter(adapterNews);
        llm.smoothScrollToPosition(recyclerUpdateStudent,null,0);
        adapterNews.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerUpdateStudent.smoothScrollToPosition(positionStart+1);
                llm.setReverseLayout(true);
                llm.setStackFromEnd(true);
            }
        });
        adapterNews.startListening();

    }

    public void downloadList(View view) {
        Query query = db.collection(SharedPrefs.getCollege()).document("Downloads").collection("Download").orderBy("timestamp");
        FirestoreRecyclerOptions<Update> options = new FirestoreRecyclerOptions.Builder<Update>()
                .setQuery(query,Update.class)
                .build();
        adapterDownload = new UpdateAdapter(options,getApplicationContext());
        adapterDownload.notifyDataSetChanged();
        recyclerUpdateStudent.setAdapter(adapterDownload);
        llm.smoothScrollToPosition(recyclerUpdateStudent,null,0);
        adapterDownload.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerUpdateStudent.smoothScrollToPosition(positionStart+1);
                llm.setReverseLayout(true);
                llm.setStackFromEnd(true);
            }
        });
        adapterDownload.startListening();
    }

    public void scholarList(View view) {
        Query query = db.collection(SharedPrefs.getCollege()).document("Downloads").collection("Scholarship").orderBy("timestamp");
        FirestoreRecyclerOptions<Update> options = new FirestoreRecyclerOptions.Builder<Update>()
                .setQuery(query,Update.class)
                .build();
        adapterScholar = new UpdateAdapter(options,getApplicationContext());
        adapterScholar.notifyDataSetChanged();
        recyclerUpdateStudent.setAdapter(adapterScholar);
        llm.smoothScrollToPosition(recyclerUpdateStudent,null,0);
        adapterScholar.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerUpdateStudent.smoothScrollToPosition(positionStart+1);
                llm.setReverseLayout(true);
                llm.setStackFromEnd(true);
            }
        });
        adapterScholar.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();

        studentList(recyclerUpdateStudent);
        adapterStudent.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterStudent.stopListening();
        adapterNews.stopListening();
        adapterDownload.stopListening();
        adapterScholar.stopListening();
    }

}
