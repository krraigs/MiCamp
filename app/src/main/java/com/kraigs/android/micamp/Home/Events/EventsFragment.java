package com.kraigs.android.micamp.Home.Events;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */

public class EventsFragment extends Fragment {

    View v;
    String category;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView rvUpcoming;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int position = FragmentPagerItem.getPosition(getArguments());

        if (position == 0){
            category = "UpcomingEvents";
        }else if (position ==1){
            category = "FeaturedEvents";
        }

        rvUpcoming = v.findViewById(R.id.eventRv);
        rvUpcoming.setHasFixedSize(true);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getActivity()));
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }catch (Exception e){}

        setUpRecyclerEventUpcoming(category);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_events, container, false);
        return v;

    }

    private void setUpRecyclerEventUpcoming(String category) {

        Query query = db.collection(SharedPrefs.getCollege()).document("Event").collection(category).orderBy("timestamp");
        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query,Event.class)
                .build();

        FirestoreRecyclerAdapter<Event,EventHolder2> adapter2 = new FirestoreRecyclerAdapter<Event, EventHolder2>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EventHolder2 holder, int position, @NonNull Event model) {

                holder.upDayTv.setText(model.getDate());
                holder.upLocatioTv.setText(model.getEventLocation());
                holder.upNameTv.setText(model.getEventName());

//                try{
//                    Bitmap bm = getBitmapFromURL(model.getEventPhotoUrl());
//                    Bitmap scaledBitmap = scaleBitmap(bm);
//                    Log.d("Bitmap","bitmap Loaded");
//                    holder.upEventPhoto.setImageBitmap(scaledBitmap);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.d("Bitmap","in catch");
//                }


                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                int height = 127;

                Picasso.get()
                        .load(model.getEventPhotoUrl())
                        .into(holder.upEventPhoto);

                final Context context = holder.upCard.getContext();
                String eventId = getSnapshots().getSnapshot(position).getId();
                holder.upCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context,EventsDetailActivity.class);
                        intent.putExtra("event_id",eventId);
                        intent.putExtra("event_type","UpcomingEvents");
                        context.startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public EventHolder2 onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.upcoming_event_list,viewGroup,false);
                return new EventHolder2(v);
            }
        };

        adapter2.notifyDataSetChanged();
        rvUpcoming.setAdapter(adapter2);
        adapter2.startListening();

    }

    public class EventHolder2 extends RecyclerView.ViewHolder {
        TextView upDayTv;
        TextView upLocatioTv;
        TextView upNameTv;
        ImageView upEventPhoto;
        CardView upCard;
        public EventHolder2(@NonNull final View itemView) {
            super(itemView);
            upDayTv = itemView.findViewById(R.id.up_event_day);
            upLocatioTv = itemView.findViewById(R.id.up_event_location);
            upNameTv = itemView.findViewById(R.id.up_event_name);
            upEventPhoto = itemView.findViewById(R.id.up_event_photo);
            upCard = itemView.findViewById(R.id.upcoming_event_card);

        }
    }

    private Bitmap scaleBitmap(Bitmap bm){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = 127;

        Bitmap background = Bitmap.createBitmap(width, 127, Bitmap.Config.ARGB_8888);

        float originalWidth = bm.getWidth();
        float originalHeight = bm.getHeight();

        Canvas canvas = new Canvas(background);

        float scale = width / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(bm, transformation, paint);

        return background;
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            return null;
        }
    }

}
