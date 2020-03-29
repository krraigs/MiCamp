package com.kraigs.android.micamp.Home.Vedio;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.GetTimeAgo;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class VedioListFragment extends Fragment {

    View v;
    RecyclerView videoRv;

    public VedioListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_vedio_list, container, false);
        videoRv = v.findViewById(R.id.videoRv);
        videoRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference().child("Blogs").child("Vedios");
        videoRef.keepSynced(true);
        CollectionReference vedioRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Home").collection("Vedios");

        Query query = vedioRef.orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Vedio> options = new FirestoreRecyclerOptions.Builder<Vedio>()
                .setQuery(query,Vedio.class)
                .build();

        FirestoreRecyclerAdapter<Vedio,VideoListAdapter> adapter = new FirestoreRecyclerAdapter<Vedio, VideoListAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VideoListAdapter holder, int i, @NonNull Vedio model) {

                Timestamp timestamp = model.getTimestamp();
                GetTimeAgo gta = new GetTimeAgo();
                String time = gta.getTimeAgo(timestamp.getSeconds());
                holder.timeTv.setText(time);

                Picasso.get().load("http://img.youtube.com/vi/" + model.getVideoID() + "/0.jpg").into(holder.videoImage);
                holder.videoTv.setText(model.getVideoText());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), VediosActivity.class);
                        intent.putExtra("key",model.getVideoID());
                        intent.putExtra("time",time);
                        intent.putExtra("vedioText",model.getVideoText());
                        intent.putExtra("text",model.getText());
                        startActivity(intent);
                    }
                });

                String text = model.getText();
                int maxLength2 = (text.length() < 30) ? text.length() : 30;
                text = text.substring(0, maxLength2) + "...";

                holder.infoTv.setText(text);

            }

            @NonNull
            @Override
            public VideoListAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_vedio_list,parent,false);
                return new VideoListAdapter(v);
            }
        };

        videoRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

        return v;
    }

    private class VideoListAdapter extends RecyclerView.ViewHolder {

        ImageView videoImage;
        TextView videoTv,timeTv,infoTv;

        public VideoListAdapter(@NonNull View itemView) {
            super(itemView);

            videoImage= itemView.findViewById(R.id.videoImage);
            videoTv= itemView.findViewById(R.id.videoTv);
            timeTv= itemView.findViewById(R.id.timeTv);
            infoTv = itemView.findViewById(R.id.videoInfoTv);
        }
    }



}
