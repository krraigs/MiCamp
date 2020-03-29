package com.kraigs.android.micamp.Home.Blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class BlogDetailActivity extends AppCompatActivity {

    private TextView blogTitle,author;
    private ImageView blogImage;
    private TextView contentDetail,viewsTv;
    private DocumentReference myRef;
    String post_key = null;
    public static final String TAG = "BlogDetailActivity";
    FirebaseAuth mAuth;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        blogImage = findViewById(R.id.blogsImageDetail);
        contentDetail = findViewById(R.id.contentDetail);
        blogTitle = findViewById(R.id.blogTitle);
        author = findViewById(R.id.authorNew);

        post_key = getIntent().getStringExtra("BlogID");
        viewsTv = findViewById(R.id.viewsTv);

        myRef = FirebaseFirestore.getInstance().collection("Public").document("Collections").collection("Blog").document(post_key);

        Map<String, Object> read = new HashMap<>();
        read.put("read", "true");

        myRef.collection("readBy").document(currentUserID).set(read).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                myRef.collection("readBy").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", task.getResult().size() + "");
                            viewsTv.setText(task.getResult().size() + " views");

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });

        myRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());
                CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
                String blog_title = (String) snapshot.get("mTitle").toString();
                String blog_author = (String) snapshot.get("mAuthor");
                String blog_content = (String) snapshot.get("mInfo").toString();


//                blogTitle.setText(blog_title);
                collapsingToolbar.setTitle(" ");
                blogTitle.setText(blog_title);
                author.setText("By " + blog_author);
                contentDetail.setText(blog_content);

                if(snapshot.contains("mPhotoUrl")){
                    String blog_imageUrl = (String) snapshot.get("mPhotoUrl").toString();
                    Picasso.get()
                            .load(blog_imageUrl)
                            .into(blogImage);
                } else{
                    blogImage.setVisibility(View.GONE);
                }

            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }
}
