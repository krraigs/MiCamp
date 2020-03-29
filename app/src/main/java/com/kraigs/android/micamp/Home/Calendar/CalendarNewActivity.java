package com.kraigs.android.micamp.Home.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.kraigs.android.micamp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kraigs.android.micamp.extras.SharedPrefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarNewActivity extends AppCompatActivity {

    private CalendarView mCalendarView;
    private List<EventDay> mEventDays = new ArrayList<>();
    DatabaseReference dateRef;
    private final List<String> dateKeyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_new);

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        retrieveData();
    }

    private void retrieveData(){
        dateRef = FirebaseDatabase.getInstance().getReference().child("Calendar").child(SharedPrefs.getCollege());
        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    dateKeyList.add(postSnapshot.getKey());
                }

                for (int i = 0;i<dateKeyList.size();i++){
                    String dateKey = dateKeyList.get(i);

                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = sdf.parse(dateKey);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        MyEventDay myEventDay = new MyEventDay(cal, R.drawable.calender_red_circle);
                        mCalendarView.setDate(myEventDay.getCalendar());
                        mEventDays.add(myEventDay);
                        mCalendarView.setEvents(mEventDays);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
