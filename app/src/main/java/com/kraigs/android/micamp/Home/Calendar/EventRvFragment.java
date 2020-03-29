package com.kraigs.android.micamp.Home.Calendar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.kraigs.android.micamp.extras.SharedPrefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventRvFragment extends Fragment {

    View v;
    RecyclerView eventsRv;
//    LinearLayoutManager llm;

    public EventRvFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_event_rv, container, false);

        eventsRv = v.findViewById(R.id.eventsRv);
//        llm = new LinearLayoutManager(getActivity());
//        eventsRv.setHasFixedSize(true);
        eventsRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query query = FirebaseDatabase.getInstance().getReference().child("Calendar").child(SharedPrefs.getCollege()).orderByChild("timestamp");

        FirebaseRecyclerOptions<Events> options = new FirebaseRecyclerOptions.Builder<Events>()
                .setQuery(query, Events.class)
                .build();

        FirebaseRecyclerAdapter<Events, EventViewHolder> adapter = new FirebaseRecyclerAdapter<Events, EventViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EventViewHolder holder, int position, @NonNull Events model) {
                String dateKey = getRef(position).getKey();

                try
                {
                    //create SimpleDateFormat object with source string date format
                    SimpleDateFormat sdfSource = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);

                    //parse the string into Date object
                    Date date = sdfSource.parse(dateKey);

                    //create SimpleDateFormat object with desired date format
                    SimpleDateFormat sdfDestination = new SimpleDateFormat("dd MMMM YYYY",Locale.ENGLISH);

                    //parse the date into another format
                    String strDate = sdfDestination.format(date);
                    holder.dateTv.setText(strDate);

                }
                catch(ParseException pe)
                {
                    System.out.println("Parse Exception : " + pe);
                }

                holder.typeTv.setText(model.getType());
                holder.detailTv.setText(model.getDetail());
            }

            @NonNull
            @Override
            public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.unit_event_rv,viewGroup,false);
                return new EventViewHolder(v);
            }
        };

        eventsRv.setAdapter(adapter);
//        llm.smoothScrollToPosition(eventsRv, null, 0);
//        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                eventsRv.smoothScrollToPosition(positionStart + 1);
//                llm.setReverseLayout(true);
//                llm.setStackFromEnd(true);
//            }
//        });

        adapter.startListening();
        return v;
    }

    private class EventViewHolder extends RecyclerView.ViewHolder {
        TextView dateTv,typeTv,detailTv;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTv = itemView.findViewById(R.id.eventDateTv);
            typeTv = itemView.findViewById(R.id.eventTypeTv);
            detailTv = itemView.findViewById(R.id.eventDescTv);
        }
    }
}
