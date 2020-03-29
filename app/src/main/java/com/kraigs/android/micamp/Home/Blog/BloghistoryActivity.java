package com.kraigs.android.micamp.Home.Blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class BloghistoryActivity extends AppCompatActivity {

    private static final String TAG = "BlogActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference myRef;
    private RecyclerView recyclerView;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloghistory);
        getSupportActionBar().setTitle("Your Blogs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.blogHistoryRv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = db.collection("Public").document("Collections").collection("Blog").orderBy("timestamp");
        myRef = FirebaseFirestore.getInstance().collection("Public").document("Collections").collection("Blog");

        FirestoreRecyclerOptions<Blog> options = new FirestoreRecyclerOptions.Builder<Blog>()
                .setQuery(query, Blog.class)
                .build();

        FirestoreRecyclerAdapter<Blog, BlogHolder> adapter = new FirestoreRecyclerAdapter<Blog, BlogHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlogHolder holder, int position, @NonNull Blog model) {
                if (currentUserID.equals(model.getUid())) {

                    holder.deleteBlog.setVisibility(View.VISIBLE);
                    holder.editBlog.setVisibility(View.VISIBLE);

                    final String post_key = getSnapshots().getSnapshot(position).getId();

                    holder.editBlog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(BloghistoryActivity.this,AddBlogActivity.class);
                            intent.putExtra("type","edit");
                            intent.putExtra("key",post_key);
                            startActivity(intent);
                        }
                    });

                    holder.deleteBlog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BloghistoryActivity.this);
                            alertDialog.setTitle("Delete").setMessage("Do you want to delete this blog?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    myRef.document(post_key).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(BloghistoryActivity.this, "Removed successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                String message = task.getException().toString();
                                                Toast.makeText(BloghistoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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

                    myRef.document(post_key).collection("readBy").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", task.getResult().size() + "");
                                holder.viewsTv.setText(task.getResult().size() + " views");

                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });

                    if(model.getmPhotoUrl()!=null){
                        Picasso.get()
                                .load(model.getmPhotoUrl())
                                .into(holder.photoImageView);

                        holder.authorTextView.setText("By " + model.getmAuthor());

                        String updateText = model.getmInfo();

                        int maxLength2 = (updateText.length() < 85) ? updateText.length() : 85;
                        updateText = updateText.substring(0, maxLength2) + "...";

                        holder.subTitleTextView.setText(updateText);

                    } else{
                        holder.photoImageView.setVisibility(View.GONE);
                        holder.authorTextView.setVisibility(View.GONE);

                        String updateText = model.getmInfo();

                        int maxLength2 = (updateText.length() < 75) ? updateText.length() : 75;
                        updateText = updateText.substring(0, maxLength2) + "... By " + model.getmAuthor();

                        holder.subTitleTextView.setText(updateText);

                    }

                    holder.titleTextView.setText(model.getmTitle());

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent detailActivity = new Intent(BloghistoryActivity.this, BlogDetailActivity.class);
                            detailActivity.putExtra("BlogID", post_key);
                            holder.itemView.getContext().startActivity(detailActivity);
                        }
                    });
                } else {

                    holder.itemView.setVisibility(View.GONE);
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height = 0;
                    params.width = 0;
                    holder.itemView.setLayoutParams(params);

                }

            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @NonNull
            @Override
            public BlogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_blog, parent, false);
                return new BlogHolder(v);
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    class BlogHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, viewsTv;
        TextView authorTextView;
        ImageView photoImageView;
        TextView subTitleTextView,deleteBlog,editBlog;

        public BlogHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
            authorTextView = itemView.findViewById(R.id.author);
            photoImageView = (ImageView) itemView.findViewById(R.id.blogsImage);
            subTitleTextView = (TextView) itemView.findViewById(R.id.subTitle);
            viewsTv = itemView.findViewById(R.id.viewNoTv);

            deleteBlog = itemView.findViewById(R.id.deleteBlog);
            editBlog = itemView.findViewById(R.id.editBlog);
        }
    }
}
