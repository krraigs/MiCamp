package com.kraigs.android.micamp.Home.News;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;

public class NewsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView newsRv;
    LinearLayoutManager llm;
    SwipeRefreshLayout mSwipeRefreshLayout;
    FirestorePagingAdapter<News,NewsHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        getSupportActionBar().setTitle("Views");

        newsRv = findViewById(R.id.newsRv);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        newsRv.setHasFixedSize(true);
        llm  = new LinearLayoutManager(this);
        newsRv.setLayoutManager(llm);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.addNewsFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewsActivity.this,AddNewsActivity.class);
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.refresh();
            }
        });

        setupAdapter();

    }


    private void setupAdapter() {

        // Init Paging Configuration
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(2)
                .setPageSize(15)
                .build();

        Query colRef = db.collection(SharedPrefs.getCollege()).document("Home").collection("News").orderBy("timestamp", Query.Direction.DESCENDING);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<News>()
                .setLifecycleOwner(this)
                .setQuery(colRef, config, News.class)
                .build();

        mAdapter = new FirestorePagingAdapter<News, NewsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NewsHolder holder, int i, @NonNull News model) {
                holder.topic.setText(model.getTopic());
                holder.content.setText(model.getContent());
            }

            @NonNull
            @Override
            public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_news,parent,false);
                return new NewsHolder(v);
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

        newsRv.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    public class NewsHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView topic;
        TextView content;

        public NewsHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.newsDate);
            topic = itemView.findViewById(R.id.newsTopic);
            content = itemView.findViewById(R.id.newsContent);

        }
    }
}
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        Query colRef = db.collection("Nit").document("Home").collection("News").orderBy("timestamp", Query.Direction.DESCENDING).limit(30);
//
//        FirestoreRecyclerOptions<News> options = new FirestoreRecyclerOptions.Builder<News>()
//                .setQuery(colRef,News.class)
//                .build();
//
//
//        adapter = new FirestoreRecyclerAdapter<News, NewsHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull NewsHolder holder, int position, @NonNull News model) {
//                holder.topic.setText(model.getTopic());
//                holder.content.setText(model.getContent());
//
//            }
//
//            @NonNull
//            @Override
//            public NewsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_news,viewGroup,false);
//                return new NewsHolder(v);
//            }
//        };
//
//        adapter.notifyDataSetChanged();
//        newsRv.setAdapter(adapter);
//        adapter.startListening();
//    }
