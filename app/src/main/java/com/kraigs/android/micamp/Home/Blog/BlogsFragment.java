package com.kraigs.android.micamp.Home.Blog;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.R;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */

public class BlogsFragment extends Fragment {

    View v;
    private static final String TAG = "BlogActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference myRef;
    private RecyclerView recyclerView;
    String currentUserID;
    LinearLayoutManager linearLayoutManager;

    public BlogsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_blogs, container, false);

        recyclerView = v.findViewById(R.id.blog_recycler_list);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddBlogActivity.class);
                intent.putExtra("type","add");
                startActivity(intent);
            }
        });

        return v;

    }


    @Override
    public void onStart() {
        super.onStart();

        Query query = db.collection("Public").document("Collections").collection("Blog").orderBy("timestamp", Query.Direction.DESCENDING);
        myRef = FirebaseFirestore.getInstance().collection("Public").document("Collections").collection("Blog");

        FirestoreRecyclerOptions<Blog> options = new FirestoreRecyclerOptions.Builder<Blog>()
                .setQuery(query, Blog.class)
                .build();

        FirestoreRecyclerAdapter<Blog, BlogHolder> adapter = new FirestoreRecyclerAdapter<Blog, BlogHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull BlogHolder holder, int position, @NonNull Blog model) {

                holder.deleteBlog.setVisibility(View.GONE);
                holder.editBlog.setVisibility(View.GONE);

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
                    if (updateText!=null){
                        int maxLength2 = (updateText.length() < 75) ? updateText.length() : 75;
                        updateText = updateText.substring(0, maxLength2) + "... By " + model.getmAuthor();
                        holder.subTitleTextView.setText(updateText);
                    }

                }

                holder.titleTextView.setText(model.getmTitle());

                final String post_key = getSnapshots().getSnapshot(position).getId();

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

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detailActivity = new Intent(getActivity(), BlogDetailActivity.class);
                        detailActivity.putExtra("BlogID", post_key);
                        holder.itemView.getContext().startActivity(detailActivity);
                    }
                });
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
        TextView subTitleTextView, deleteBlog, editBlog;

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
