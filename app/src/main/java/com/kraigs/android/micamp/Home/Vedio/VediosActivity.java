package com.kraigs.android.micamp.Home.Vedio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
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

public class VediosActivity extends YouTubeBaseActivity {

    RecyclerView videoRv;
    String key,videoText,time,text;
    TextView videoTv,timeTv,vedioInfoTv;
    YouTubePlayerView youTubePv;
    YouTubePlayer ytp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedios);

        key = getIntent().getStringExtra("key");

        videoText = getIntent().getStringExtra("vedioText");
        text = getIntent().getStringExtra("text");
        time = getIntent().getStringExtra("time");

        videoTv = findViewById(R.id.videoTv);
        timeTv = findViewById(R.id.timeTv);
        youTubePv = findViewById(R.id.youTubePlayerView);
        vedioInfoTv = findViewById(R.id.vedioInfoTv);

        videoTv.setText(videoText);
        vedioInfoTv.setText(text);
        timeTv.setText(time);

        setUpRecyclerView();

        playVideo(key);

    }

    private void playVideo(String videoId) {
        youTubePv.initialize("AIzaSyAW5wD0GFiF1USESnDRAS9RN2G0LDUGD4M",
                new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        ytp = youTubePlayer;
                        youTubePlayer.cueVideo(videoId);
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                    }
                });

    }

    private void setUpRecyclerView() {

        videoRv = findViewById(R.id.videoRv);
        videoRv.setLayoutManager(new LinearLayoutManager(this));

        CollectionReference vedioRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Home").collection("Vedios");

       Query query = vedioRef.orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Vedio> options = new FirestoreRecyclerOptions.Builder<Vedio>()
                .setQuery(query,Vedio.class)
                .build();
        FirestoreRecyclerAdapter<Vedio,VideoListAdapter> adapter = new FirestoreRecyclerAdapter<Vedio, VideoListAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VideoListAdapter holder, int i, @NonNull Vedio vedio) {
                Timestamp timestamp = vedio.getTimestamp();
                GetTimeAgo gta = new GetTimeAgo();
                String timeS = gta.getTimeAgo(timestamp.getSeconds());

                holder.timeTv.setText(timeS);
                Picasso.get().load("http://img.youtube.com/vi/" + vedio.getVideoID() + "/0.jpg").into(holder.videoImage);
                holder.videoTv.setText(vedio.getVideoText());

                String text = vedio.getText();
                int maxLength2 = (text.length() < 28) ? text.length() : 28;
                text = text.substring(0, maxLength2) + "...";

                holder.infoTv.setText(text);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        key = vedio.getVideoID();
                        ytp.cueVideo(key);

                        time = timeS;
                        videoText = vedio.getVideoText();

                        videoTv.setText(videoText);
                        timeTv.setText(timeS);

                    }
                });
            }

            @NonNull
            @Override
            public VideoListAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_vedio_list
                        , parent, false);
                return new VideoListAdapter(v);
            }
        };

        videoRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    private class VideoListAdapter extends RecyclerView.ViewHolder {

        ImageView videoImage;
        TextView videoTv, timeTv,infoTv;

        public VideoListAdapter(@NonNull View itemView) {
            super(itemView);

            videoImage = itemView.findViewById(R.id.videoImage);
            videoTv = itemView.findViewById(R.id.videoTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            infoTv = itemView.findViewById(R.id.videoInfoTv);
        }
    }
}
