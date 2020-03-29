package com.kraigs.android.micamp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.Home.Events.Event;
import com.kraigs.android.micamp.Home.Events.EventsActivity;
import com.kraigs.android.micamp.Home.Events.EventsDetailActivity;
import com.kraigs.android.micamp.Home.News.News;
import com.kraigs.android.micamp.Home.News.NewsActivity;
import com.kraigs.android.micamp.Order.ItemsListActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.tomer.fadingtextview.FadingTextView;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static java.util.concurrent.TimeUnit.SECONDS;

public class HomeFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private View v;
    LinearLayout booksLl,hostelsLl,buyLl;

    private final List<News> newsList = new ArrayList<>();
    private String[] titleArray = new String[3];
    private String[] textArray = new String[3];
    private RecyclerView eventsHomeRv;

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_home, container, false);

        eventsHomeRv = v.findViewById(R.id.eventsHomeRv);
        eventsHomeRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        eventsHomeRv.setHasFixedSize(true);

        final FadingTextView titleFTV = (FadingTextView) v.findViewById(R.id.fadingTextView);
        titleFTV.setTimeout(3, SECONDS);
        final FadingTextView textFTV = (FadingTextView) v.findViewById(R.id.updatesFTV);
        textFTV.setTimeout(3, SECONDS);

        Query newsCol = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Home").collection("News").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
        newsCol.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if(snapshots!=null){
                    newsList.addAll(snapshots.toObjects(News.class));
                    int usl = newsList.size();

                    if (usl > 2) {

                        for (int i = usl - 3; i < usl; i++) {
                            News news = newsList.get(i);

                            String updateText = news.getContent();
                            int maxLength2 = (updateText.length() < 95) ? updateText.length() : 95;
                            updateText = updateText.substring(0, maxLength2) + "...";

                            titleArray[i - usl + 3] = news.getTopic();
                            textArray[i - usl + 3] = updateText;
                        }

                        titleFTV.setTexts(titleArray);
                        textFTV.setTexts(textArray);
                    }
                }
            }
        });

        seeAll();
        return v;
    }


    private void seeAll() {

        TextView seeAllTv = (TextView) v.findViewById(R.id.seeAllUpdates);
        TextView seeAllEvents = (TextView) v.findViewById(R.id.seeAllEvents);

        v.findViewById(R.id.orderCv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ItemsListActivity.class);
                startActivity(intent);
            }
        });

        seeAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewsActivity.class);
                startActivity(intent);
            }
        });

        seeAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EventsActivity.class);
                startActivity(intent);
            }
        });

        v.findViewById(R.id.viewCv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewsActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        setUpRecyclerEvent();
    }
    private void setUpRecyclerEvent() {

        Query query = db.collection(SharedPrefs.getCollege()).document("Event").collection("UpcomingEvents").orderBy("timestamp");
        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>().setQuery(query, Event.class).build();
        FirestoreRecyclerAdapter<Event, EventHolder2> adapterEvent = new FirestoreRecyclerAdapter<Event, EventHolder2>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EventHolder2 holder, int position, @NonNull final Event model) {

                holder.upDayTv.setText(model.getDate());
                holder.upNameTv.setText(model.getEventName());
                holder.locationTv.setText(model.getEventLocation());

                String eventId = getSnapshots().getSnapshot(position).getId();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), EventsDetailActivity.class);
                        intent.putExtra("event_id", eventId);
                        intent.putExtra("event_type", "UpcomingEvents");
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public EventHolder2 onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_home_events, viewGroup, false);
                return new EventHolder2(v);
            }
        };

        eventsHomeRv.setAdapter(adapterEvent);
        adapterEvent.notifyDataSetChanged();
        adapterEvent.startListening();

    }

    public class EventHolder2 extends RecyclerView.ViewHolder {
        TextView upDayTv, upNameTv,locationTv;

        EventHolder2(@NonNull final View itemView) {
            super(itemView);
            upDayTv = itemView.findViewById(R.id.up_event_day);
            upNameTv = itemView.findViewById(R.id.up_event_name);
            locationTv = itemView.findViewById(R.id.up_event_location);
        }
    }
}