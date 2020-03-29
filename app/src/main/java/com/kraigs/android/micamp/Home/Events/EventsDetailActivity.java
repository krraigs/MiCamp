package com.kraigs.android.micamp.Home.Events;

import android.content.Intent;
import android.net.Uri;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kraigs.android.micamp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class EventsDetailActivity extends AppCompatActivity {

    private String eventId, eventType;
    private DocumentReference myRef;

    private ImageView eventImage;
    private TextView eventLocation;
    private TextView eventDate, eventContent2, eventTimings,eventNameEt;
    private Button register, contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_detail);

        eventImage = findViewById(R.id.event_detail_image);
        eventLocation = findViewById(R.id.event_detail_location);
        eventDate = findViewById(R.id.event_detail_date);
        eventTimings = findViewById(R.id.event_detail_timings);
        eventContent2 = findViewById(R.id.event_detail_content2);
        register = findViewById(R.id.event_detail_register);
        contact = findViewById(R.id.event_detail_contact);
        eventNameEt =  findViewById(R.id.eventNameEt);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("event_id");
        eventType = intent.getStringExtra("event_type");

        if (eventId != null) {
            myRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Event").collection(eventType).document(eventId);
            myRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable final DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.contains("eventName")) {
                        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_event);
                        collapsingToolbar.setTitle(" ");
                        eventNameEt.setText((String) documentSnapshot.get("eventName"));

                    }

                    if (documentSnapshot.contains("eventLocation")) {
                        eventLocation.setText((String) documentSnapshot.get("eventLocation"));

                    }
                    if (documentSnapshot.contains("date")) {
                        eventDate.setText((String) documentSnapshot.get("date"));

                    }
                    if (documentSnapshot.contains("timings")) {
                        eventTimings.setText((String) documentSnapshot.get("timings"));

                    }

                    if (documentSnapshot.contains("content")) {
                        eventContent2.setText((String) documentSnapshot.get("content"));
                    }

                    if (documentSnapshot.contains("eventPhotoUrl")) {
                        Picasso.get()
                                .load((String) documentSnapshot.get("eventPhotoUrl"))
                                .into(eventImage);
                    }

                    register.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (documentSnapshot.contains("registerUrl")) {

                                String uriS = documentSnapshot.get("registerUrl").toString();

                                try{
                                    Uri uri = Uri.parse(uriS);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }catch (Exception e){
                                    Toast.makeText(EventsDetailActivity.this, "Form available has some problems! Try to contact organizer.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                            } else {
                                Toast.makeText(EventsDetailActivity.this, "Form not available!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    contact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (documentSnapshot.contains("contact")) {

                                final Uri u = Uri.parse("tel:" + (String) documentSnapshot.get("contact"));
                                Intent i = new Intent(Intent.ACTION_DIAL, u);
                                startActivity(i);

                            } else {
                                Toast.makeText(EventsDetailActivity.this, "Contact not Available", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });
        }


    }
}
